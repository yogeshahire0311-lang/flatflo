# Phase 0 Research: Unified Rental Search (FlatFlo MVP — expanded scope)

Regenerated 2026-07-03 after the MVP was expanded to adopt the attached UI spec (duplicate grouping, best-deal smart layer, map toggle, best-deal sort, budget range). No `[NEEDS CLARIFICATION]` markers remain in the spec; this records the technical decisions and their rationale.

## D1. Two swap seams: data feed and grouping

- **Decision**: `ListingSource` returns raw `Listing`s (seeded now, agent-backed later). A separate `ListingGrouper` turns `List<Listing>` into `List<ListingGroup>` (dedup + area-average + best-deal). `SearchService` depends on both interfaces.
- **Rationale**: FR-002 requires the feed to be swappable; FR-003 says the *grouping algorithm* is a backend concern the UI shouldn't see. Two seams let us replace the feed and improve the dedup algorithm independently, without touching the controller or UI.
- **Alternatives considered**: One combined "source that returns groups" — rejected; conflates data acquisition with dedup and makes the seeded/agent swap harder. Grouping in the controller — rejected; not unit-testable, leaks logic into the web layer.

## D2. Grouping (duplicate detection) for the MVP

- **Decision**: `SeedListingGrouper` groups raw listings by a **dedup key** = normalized `(locality, bhk, areaSqft-bucket, society/title-normalized)`. Listings sharing a key form one `ListingGroup`; their offers are sorted ascending by price; `cheapestPrice = offers[0].price`; `primaryPhoto` = cheapest offer's first photo (or first available).
- **Rationale**: The spec explicitly scopes the *algorithm* as a backend concern and lets the MVP satisfy it over the seeded feed. A deterministic key over the fields we control in the seed is enough to demonstrate merged cards and the dup-count meta line, and is trivially unit-testable. The interface (`ListingGrouper`) lets a fuzzy/ML matcher replace it later.
- **Alternatives considered**: Geo-distance + fuzzy title matching — deferred (right for real data, overkill for a curated seed). No grouping (pass-through) — rejected; the expanded spec requires grouped cards.
- **Note**: Seed data will be authored so the dedup key is reliable (same flat gets identical key fields across sources).

## D3. Area-average and the 10% best-deal rule

- **Decision**: `AreaAverageCalculator` computes the mean rent per `(locality, bhk)` across all raw listings in the feed. For each group, `isBestDeal = cheapestPrice <= areaAveragePrice * 0.90`; when true, `bestDealDiscountPct = round((areaAveragePrice − cheapestPrice) / areaAveragePrice * 100)`.
- **Rationale**: FR-007/FR-008. Deriving the average from the seed itself keeps the MVP self-contained (Assumptions). Integer-percent rounding matches the "{pct}% below area average" copy.
- **Alternatives considered**: Median instead of mean (more robust to outliers) — reasonable, but mean is simpler and the seed is curated; revisit with real data. External market-rent source — deferred.
- **Scarcity guard**: If the seed produces too many best deals (UI spec: >~1 per screen), tighten by curating seed prices rather than changing the 10% rule in code.

## D4. Reuse and extend the existing model

- **Decision**: Keep the existing `com.flatflow.listing.Listing` record + enums as the **raw** model. Add `ListingGroup` and `SourceOffer` as new domain types. Expose `ListingGroupDto` / `SourceOfferDto` at the API boundary (never serialize internal types directly).
- **Rationale**: Spring Boot best practice (DTOs at the boundary) + the existing record already carries locality/bhk/rent/area/furnishing/sourcePlatform/sourceUrl. The UI spec adds `photos`, `floor`, `lastUpdated`, `lat/lng` — see D5.
- **Alternatives considered**: Serialize `ListingGroup` directly — rejected (couples wire format to internals, no room for display formatting/labels).

## D5. Model gaps vs. the UI spec (fields to add to the raw Listing / seed)

The UI spec's raw `Listing` needs fields the current record lacks. **Decision**: extend the seed + raw model with:

| UI-spec field | Current record | Action |
|---|---|---|
| `price` (single monthly rent) | `rentMin`/`rentMax` | Use `rentMin` as the offer price (headline "from"); keep `rentMax` optional/ignored for offer price |
| `photos: string[]` | absent | Add to seed + raw model |
| `floor?` | absent | Add (optional) |
| `lastUpdated` (ISO) | absent | Add (drives "newest" sort) |
| `lat`/`lng` | absent | Add (unused by MVP UI beyond future map; carried through) |
| `title` | derivable from society+bhk+locality | Compose canonical title in the grouper/DTO |

- **Rationale**: These are needed for photos/fallback (FR-018), "newest" sort (FR-009), and the floor line (FR-012). Adding them to the seed and record is low-cost and keeps the DTO honest.
- **Alternatives considered**: Fabricate photos/dates at the DTO layer — rejected; the seed should be the single source of truth so behavior is testable.

## D6. Filter, sort, paginate over groups

- **Decision**: `SearchService` filters groups by budget **range** (`budgetMin <= cheapestPrice <= budgetMax`), BHK, and optional furnishing; sorts by `SortMode` — BEST_DEAL (default: best deals first, then by discount desc / price asc), PRICE_ASC, PRICE_DESC, NEWEST (max `lastUpdated` in the group) — then paginates ~20 groups/page consistently. The response carries `count`, `dupCount = sum(offers per group) − groups`, active `sort`, per-source status, and paging meta.
- **Rationale**: FR-006, FR-009, FR-011. All ordering server-side for cross-page consistency. Dup-count computed where the groups are known.
- **Alternatives considered**: Client-side sort/paginate — rejected (breaks consistency, pushes logic to UI). Filtering on `rentMax` — rejected; cheapest price is the card's headline and the natural filter target.

## D7. Partial / all-source failure model

- **Decision**: The search response includes `sources: [{ sourcePlatform, reachable }]`. Partial failure → succeeding sources' groups still returned + optional "N of M sources" note (FR-017). All sources down → `503 ALL_SOURCES_UNAVAILABLE` so the UI shows retry, not the empty state. In the MVP (single seeded source) all are reachable.
- **Rationale**: FR-017 / edge cases; the seam exists for real sources even though the seed can't fail.
- **Alternatives considered**: Omit source status in the MVP — rejected; the UI spec's states table and meta-line note require it, and adding it later would change the contract.

## D8. Frontend architecture (Angular 20 standalone)

- **Decision**: Standalone components matching the UI spec — `search-page` (container), `filter-bar` (sticky; reactive forms; location picker, budget range, BHK, furnishing, Filters count, Map toggle), `results-meta` (count/dup/sort), `listing-card` (grouped: photo+fallback, best-deal badge, price+caption, source chips, whole-card & per-chip redirect), `ui-states` (skeleton/empty/partial/error). Typed `search.service.ts` over `HttpClient`. Filter/sort state synced to URL query params. Add `provideHttpClient()` to `app.config.ts`, `/search` route.
- **Rationale**: Angular 20 defaults (standalone + providers) and the existing scaffold. Component split mirrors the spec's Sections 4–7 so each is independently spec-able.
- **Alternatives considered**: NgModules — rejected (against Angular 20 defaults). One monolithic component — rejected (states/card/filter-bar are separately testable).

## D9. Image handling & accessibility

- **Decision**: Card `<img>` uses native lazy-loading and an `onerror` handler swapping to a category-icon placeholder (never broken-image). Whole card is a focusable click target with a visible focus ring; each source chip is a separate focus stop with an accessible label ("View this listing on {source}, ₹{price} per month"); best-deal conveyed via the text subtext, not color alone.
- **Rationale**: FR-018, FR-022 directly from the UI spec Sections 9–10.
- **Alternatives considered**: Detecting/ messaging specific hotlink failures — rejected per UI spec (treat as missing photo).

## D10. Dev integration & testing

- **Decision**: Angular dev proxy `/api` → `http://localhost:8080` (no CORS in dev). Backend tests: `SeedListingGrouperTest` (dedup key, area-average, 10% threshold incl. just-above/just-below boundaries), `SearchServiceTest` (budget-range filter, all 4 sort modes, pagination, dup-count), `SearchControllerTest` (`@WebMvcTest` contract incl. empty + 503). Frontend: service HTTP specs + component specs (best-deal badge threshold rendering, single vs multi-source chips, image fallback, empty/error states).
- **Rationale**: Test slices keep tests fast; boundary tests pin the 10% rule and sort correctness — the highest-risk new logic.
- **Alternatives considered**: Full `@SpringBootTest` everywhere — rejected as heavier than needed.

## Summary of decisions

| ID | Decision |
|----|----------|
| D1 | Two seams: `ListingSource` (feed) + `ListingGrouper` (dedup/insight) |
| D2 | MVP grouping by deterministic dedup key over seed fields |
| D3 | Area-average = mean rent per locality+BHK; best deal if cheapest ≤ 90% of avg; integer discount % |
| D4 | Reuse raw `Listing`; add `ListingGroup`/`SourceOffer`; DTOs at boundary |
| D5 | Extend seed+raw model with photos, floor, lastUpdated, lat/lng; price = rentMin |
| D6 | Server-side filter (budget range/BHK/furnishing) → sort (4 modes, best-deal default) → paginate 20 groups |
| D7 | Per-source status in response; 503 when all sources down |
| D8 | Angular 20 standalone components mirroring UI spec; URL-synced filter/sort state |
| D9 | Lazy images + icon fallback; keyboard/focus + accessible chip labels |
| D10 | Dev proxy; grouper/service/controller unit+slice tests with best-deal boundary cases |

No unresolved unknowns. Ready for Phase 1.
