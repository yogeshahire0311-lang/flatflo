# Quickstart & Validation: Unified Rental Search (FlatFlo MVP — expanded scope)

Regenerated 2026-07-03 for grouped results + best-deal + budget range + sort modes. References [contracts/search-api.md](./contracts/search-api.md) and [data-model.md](./data-model.md) rather than duplicating them. Implementation code belongs in `tasks.md` / the implementation phase.

## Prerequisites

- Java 21 (backend toolchain pinned in `java/flatflo/build.gradle`)
- Node.js + npm (Angular 20)
- A seeded feed at `java/flatflo/src/main/resources/listings-seed.json` authored to exercise the expanded scope:
  - The **same flat on ≥2 sources** (identical dedup-key fields, different `sourcePlatform`/`sourceUrl`/`price`) → a merged card with comparison chips.
  - At least one flat whose cheapest price is **≥10% below** its locality+BHK average → a best-deal card; and one **just below 10%** → no badge (boundary).
  - A **single-source** flat → one plain chip.
  - At least one listing with **empty/broken `photos`** → placeholder fallback.
  - Varied `lastUpdated` dates → "newest" sort is observable.

## Run

### Backend (Spring Boot, :8080)
```bash
cd java/flatflo
./gradlew bootRun
```

### Frontend (Angular, :4200)
```bash
cd angular
npm install
npm start
```
Angular dev server proxies `/api` → `http://localhost:8080`. Open http://localhost:4200/search.

## Automated tests
```bash
# Backend: grouper (dedup/area-average/10% rule) + search service (filter/sort/paginate) + @WebMvcTest contract
cd java/flatflo && ./gradlew test

# Frontend: service + component specs
cd angular && npm test
```

## Manual validation scenarios

Each maps to spec acceptance criteria / FRs. Run against the seeded feed.

### V1 — Unified, de-duplicated search (US1 / FR-001–003, FR-011)
1. Open `/search`. Confirm location is a **picker** from `GET /api/areas` (no free text).
2. Select "Goregaon East", budget "₹20,000–₹40,000", BHK "2 BHK"; submit.
3. **Expect**: a single-column list of **grouped cards**; a flat on multiple sources appears **once**; the meta line reads "{count} flats found, {dupCount} duplicates merged".

### V2 — Source comparison chips (US1 #3 / FR-004, FR-012)
1. Find a multi-source card.
2. **Expect**: card price = cheapest source; an "Also listed on" row with one equal-weight chip per additional source showing name + that source's price. A single-source card shows one plain chip (no price repeat, no "Also listed on").

### V3 — Best deal badge + threshold (US3 / FR-007, FR-008)
1. Identify the seeded best-deal flat (cheapest ≥10% below area avg).
2. **Expect**: "Best deal" badge + subtext "{pct}% below area average" + a 2px accent border. The boundary flat just below 10% shows **no** badge/border.

### V4 — Sorting (US4 / FR-009)
1. Confirm default sort is **best deal** (meta line "Sorted by best deal").
2. Switch to "price low–high", "price high–low", "newest".
3. **Expect**: order changes accordingly; ordering stays consistent across pages.

### V5 — Filters + URL state (US4 / FR-010, FR-013)
1. Set furnishing = "Furnished".
2. **Expect**: only furnished groups remain, no full page reload, URL query params update; the "Filters" button shows an active-filter count badge. Clearing filters restores the set and removes the badge.

### V6 — Budget range (FR-001, FR-013)
1. Set a range that excludes the cheapest and most expensive seeded flats.
2. **Expect**: only groups whose cheapest price falls within the range remain.

### V7 — Pagination (FR-006)
1. Search a combination with >20 matching groups.
2. **Expect**: ~20 groups on page 0, a load-more/next control, page 1 continues the same sort order with no duplicates/gaps (`hasMore` true → false at the end).

### V8 — Redirect: card vs chip (US2 / FR-005, FR-020)
1. Click a multi-source card body → **expect** the cheapest source's listing opens in a new tab; results tab preserved.
2. Click a specific (non-cheapest) source chip → **expect** that source's listing opens instead.

### V9 — Image fallback (FR-018)
1. View the card whose photos are empty/broken.
2. **Expect**: a category-icon placeholder (never a broken-image glyph); below-the-fold images lazy-load.

### V10 — States (FR-015–017)
1. **Empty**: search a combo with no matches → "No flats match these filters" + widen suggestion.
2. **Loading**: on submit, skeleton cards appear without blocking the filter bar.
3. **Partial/all-source** (contract): with a source marked unreachable the UI notes "Results from N of M sources"; when all are unreachable the API returns `503 ALL_SOURCES_UNAVAILABLE` and the UI shows a full-width error + retry (not the empty state).

### V11 — Accessibility (FR-022)
1. Tab through results.
2. **Expect**: each card is focusable with a visible ring; each source chip is its own focus stop with an accessible label ("View this listing on {source}, ₹{price} per month"); best-deal status is conveyed by text, not color alone.

### V12 — Performance smoke (FR-021 / SC-005)
1. Submit a normal search.
2. **Expect**: first page renders within ~3s (trivially met over the seed; the ~8s source-drop behavior is covered by aggregation-layer unit tests).

## Definition of done for this feature

- V1–V12 pass against the seeded feed.
- `GET /api/areas` and `GET /api/search` conform to [contracts/search-api.md](./contracts/search-api.md), including grouped shape, best-deal fields, dup-count, and 503 behavior.
- Swapping the `ListingSource` implementation, or the `ListingGrouper` implementation, requires **no** change to `SearchController`, DTOs, or the Angular app (FR-002/FR-003) — verifiable by unit tests injecting alternate implementations.
