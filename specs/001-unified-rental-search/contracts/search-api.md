# API Contract: Unified Rental Search (FlatFlo MVP — expanded scope)

Regenerated 2026-07-03 for grouped results + best-deal + budget range + sort modes. REST/JSON, base path `/api`. Backend: Spring Boot 4 (`@RestController`). Anonymous (no auth). Money in INR (integers), area in sq ft, rent monthly.

Field shapes reference [data-model.md](../data-model.md).

---

## GET /api/areas

Predefined supported areas backing the location picker (FR-001).

**200 OK**
```json
{
  "areas": [
    { "id": "GOREGAON_EAST", "name": "Goregaon East" },
    { "id": "MALAD_WEST",    "name": "Malad West" },
    { "id": "THANE",         "name": "Thane" }
  ]
}
```
`id` is passed back as `location` to `/api/search`.

---

## GET /api/search

Runs a unified search and returns a **page of grouped listings** (duplicates merged) with best-deal signals, ordered by the active sort, plus dedup + per-source metadata.

**Query parameters**

| Param | Type | Required | Default | Notes |
|-------|------|----------|---------|-------|
| `location` | string (SupportedArea id) | Yes | — | Must be valid (from `/api/areas`) |
| `bhk` | `ONE_BHK`\|`TWO_BHK`\|`THREE_BHK` | Yes | — | |
| `budgetMin` | integer (INR) | No | — | Range lower bound |
| `budgetMax` | integer (INR) | No | — | Range upper bound |
| `furnishing` | `UNFURNISHED`\|`SEMI_FURNISHED`\|`FULLY_FURNISHED` | No | — | Filter (FR-013) |
| `sort` | `BEST_DEAL`\|`PRICE_ASC`\|`PRICE_DESC`\|`NEWEST` | No | `BEST_DEAL` | FR-009 |
| `page` | integer ≥ 0 | No | 0 | FR-006 |

Filter: a group qualifies when its `cheapestPrice` is within `[budgetMin, budgetMax]` (either bound optional), `bhk` matches, and (if provided) furnishing matches.

**200 OK** — results present
```json
{
  "results": [
    {
      "groupId": "goregaon-east-2bhk-siddharth-650",
      "title": "2 BHK in Goregaon East",
      "metaLine": "650 sq ft · Semi-furnished · floor 4",
      "locality": "Goregaon East",
      "priceDisplay": "₹32,000",
      "cheapestPrice": 32000,
      "isBestDeal": true,
      "bestDealDiscountPct": 12,
      "primaryPhotoUrl": "https://.../thumb.jpg",
      "available": true,
      "sources": [
        {
          "sourcePlatform": "NoBroker",
          "sourceUrl": "https://www.nobroker.in/property/...",
          "priceDisplay": "₹32,000",
          "price": 32000,
          "accessibleLabel": "View this listing on NoBroker, ₹32,000 per month"
        },
        {
          "sourcePlatform": "MagicBricks",
          "sourceUrl": "https://www.magicbricks.com/propertyDetails/...",
          "priceDisplay": "₹34,500",
          "price": 34500,
          "accessibleLabel": "View this listing on MagicBricks, ₹34,500 per month"
        }
      ]
    }
  ],
  "count": 142,
  "dupCount": 38,
  "sort": "BEST_DEAL",
  "page": 0,
  "pageSize": 20,
  "hasMore": true,
  "sources": [
    { "sourcePlatform": "MagicBricks", "reachable": true },
    { "sourcePlatform": "NoBroker",    "reachable": true },
    { "sourcePlatform": "Housing.com", "reachable": true }
  ]
}
```

**Contract rules**
- `results` are **groups** (one per de-duplicated flat), each with `sources` ascending by price; `cheapestPrice == sources[0].price` and `priceDisplay` formats it.
- Ordering follows `sort`: `BEST_DEAL` (best deals first, then higher discount / lower price), `PRICE_ASC`/`PRICE_DESC` by `cheapestPrice`, `NEWEST` by newest offer date. Consistent across pages (FR-009).
- `results.length <= pageSize` (~20 groups). `hasMore = (page+1)*pageSize < count`.
- `count` = total matching groups; `dupCount = Σ(sources.length across matching groups) − count` (meta line, FR-011).
- `isBestDeal` true only when `cheapestPrice ≤ 90%` of the group's locality+BHK area average; `bestDealDiscountPct` present iff `isBestDeal` (FR-007/FR-008).
- A single-source group has `sources.length == 1` (UI renders one plain chip, no comparison row).
- Missing optional fields serialize as `null` (e.g., `primaryPhotoUrl: null`) → UI placeholder (FR-018). Floor omitted from `metaLine` when absent.
- `sources[]` (top level) reports reachability per configured source; any `reachable:false` → UI shows "Results from N of M sources" (FR-017). MVP seed: all `true`.

**200 OK** — empty state (valid search, no matches; FR-016)
```json
{ "results": [], "count": 0, "dupCount": 0, "sort": "BEST_DEAL", "page": 0, "pageSize": 20, "hasMore": false, "sources": [ { "sourcePlatform": "SeedFeed", "reachable": true } ] }
```
UI shows "No flats match these filters" + widen-budget/BHK suggestion.

**400 Bad Request** — invalid input
```json
{ "error": "INVALID_LOCATION", "message": "Unknown area id 'XYZ'. Choose from /api/areas." }
```
Cases: `location` not a supported area; `bhk` missing/invalid; `budgetMin`/`budgetMax` negative or `budgetMin > budgetMax`; invalid `sort`; negative `page`.

**503 Service Unavailable** — all sources unreachable (FR-017 "all sources down")
```json
{ "error": "ALL_SOURCES_UNAVAILABLE", "message": "No listing sources could be reached. Please retry." }
```
Distinguished from empty results so the UI shows the full-width error + retry, not the empty state. Not expected in the MVP (seed always available); reserved for real sources.

---

## Redirect (client-side, no dedicated endpoint)

Redirection (FR-005, User Story 2) uses `sourceUrl`:
- **Card body click** → open `sources[0].sourceUrl` (cheapest) in a new tab.
- **Source chip click** → open that offer's `sourceUrl`, overriding the default.
FlatFlo does not proxy sources; the results tab is preserved. Unreachable targets are surfaced by the browser / a client-side notice (FR-020).

---

## Performance contract (FR-021)

- First page returns within ~3s under normal conditions.
- Aggregation drops any source not responding within ~8s and reflects it in top-level `sources[].reachable`; it does not block beyond that bound.
- Trivially met over the in-memory seed; the timeout/partial-source behavior lives in the aggregation layer so it holds when real sources replace the seed (FR-002).

---

## Notes for the map toggle (FR-014)

No endpoint. The "Map" toggle is a client-side entry point only; the map screen and its data needs are a separate later feature. `lat`/`lng` are carried on listings/groups for future use but are not required by any endpoint in this contract.
