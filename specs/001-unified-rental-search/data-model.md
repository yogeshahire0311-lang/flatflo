# Phase 1 Data Model: Unified Rental Search (FlatFlo MVP — expanded scope)

Regenerated 2026-07-03 for the expanded spec (duplicate grouping, best-deal, budget range, sort modes). Internal model reuses/extends the existing `com.flatflow.listing` classes and adds a `grouping` package; API-boundary DTOs are new. No database — everything is computed in memory over the seeded feed.

## Raw model (internal)

### Listing (raw, pre-grouping — extend existing `com.flatflow.listing.Listing`)

| Field | Type | Status | Notes |
|-------|------|--------|-------|
| `id` | String | existing | Convenience id |
| `society` | String | existing | Building/society; part of title + dedup key |
| `locality` | String | existing | Maps to searchable area; part of dedup key + area-average key |
| `bhkType` | `BhkType` | existing | ONE/TWO/THREE_BHK; dedup + area-average key |
| `rentMin` | int | existing | **Offer price** (monthly, INR) used everywhere as the source's price |
| `rentMax` | int | existing | Optional display range upper bound; not the offer price |
| `areaSqFt` | int | existing | Size; part of dedup key (bucketed) |
| `areaType` | `AreaType` | existing | CARPET / BUILT_UP |
| `furnishing` | `Furnishing` | existing | Filter |
| `amenities` | List<String> | existing | Optional |
| `nearby` | List<String> | existing | Optional |
| `status` | `ListingStatus` | existing | AVAILABLE / UNAVAILABLE (display only) |
| `sourcePlatform` | String | existing | Source id/name |
| `sourceUrl` | String | existing | Deep link (redirect target) |
| `photos` | List<String> | **NEW** | Source-hosted URLs; may be empty/fail (FR-018) |
| `floor` | Integer | **NEW** | Optional; card metadata line |
| `lastUpdated` | String (ISO date) | **NEW** | Drives "newest" sort |
| `lat` | double | **NEW** | Carried through (future map); not used by MVP list UI |
| `lng` | double | **NEW** | Carried through |

**Identity (raw)**: `(sourcePlatform, sourceUrl)` — stable across feed refreshes; basis for redirect.

**Validation / rules**:
- `rentMin >= 0`.
- Missing optional display fields (photos empty, floor null) render placeholders (FR-018) but never exclude a listing.

### Enums (existing, unchanged)
`BhkType`, `AreaType`, `Furnishing`, `ListingStatus`.

## Grouping model (internal — new `grouping` package)

### ListingGroup
Output of `ListingGrouper`; the unit the UI renders (one per de-duplicated flat).

| Field | Type | Notes |
|-------|------|-------|
| `groupId` | String | Stable id (from dedup key) |
| `title` | String | Canonical title (e.g., "2 BHK in {locality}") |
| `locality` | String | |
| `areaSqFt` | int | |
| `bhk` | `BhkType` | |
| `furnishing` | `Furnishing` | |
| `floor` | Integer | Optional |
| `lat` / `lng` | double | Carried from primary listing |
| `offers` | List<SourceOffer> | Sorted ascending by price; length ≥ 1 |
| `cheapestPrice` | int | = `offers[0].price` |
| `areaAveragePrice` | int | Mean rent for this `(locality, bhk)` across the feed |
| `isBestDeal` | boolean | `cheapestPrice <= areaAveragePrice * 0.90` (FR-007) |
| `bestDealDiscountPct` | Integer | Present only when `isBestDeal`; `round((avg − cheapest)/avg*100)` |
| `primaryPhoto` | String | From the cheapest offer's listing, or first available; null → placeholder |
| `newestUpdated` | String (ISO) | Max `lastUpdated` across offers; drives "newest" sort |

**Validation / rules**:
- `offers` non-empty and price-ascending. `cheapestPrice` == first offer price.
- `dupCount` contribution of a group = `offers.length − 1`.
- `bestDealDiscountPct` absent when `isBestDeal == false`.

### SourceOffer
One source's offer within a group.

| Field | Type | Notes |
|-------|------|-------|
| `sourcePlatform` | String | Source id/name (chip label + badge) |
| `sourceUrl` | String | Deep link (redirect target for this chip) |
| `price` | int | Monthly rent (INR) for this source |
| `lastUpdated` | String (ISO) | |

## Query model (internal)

### SearchQuery
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `location` | String (SupportedArea id) | Yes | Must be a supported area |
| `budgetMin` | Integer | No | Range lower bound |
| `budgetMax` | Integer | No | Range upper bound |
| `bhk` | `BhkType` | Yes | |
| `furnishing` | `Furnishing` | No | Filter |
| `sort` | `SortMode` | No (default BEST_DEAL) | BEST_DEAL / PRICE_ASC / PRICE_DESC / NEWEST |
| `page` | int | No (default 0) | Zero-based |

Filter rule: keep groups where `cheapestPrice` is within `[budgetMin, budgetMax]` (either bound may be absent), BHK matches, and (if set) any offer/group furnishing matches.

### SortMode (enum)
`BEST_DEAL` (best deals first, then higher discount / lower price), `PRICE_ASC` (cheapestPrice ↑), `PRICE_DESC` (cheapestPrice ↓), `NEWEST` (`newestUpdated` ↓).

### SupportedArea
Predefined searchable locations (id + display name); backs `/api/areas` and the picker; matches `Listing.locality` in the seed.

## API-boundary DTOs (new)

### ListingGroupDto (FR-012)
| Field | Type | Notes |
|-------|------|-------|
| `groupId` | String | |
| `title` | String | |
| `metaLine` | String | "{area} sq ft · {furnishing} · floor {n}" (floor omitted if null) |
| `locality` | String | |
| `priceDisplay` | String | Cheapest, formatted "₹35,000"; caption "per month" added by UI |
| `cheapestPrice` | int | Numeric ref |
| `isBestDeal` | boolean | |
| `bestDealDiscountPct` | Integer | Only when best deal → badge subtext "{pct}% below area average" |
| `primaryPhotoUrl` | String | Nullable → UI placeholder |
| `sources` | List<SourceOfferDto> | Ascending by price; ≥1 |
| `available` | boolean | From status |

### SourceOfferDto (chip)
| Field | Type | Notes |
|-------|------|-------|
| `sourcePlatform` | String | Chip label + badge |
| `sourceUrl` | String | Redirect target |
| `priceDisplay` | String | e.g., "₹24,000" |
| `price` | int | Numeric |
| `accessibleLabel` | String | "View this listing on {source}, ₹{price} per month" (FR-022) |

### SourceStatusDto (FR-017)
`{ sourcePlatform: string, reachable: boolean }`.

### AreaDto
`{ id: string, name: string }`.

### SearchResponseDto
| Field | Type | Notes |
|-------|------|-------|
| `results` | List<ListingGroupDto> | Current page, ordered per active sort |
| `count` | int | Total groups matching (all pages) |
| `dupCount` | int | `sum(offers per matching group) − count` (meta line, FR-011) |
| `sort` | String | Active `SortMode` |
| `page` | int | Zero-based |
| `pageSize` | int | ~20 (FR-006) |
| `hasMore` | boolean | Next page exists |
| `sources` | List<SourceStatusDto> | Per-source reachability |

## Relationships

```text
ListingSource ──▶ [Listing (raw)]
                      │
        ListingGrouper (dedup key ─▶ groups; AreaAverageCalculator ─▶ avg per locality+BHK; 10% rule ─▶ best deal)
                      ▼
                [ListingGroup] ──(SearchService: filter budget-range/BHK/furnishing ─▶ sort(mode) ─▶ paginate)
                      │                                                                     │
        SupportedArea constrains SearchQuery.location                                        ▼
        per-source status collected during aggregation ───────────────▶ SearchResponseDto (groups + meta + sources)
                      │
                      └─(map to DTOs at boundary)─▶ ListingGroupDto / SourceOfferDto
```

## Lifecycle / state

- Read-only in the MVP (no create/update/delete via the app).
- `ListingStatus` is display-only; FlatFlo does not guarantee live availability (FR-020) and does not mutate source state.
- Grouping + area-average are recomputed from the current feed on each refresh; `groupId` derives from the dedup key so it is stable for the same flat across refreshes.
