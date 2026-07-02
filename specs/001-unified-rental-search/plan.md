# Implementation Plan: Unified Rental Search (FlatFlo MVP)

**Branch**: `flatflo-0000` | **Date**: 2026-07-03 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-unified-rental-search/spec.md` (expanded 2026-07-03 to adopt the attached UI spec: duplicate grouping, best-deal smart layer, map toggle, best-deal sort, budget range).

## Summary

Deliver the expanded FlatFlo MVP: a single search (location, budget range, BHK) that aggregates listings from multiple source platforms, **merges duplicates into `ListingGroup` cards**, surfaces the **cheapest source** and a **"best deal" signal** (cheapest ≥10% below the locality+BHK area average), and lets the tenant deep-link to any source. Results render as a single-column card list with a sticky filter bar, a dedup-aware meta line, multiple sort modes (best-deal default), and a map toggle entry point.

Backend is the existing **Spring Boot 4 / Java 21** app: it loads the seeded feed behind a `ListingSource` interface, runs a **grouping pipeline** (dedup → area-average → best-deal flag) behind a `ListingGrouper` interface, then filters/sorts/paginates `ListingGroup`s and exposes them via a small REST API. Frontend is the existing **Angular 20** app: sticky filter bar, grouped result cards with source chips, best-deal badge, sort control, states (loading/empty/partial/error), image fallbacks, and accessibility per the UI spec.

## Technical Context

**Language/Version**: Java 21 (backend, Gradle toolchain); TypeScript 5.x on Angular 20 (frontend)

**Primary Dependencies**: Backend — Spring Boot 4.1.0 (`spring-boot-starter-webmvc`), Lombok, Jackson (JSON feed parsing). Frontend — Angular 20 (`@angular/router`, `@angular/forms` reactive forms, `@angular/common/http`), RxJS 7.

**Storage**: None (no database). The seeded feed is a bundled JSON fixture loaded into memory. Grouping and area-average are computed in memory over that feed. The `ListingSource` + `ListingGrouper` seams allow a DB- or agent-backed implementation later.

**Testing**: Backend — JUnit 5, `@WebMvcTest` (API contract), plain unit tests for the grouper (dedup/area-average/best-deal) and the search service (filter/sort/paginate). Frontend — Jasmine/Karma component + service specs, incl. card states, best-deal badge threshold, source-chip rendering, and image fallback.

**Target Platform**: Backend = JVM web service (localhost:8080 dev). Frontend = browser SPA (ng serve on :4200) calling the backend over HTTP/JSON via a dev proxy.

**Project Type**: Web application (separate backend `java/flatflo` + frontend `angular/`, both scaffolded).

**Performance Goals**: First page within ~3s (FR-021); source not responding within ~8s is dropped and its absence reflected (FR-017/FR-021). Trivially met over the in-memory seed; the timeout/partial-source seam is built into the aggregation layer so it holds when real sources are added.

**Constraints**: Prices INR, area sq ft, rent monthly ("per month", FR-019). Raw-listing identity = source + original URL/ID. Best-deal threshold = cheapest ≥10% below locality+BHK area average (FR-007). Page size ~20 groups (FR-006). Sort modes: best-deal (default), price↑, price↓, newest (FR-009). Filter/sort state in URL query params (FR-010). Anonymous (no auth). Map screen deferred (toggle entry point only, FR-014).

**Scale/Scope**: Mumbai-first, ~3 source platforms as labels, seeded dataset ~tens–low-hundreds of raw listings grouping into fewer cards. 1 results screen (filter bar + meta line + card list) + search entry. Backend endpoints: `/api/areas`, `/api/search` (grouped, filtered, sorted, paginated).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution at `.specify/memory/constitution.md` is an **unfilled template** (placeholder tokens, no ratified principles). No enforceable governance gates exist.

Applied defaults (industry-standard, in place of a ratified constitution):
- **Simplicity / YAGNI**: Still no database, no auth, no broker. The scope expansion adds *computation* (grouping, area-average, best-deal) over the same in-memory feed, not new infrastructure. PASS.
- **Separation of concerns**: `ListingSource` (raw feed) → `ListingGrouper` (dedup + area-average + best-deal) → `SearchService` (filter/sort/paginate groups) → REST controller → DTOs at the boundary. Each layer independently testable. PASS.
- **Testability**: Grouper and search service are pure, stateless, unit-testable; API covered by `@WebMvcTest`. PASS.
- **Swappability**: Feed behind `ListingSource` (seed → agent later); grouping behind `ListingGrouper` (naive seed grouping now → smarter algorithm later) — neither swap changes the controller or UI (FR-002, FR-003). PASS.

**Gate result: PASS** (no violations; Complexity Tracking left empty). The added grouping/best-deal logic is inherent to the (now expanded) requirements, not incidental complexity.

**Post-Phase-1 re-check**: The design added a `grouping` package (`ListingGrouper`, `SeedListingGrouper`, `AreaAverageCalculator`, `ListingGroup`, `SourceOffer`), boundary DTOs, and a `SortMode` enum — all required by the expanded spec. No database, auth, broker, or extra project introduced; the two swap seams keep feed + grouping replaceable. No new violations. **Re-check result: PASS.**

> Recommendation (non-blocking): run `/speckit-constitution` to ratify real principles before the codebase grows.

**Agent context**: No agent-context update script and no `CLAUDE.md` in this Spec Kit install, so the agent-context update step is a no-op. Run `/init` if a `CLAUDE.md` is desired.

## Project Structure

### Documentation (this feature)

```text
specs/001-unified-rental-search/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output — regenerated for expanded scope
├── data-model.md        # Phase 1 output — regenerated (ListingGroup model)
├── quickstart.md        # Phase 1 output — regenerated (grouping/best-deal validation)
├── contracts/
│   └── search-api.md    # Phase 1 output — regenerated (grouped response shape)
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

Backend — organized by domain, extending the existing `com.flatflow.listing` package and adding grouping + search:

```text
java/flatflo/src/main/java/com/flatflow/
├── FlatfloApplication.java              # existing entry point
├── listing/                             # existing domain model (raw Listing + enums)
│   ├── Listing.java + BhkType/AreaType/Furnishing/ListingStatus.java  # existing
│   ├── ListingSource.java               # NEW: interface over the raw feed
│   ├── SeededListingSource.java         # NEW: loads bundled listings-seed.json
│   └── SupportedArea.java               # NEW: predefined searchable areas
├── grouping/                            # NEW: dedup + insight pipeline
│   ├── ListingGroup.java                # NEW: grouped flat (sources, cheapest, area avg, best-deal)
│   ├── SourceOffer.java                 # NEW: one source's offer within a group
│   ├── ListingGrouper.java              # NEW: interface (List<Listing> -> List<ListingGroup>)
│   ├── SeedListingGrouper.java          # NEW: MVP grouping over the seed (dedup key + area-average + 10% rule)
│   └── AreaAverageCalculator.java       # NEW: locality+BHK average rent
└── search/                              # NEW: query handling + web layer
    ├── SearchController.java            # NEW: /api/areas, /api/search
    ├── SearchService.java               # NEW: filter (budget range, BHK, furnishing) + sort + paginate groups
    ├── SearchQuery.java                 # NEW: location, budgetMin/Max, bhk, furnishing?, sort, page
    ├── SortMode.java                    # NEW: BEST_DEAL(default)/PRICE_ASC/PRICE_DESC/NEWEST
    └── dto/
        ├── ListingGroupDto.java         # NEW: group shape for the UI (title, price, badge, chips…)
        ├── SourceOfferDto.java          # NEW: chip data (source name, price, url, label)
        ├── SearchResponseDto.java       # NEW: groups + meta (count, dupCount, sort) + source status + paging
        └── AreaDto.java                 # NEW: supported-area option

java/flatflo/src/main/resources/
└── listings-seed.json                   # NEW: seeded feed (dupes across sources; some best deals; ≥2 sources)

java/flatflo/src/test/java/com/flatflow/
├── grouping/SeedListingGrouperTest.java # NEW: dedup, area-average, 10% best-deal threshold
├── search/SearchServiceTest.java        # NEW: filter/sort(4 modes)/paginate, dup-count meta
└── search/SearchControllerTest.java     # NEW: @WebMvcTest API contract
```

Frontend — Angular 20 standalone components + typed HTTP service:

```text
angular/src/app/
├── app.routes.ts                        # existing (add /search route)
├── app.config.ts                        # existing (add provideHttpClient)
└── search/                              # NEW feature area
    ├── search.service.ts                # NEW: calls /api/areas + /api/search; typed models
    ├── models.ts                        # NEW: ListingGroup / SourceOffer / SearchResponse / SortMode types
    ├── search-page/                     # NEW: container — orchestrates filter bar, meta line, list, states, paging
    ├── filter-bar/                      # NEW: sticky bar (location, budget range, BHK, furnishing, Filters count, Map toggle)
    ├── results-meta/                    # NEW: "N flats found, M duplicates merged" + sort control
    ├── listing-card/                    # NEW: grouped card — photo(+fallback), best-deal badge, price, source chips, redirect
    └── ui-states/                       # NEW: skeleton, empty, partial-source note, full error+retry
```

**Structure Decision**: Web application reusing both existing scaffolds. Backend gains two new domain packages — `grouping` (the dedup + best-deal pipeline behind `ListingGrouper`) and `search` (query + web) — on top of the existing `listing` model. Two seams (`ListingSource`, `ListingGrouper`) keep both the data feed and the grouping algorithm swappable without touching the API or UI (FR-002, FR-003). Frontend mirrors the UI spec's components (filter bar, meta line, grouped card, state views).

## Complexity Tracking

> No constitution violations. Section intentionally empty. (Grouping/best-deal logic is required by the expanded spec, not incidental complexity.)
