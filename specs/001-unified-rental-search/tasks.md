---
description: "Task list for Unified Rental Search (FlatFlo MVP)"
---

# Tasks: Unified Rental Search (FlatFlo MVP)

> **Status — reconciled 2026-07-03** (39/56 tasks done)
>
> | Phase | State |
> |---|---|
> | Setup (T001–T004) | ✅ done |
> | Foundational (T005–T022) | ✅ done |
> | US1 — search + grouped cards (T023–T032) | ✅ done |
> | **US2 — redirect (T033–T036)** | ❌ **not started** — `listing-card` has no click/redirect or availability notice |
> | **US3 — best-deal presentation (T037, T038, T040)** | ❌ **not started** — badge/subtext/border not rendered; only backend default sort T039 ✅ done |
> | US4 — filters + sort (T041–T047) | ✅ done, **except T045 Map-toggle** (deferred; furnishing selector, sticky bar, Filters badge done) |
> | Polish (T048–T056) | ❌ not started (T050 has native lazy-load only; no `onerror` fallback yet) |
>
> **Recommended next:** US2 (T033–T036) then US3 presentation (T037/T038/T040) — these complete the MVP-critical card behavior before Phase 7 Polish.

**Input**: Design documents from `specs/001-unified-rental-search/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/search-api.md](./contracts/search-api.md), [quickstart.md](./quickstart.md)

**Tests**: INCLUDED — the plan/research/quickstart explicitly call for grouper/service unit tests, `@WebMvcTest` API-contract tests, and Angular Jasmine/Karma specs. Test tasks precede the implementation they cover within each phase.

**Organization**: Grouped by user story (US1–US4) so each is an independently testable increment.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on incomplete tasks)
- **[Story]**: US1 / US2 / US3 / US4 (Setup, Foundational, Polish carry no story label)

## Path Conventions (Web app — from plan.md)

- Backend: `java/flatflo/src/main/java/com/flatflow/`, tests `java/flatflo/src/test/java/com/flatflow/`, resources `java/flatflo/src/main/resources/`
- Frontend: `angular/src/app/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Wire up the existing backend + frontend scaffolds for this feature.

- [x] T001 [P] Add HTTP + CORS/dev-proxy wiring: create `angular/proxy.conf.json` mapping `/api` → `http://localhost:8080`, and reference it from the `serve` target in `angular/angular.json`
- [x] T002 [P] Register `provideHttpClient()` in `angular/src/app/app.config.ts`
- [x] T003 [P] Add the `/search` route in `angular/src/app/app.routes.ts` (lazy-load the search page component created in Phase 2/US1)
- [x] T004 [P] Confirm backend JSON/Jackson support and add a package skeleton: create empty packages `com.flatflow.grouping` and `com.flatflow.search` (with `dto`) under `java/flatflo/src/main/java/com/flatflow/`

**Checkpoint**: Both apps build; frontend can reach the backend origin via proxy.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The raw model extensions, seeded feed, grouping pipeline, and query/DTO scaffolding that EVERY user story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Raw model + feed

- [x] T005 [P] Extend the raw `Listing` record with `photos: List<String>`, `floor: Integer`, `lastUpdated: String`, `lat: double`, `lng: double` in `java/flatflo/src/main/java/com/flatflow/listing/Listing.java` (per data-model.md D5)
- [x] T006 [P] Create `SupportedArea` (id + display name; e.g., GOREGAON_EAST, MALAD_WEST, THANE) in `java/flatflo/src/main/java/com/flatflow/listing/SupportedArea.java`
- [x] T007 Author the seeded feed `java/flatflo/src/main/resources/listings-seed.json` exercising: same flat on ≥2 sources (identical dedup-key fields), a best-deal flat (cheapest ≥10% below area avg), a boundary flat (just <10%), a single-source flat, and a flat with empty/broken photos (per quickstart Prerequisites)
- [x] T008 [P] Define `ListingSource` interface (return raw `List<Listing>`) in `java/flatflo/src/main/java/com/flatflow/listing/ListingSource.java`
- [x] T009 Implement `SeededListingSource` loading `listings-seed.json` into memory at startup in `java/flatflo/src/main/java/com/flatflow/listing/SeededListingSource.java` (depends on T005, T007, T008)

### Grouping pipeline (dedup + area-average + best-deal)

- [x] T010 [P] Create `SourceOffer` type (sourcePlatform, sourceUrl, price, lastUpdated) in `java/flatflo/src/main/java/com/flatflow/grouping/SourceOffer.java`
- [x] T011 [P] Create `ListingGroup` type (groupId, title, locality, area, bhk, furnishing, floor, lat/lng, offers, cheapestPrice, areaAveragePrice, isBestDeal, bestDealDiscountPct, primaryPhoto, newestUpdated) in `java/flatflo/src/main/java/com/flatflow/grouping/ListingGroup.java`
- [x] T012 [P] Define `ListingGrouper` interface (`List<Listing>` → `List<ListingGroup>`) in `java/flatflo/src/main/java/com/flatflow/grouping/ListingGrouper.java`
- [x] T013 [P] Implement `AreaAverageCalculator` (mean rent per locality+BHK across the feed) in `java/flatflo/src/main/java/com/flatflow/grouping/AreaAverageCalculator.java`
- [x] T014 [P] Unit test the grouping pipeline (dedup key merges same flat; area-average correct; best-deal at exactly 10% boundary and just-below; discount % rounding) in `java/flatflo/src/test/java/com/flatflow/grouping/SeedListingGrouperTest.java`
- [x] T015 Implement `SeedListingGrouper` (dedup key over normalized locality+bhk+area-bucket+society/title; build offers sorted by price; compute cheapest, area avg via T013, 10% best-deal flag, primary photo) in `java/flatflo/src/main/java/com/flatflow/grouping/SeedListingGrouper.java` (depends on T010–T013; satisfies T014)

### Query + DTO scaffolding

- [x] T016 [P] Create `SortMode` enum (BEST_DEAL default, PRICE_ASC, PRICE_DESC, NEWEST) in `java/flatflo/src/main/java/com/flatflow/search/SortMode.java`
- [x] T017 [P] Create `SearchQuery` (location, budgetMin?, budgetMax?, bhk, furnishing?, sort, page) in `java/flatflo/src/main/java/com/flatflow/search/SearchQuery.java`
- [x] T018 [P] Create boundary DTOs `AreaDto`, `SourceOfferDto`, `SourceStatusDto` in `java/flatflo/src/main/java/com/flatflow/search/dto/`
- [x] T019 [P] Create `ListingGroupDto` (incl. `newestUpdated` so the NEWEST sort has a field to order by — FR-009) and `SearchResponseDto` (results, count, dupCount, sort, page, pageSize, hasMore, sources) in `java/flatflo/src/main/java/com/flatflow/search/dto/`

### Frontend foundation

- [x] T020 [P] Create typed models (`ListingGroup`, `SourceOffer`, `SearchResponse`, `Area`, `SortMode`) in `angular/src/app/search/models.ts` (mirror contracts/search-api.md)
- [x] T021 Create `SearchService` with `getAreas()` and `search(query)` calling `/api/areas` and `/api/search` in `angular/src/app/search/search.service.ts` (depends on T020)
- [x] T022 Create the `search-page` container shell (loads areas, holds query state, renders child slots) in `angular/src/app/search/search-page/` (depends on T021)

**Checkpoint**: Feed loads, groups compute (tests green), API/DTO types exist, and the frontend shell can call the backend. User stories can now begin.

---

## Phase 3: User Story 1 - Search rentals across platforms in one place (Priority: P1) 🎯 MVP

**Goal**: One search (location, budget range, BHK) returns de-duplicated grouped cards from multiple sources, with a dedup-aware meta line.

**Independent Test**: Submit location+budget+BHK; confirm grouped cards render, a multi-source flat appears once, and the meta line shows "{count} flats found, {dupCount} duplicates merged".

### Tests for User Story 1

- [x] T023 [P] [US1] Unit test `SearchService` filter-by-location/BHK + dupCount computation + pagination (~20 groups) in `java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java`
- [x] T024 [P] [US1] `@WebMvcTest` contract test for `GET /api/areas` and `GET /api/search` happy path (grouped shape, count/dupCount, pageSize, empty state) in `java/flatflo/src/test/java/com/flatflow/search/SearchControllerTest.java`

### Implementation for User Story 1

- [x] T025 [US1] Implement `SearchService` — take grouped listings from `ListingGrouper`, filter by location + BHK, paginate ~20 groups, compute `count` and `dupCount` in `java/flatflo/src/main/java/com/flatflow/search/SearchService.java` (depends on T015, T017; satisfies T023)
- [x] T026 [US1] Implement `SearchController` `GET /api/areas` (from `SupportedArea`) and `GET /api/search` (map groups → `SearchResponseDto`, include per-source status). Apply consistent unit formatting at the DTO boundary — INR price display (`priceDisplay`), sq-ft area in `metaLine`, and rent captioned "per month" (FR-019) — in `java/flatflo/src/main/java/com/flatflow/search/SearchController.java` (depends on T025, T018, T019; satisfies T024)
- [x] T027 [US1] Add input validation + error handling (400 on invalid location/bhk/budget, 200 empty state) via `@ControllerAdvice` in `java/flatflo/src/main/java/com/flatflow/search/` (per contracts)
- [x] T028 [P] [US1] Build the `filter-bar` component with location picker (from `/api/areas`), budget range, and BHK selector; emit query changes in `angular/src/app/search/filter-bar/`
- [x] T029 [P] [US1] Build the `results-meta` component ("{count} flats found, {dupCount} duplicates merged" + sort label) in `angular/src/app/search/results-meta/`
- [x] T030 [P] [US1] Build the `listing-card` component rendering grouped card: photo, title, meta line, cheapest price + "per month", and source chips row in `angular/src/app/search/listing-card/`
- [x] T031 [US1] Wire `search-page` to run a search on submit and render meta line + card list from `SearchService`; reflect query in URL params in `angular/src/app/search/search-page/` (depends on T022, T028, T029, T030)
- [x] T032 [P] [US1] Component spec for `listing-card` (single vs multi-source chip rendering) and `search-page` (renders results + meta) in `angular/src/app/search/` (Jasmine/Karma)

**Checkpoint**: US1 fully functional — unified, de-duplicated search with grouped cards and dedup meta line. This is the demoable MVP.

---

## Phase 4: User Story 2 - Redirect to the original listing (Priority: P1)

**Goal**: Clicking a card opens the cheapest source; clicking a specific chip opens that source — both in a new tab, preserving the results view.

**Independent Test**: Click a multi-source card body → cheapest source opens; click a non-cheapest chip → that source opens.

### Tests for User Story 2

- [ ] T033 [P] [US2] Component spec: card-body click targets `sources[0].sourceUrl`; chip click targets that chip's `sourceUrl`; both open in a new tab in `angular/src/app/search/listing-card/`

### Implementation for User Story 2

- [ ] T034 [US2] Make the whole `listing-card` a keyboard-focusable click target that opens the cheapest source's `sourceUrl` in a new tab (results tab preserved) in `angular/src/app/search/listing-card/` (satisfies part of T033)
- [ ] T035 [US2] Make each source chip an independent click target/link opening its own `sourceUrl`, overriding the card default in `angular/src/app/search/listing-card/` (satisfies part of T033)
- [ ] T036 [P] [US2] Add the "availability not guaranteed" affordance/notice near results/cards in `angular/src/app/search/listing-card/` or `search-page/` (FR-020)

**Checkpoint**: US1 + US2 work — tenants can find and act on a listing via card or specific source.

---

## Phase 5: User Story 3 - Spot the best deal (Priority: P2)

**Goal**: Best-deal cards are visually flagged (badge + "{pct}% below area average" + 2px accent border) only when cheapest ≥10% below the locality+BHK average; default sort surfaces them first.

**Independent Test**: The seeded best-deal flat shows the badge + correct % + accent border; the boundary flat shows none; default sort orders best deals first.

**Note**: The best-deal *computation* already lands in Foundational (T013/T015). This phase is the presentation + default-sort surfacing.

### Tests for User Story 3

- [ ] T037 [P] [US3] Extend `SearchServiceTest` with BEST_DEAL sort ordering (best deals first, then discount desc / price asc) in `java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java`
- [ ] T038 [P] [US3] Component spec: best-deal badge + subtext + accent border render iff `isBestDeal`; hidden for boundary group in `angular/src/app/search/listing-card/`

### Implementation for User Story 3

- [x] T039 [US3] Implement BEST_DEAL as the default sort in `SearchService` (best deals first, tiebreak by discount desc then price asc) in `java/flatflo/src/main/java/com/flatflow/search/SearchService.java` (satisfies T037)
- [ ] T040 [US3] Render the best-deal badge, "{pct}% below area average" subtext, and 2px accent border in `listing-card` when `isBestDeal` is true (and nothing otherwise) in `angular/src/app/search/listing-card/` (satisfies T038)

**Checkpoint**: US1–US3 work — the differentiation (best-deal insight) is visible and drives default ordering.

---

## Phase 6: User Story 4 - Refine and sort results (Priority: P2)

**Goal**: Sticky filter bar with budget range + BHK + furnishing filters, active-filter count badge, and switchable sort modes; state reflected in the URL.

**Independent Test**: Change a filter → set updates without full reload + URL updates + Filters badge count; switch sort → order changes.

### Tests for User Story 4

- [x] T041 [P] [US4] Extend `SearchServiceTest` with budget-range filter, furnishing filter, and PRICE_ASC/PRICE_DESC/NEWEST sort ordering in `java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java`
- [x] T042 [P] [US4] `@WebMvcTest` cases for `sort` param (all modes) and budget-range/furnishing filters, incl. 400 on `budgetMin > budgetMax` in `java/flatflo/src/test/java/com/flatflow/search/SearchControllerTest.java`

### Implementation for User Story 4

- [x] T043 [US4] Add budget-range + furnishing filtering and PRICE_ASC/PRICE_DESC/NEWEST sorting to `SearchService` in `java/flatflo/src/main/java/com/flatflow/search/SearchService.java` (satisfies T041)
- [x] T044 [US4] Accept/validate `budgetMin`, `budgetMax`, `furnishing`, `sort` params in `SearchController` (400 on invalid combos) in `java/flatflo/src/main/java/com/flatflow/search/SearchController.java` (satisfies T042)
- [ ] T045 [US4] Make the filter bar sticky; add furnishing selector, a "Filters" control showing active non-default filter count as a badge, and a "Map" toggle entry point in `angular/src/app/search/filter-bar/` (FR-010, FR-014) — **PARTIAL: sticky + furnishing selector + Filters badge done; Map toggle deferred**
- [x] T046 [US4] Add a sort control to `results-meta` and wire sort/filter changes to re-query without full reload, syncing all query params to the URL in `angular/src/app/search/search-page/` (depends on T045)
- [x] T047 [P] [US4] Component spec: active-filter count badge, clear-filters restores set, sort switch re-queries, URL params update in `angular/src/app/search/`

**Checkpoint**: All four user stories independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Robustness, states, accessibility, and validation that span stories.

- [ ] T048 [P] Implement UI states in `angular/src/app/search/ui-states/`: skeleton/loading (3–5 cards, filter bar not blocked), empty ("No flats match these filters" + widen suggestion), and full-width error + retry (all sources down) — FR-015, FR-016, FR-017
- [ ] T049 [P] Add per-source status handling: surface "Results from N of M sources" in `results-meta` on partial failure; treat `503 ALL_SOURCES_UNAVAILABLE` as the error state in `angular/src/app/search/search-page/` (FR-017)
- [ ] T050 [P] Image handling in `listing-card`: native lazy-load + `onerror` fallback to a category-icon placeholder (never broken-image) in `angular/src/app/search/listing-card/` (FR-018) — **PARTIAL: native lazy-load + null-URL placeholder done; `onerror` fallback for broken loads not yet**
- [ ] T051 [P] Accessibility pass: card focus ring + keyboard activation, per-chip focus stop with accessible label ("View this listing on {source}, ₹{price} per month"), best-deal conveyed via text in `angular/src/app/search/listing-card/` (FR-022)
- [ ] T052 [P] Apply reference copy (sentence case; no exclamation/"successfully"/"please") across meta line, badge/subtext, chip prefix, empty state, price caption in `angular/src/app/search/` (FR-023)
- [ ] T053 [P] Mobile: collapse filter-bar fields into a single "Edit search" summary opening a full-screen sheet in `angular/src/app/search/filter-bar/` (UI spec §4)
- [ ] T054 Add the ~3s-first-page / ~8s-source-drop timeout + partial-source seam in the aggregation path (backend) with a unit test asserting a slow/failed source is dropped and reported in `java/flatflo/src/main/java/com/flatflow/search/` + test (FR-021)
- [ ] T055 Run `quickstart.md` V1–V12 end-to-end against the seeded feed and fix any gaps
- [ ] T056 [P] Verify swap seams: a unit test injecting an alternate `ListingSource` and an alternate `ListingGrouper` proves no controller/DTO change is required (FR-002/FR-003) in `java/flatflo/src/test/java/com/flatflow/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies — start immediately.
- **Foundational (Phase 2)**: depends on Setup — **BLOCKS all user stories**.
- **User Stories (Phases 3–6)**: all depend on Foundational.
  - US1 (P1) → US2 (P1) → US3 (P2) → US4 (P2) in priority order, or in parallel by different developers after Foundational.
- **Polish (Phase 7)**: depends on the user stories it touches (states/a11y assume US1 cards exist).

### User Story Dependencies

- **US1**: after Foundational. No dependency on other stories.
- **US2**: after Foundational; builds on the US1 `listing-card` (chips/redirect), but redirect is independently testable.
- **US3**: after Foundational (best-deal computation already there); presentation independent of US2.
- **US4**: after Foundational; extends the US1 filter bar + search service. Independently testable.

### Within Each Story

- Tests are written before the implementation they cover and should fail first.
- Backend: model → grouper → service → controller. Frontend: models/service → components → wiring.

### Parallel Opportunities

- Setup: T001–T004 all [P].
- Foundational: T005/T006/T008 [P]; T010–T013 [P]; T014 [P]; T016–T020 [P] (distinct files). T007→T009, T013/T010–T012→T015, T020→T021→T022 are sequential.
- US1: T023/T024 [P]; frontend T028/T029/T030 [P]; then T031 wires them.
- Across stories: after Foundational, US1–US4 can be staffed in parallel.
- Polish: T048–T053, T056 largely [P].

---

## Parallel Example: User Story 1

```text
# Tests first (parallel):
Task: "Unit test SearchService in java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java"   # T023
Task: "@WebMvcTest for /api/areas + /api/search in .../SearchControllerTest.java"                          # T024

# Frontend components (parallel):
Task: "filter-bar component in angular/src/app/search/filter-bar/"      # T028
Task: "results-meta component in angular/src/app/search/results-meta/"  # T029
Task: "listing-card component in angular/src/app/search/listing-card/"  # T030
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1 Setup → 2. Phase 2 Foundational (critical) → 3. Phase 3 US1 → **STOP & VALIDATE** (quickstart V1–V2) → demo the unified de-duplicated search.

### Incremental Delivery

Setup + Foundational → US1 (MVP: search + grouping) → US2 (redirect) → US3 (best deal) → US4 (filters + sort) → Polish (states, a11y, images, performance). Each story is a testable increment that doesn't break prior ones.

### Parallel Team Strategy

After Foundational: Dev A → US1, Dev B → US2 (coordinating on `listing-card`), Dev C → US3, Dev D → US4. Polish tasks fan out at the end.

---

## Notes

- Tests are included per the design intent (plan/research/quickstart). Verify they fail before implementing.
- Best-deal computation lives in Foundational (T013/T015); US3 is its presentation + default-sort surfacing.
- `listing-card` is touched by US1 (base), US2 (redirect), US3 (badge), and Polish (images/a11y) — sequence those edits to avoid conflicts, or assign the card to one owner.
- Commit after each task or logical group; stop at any checkpoint to validate a story independently.
