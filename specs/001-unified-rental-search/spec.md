# Feature Specification: Unified Rental Search (FlatFlo MVP)

**Feature Branch**: `flatflo-0000`

**Created**: 2026-07-03

**Status**: Draft

**Input**: User description: "refer the readme for an idea on what i am working on and lets start"; plus attached UI specification `flatflo-results-card-spec.md` (search results card + list layout).

## Overview

FlatFlo is a rental meta-search engine. This feature is the MVP: a single search that aggregates rental listings from multiple external platforms (e.g., MagicBricks, NoBroker, Housing.com), **merges duplicate flats into one card**, surfaces the **cheapest source and a "best deal" signal**, and redirects the user to the original platform to proceed. FlatFlo does not host listings or handle bookings — it is a discovery and comparison layer.

For the MVP, listings come from a **seeded (manually created) dummy data feed** so the concept can be demonstrated end to end without depending on external platforms. The eventual real source is an **AI-agent-driven aggregation layer**: a delegated agent ("Claude search") retrieves listings from source platforms and returns them in a structured (JSON) format, which FlatFlo ingests and displays. Manual seeding is a temporary stand-in and will be replaced by this authoritative ("golden") source in a later phase.

The results view (aggregated listings, post-search / pre-redirect) is defined in detail by the attached UI specification and captured in the **Results UI** section below. It is the core screen of the MVP — where the meta-search value is proven.

**In scope for this MVP** (expanded to match the attached UI spec): unified search, duplicate grouping into single cards, cheapest-source surfacing, a "best deal" badge (cheapest ≥10% below the locality+BHK area average), source comparison chips, multiple sort modes, a map toggle entry point, and redirect to source.

**Deferred to later phases** (out of scope here): the map view screen itself (only the toggle entry point is in scope; synced list-map hover is later), a listing detail/intermediate page, area insight/rent-trend charts, and saved searches/alerts. The duplicate-detection *algorithm* is a backend concern; this feature assumes the search API returns already-grouped results.

## Clarifications

### Session 2026-07-03

- Q: How is a listing uniquely identified? → A: Source platform + original listing URL/ID
- Q: How does the tenant enter location? → A: Autocomplete/picker from a predefined list of supported areas
- Q: How are results sized/paged? → A: Paginated, ~20 groups per page; tenant loads more pages

### Session 2026-07-03 (UI spec integration — MVP scope expanded)

- Q: How should the attached UI spec be folded into the spec, given it re-introduces deferred features? → A: Adopt the UI spec as-is; expand the MVP to include duplicate grouping, the "best deal" smart layer, the map toggle, best-deal sort, and budget range.
- Decision: Results are rendered as **grouped cards** (`ListingGroup`), one per de-duplicated flat, with source-comparison chips. (Supersedes the earlier "show listings as-is, no dedup" decision.)
- Decision: **Default sort is "best deal"**, with additional modes: price low–high, price high–low, newest. (Supersedes the earlier "price ascending" default.)
- Decision: **Budget is a range (min + max)**. (Supersedes the earlier "optional max-only cap".)
- Decision: A **"best deal" badge** is shown only when the cheapest source is at least **10% below** the area-average rent for that locality + BHK.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Search rentals across platforms in one place (Priority: P1)

A tenant looking for a flat selects a location from the supported-areas list, sets a budget range, and picks a BHK (number of bedrooms). FlatFlo returns rental listings drawn from multiple external platforms, **merged so each physical flat appears once**, in a single scannable list. This replaces the need to open and search 3–5 apps separately.

**Why this priority**: This is the core value proposition and the reason the product exists. Without unified, de-duplicated search there is no product.

**Independent Test**: Enter a valid location, budget range, and BHK; confirm results appear as grouped cards, a flat listed on multiple sources appears once with its sources compared, and the results meta line reports the duplicate-merge count.

**Acceptance Scenarios**:

1. **Given** a tenant on the search screen, **When** they select "Goregaon East", budget "₹20,000–₹40,000", and "2 BHK" and submit, **Then** a list of grouped rental cards is displayed, drawn from multiple source platforms.
2. **Given** a search that matches no listings, **When** results are returned, **Then** the tenant sees the empty state ("No flats match these filters" + a suggestion to widen budget or BHK).
3. **Given** the same flat is listed on three platforms, **When** results are displayed, **Then** it appears as one card whose price is the cheapest source's rent and whose other sources are shown as comparison chips.
4. **Given** a search returns results, **When** the meta line renders, **Then** it shows the group count and the number of duplicates merged (e.g., "142 flats found, 38 duplicates merged").

---

### User Story 2 - Redirect to the original listing (Priority: P1)

Having found a listing they like, the tenant clicks the card (or a specific source chip) and is taken directly to that listing on the chosen source platform, so they can see full details and contact the lister.

**Why this priority**: Aggregation is only useful if the tenant can act on a result. Redirection is how the tenant completes their journey. Co-critical with search.

**Independent Test**: Click a card and confirm it opens the cheapest source's listing in a new tab; click a specific source chip and confirm it opens that source's listing instead.

**Acceptance Scenarios**:

1. **Given** a grouped card, **When** the tenant clicks the card body, **Then** the cheapest source's original listing opens in a new tab and the FlatFlo results tab is preserved.
2. **Given** a grouped card with multiple sources, **When** the tenant clicks a specific source chip, **Then** that source's original listing opens (overriding the cheapest-source default).
3. **Given** a listing whose original link is no longer reachable, **When** the tenant opens it, **Then** availability is not guaranteed and the tenant may reach a removed page (communicated as a limitation).

---

### User Story 3 - Spot the best deal (Priority: P2)

The tenant can immediately see which flats are priced notably below the going rate for that area and size, so they can prioritize good-value options.

**Why this priority**: This is FlatFlo's differentiation — turning aggregation into insight. It rides on top of core search but is a primary reason a tenant would prefer FlatFlo.

**Independent Test**: With seeded data where one flat's cheapest source is ≥10% below the locality+BHK average, confirm that card shows the "best deal" badge with the correct discount percentage and an accent border, and cards below the threshold show no badge.

**Acceptance Scenarios**:

1. **Given** a flat whose cheapest price is 12% below its locality+BHK area average, **When** its card renders, **Then** it shows a "Best deal" badge with subtext "12% below area average" and a 2px accent border.
2. **Given** a flat whose cheapest price is 5% below the area average, **When** its card renders, **Then** no best-deal badge or accent border is shown.
3. **Given** the default sort, **When** results load, **Then** they are ordered by best deal.

---

### User Story 4 - Refine and sort results (Priority: P2)

The tenant narrows results using filters (budget range, BHK, furnishing) via a sticky filter bar, and changes the sort mode, to focus on the listings that fit their needs.

**Why this priority**: Improves usability and relevance on top of core search. The product is viable with just Story 1 but far more usable with filtering and sorting.

**Independent Test**: Change a filter and confirm the result set updates and the URL reflects the new state; change the sort mode and confirm ordering changes.

**Acceptance Scenarios**:

1. **Given** a result set, **When** the tenant sets furnishing to "Furnished", **Then** only furnished groups remain and a new search runs without a full page reload.
2. **Given** active non-default filters, **When** any are applied, **Then** the "Filters" button shows the active filter count as a badge; clearing them removes the badge and restores the full set.
3. **Given** a result set, **When** the tenant switches sort to "price low–high", **Then** groups reorder by cheapest price ascending.
4. **Given** a filter change, **When** it is applied, **Then** the URL query params update so the search is shareable/bookmarkable.

---

### Edge Cases

- **All sources fail**: When no source can be reached, the tenant sees a full-width error state with a retry action — not an empty list implying no listings exist.
- **Partial source failure**: When at least one source fails but others succeed, results from the succeeding sources still render; the meta line may note "Results from 2 of 3 sources" when the gap is significant.
- **Slow source**: Results from faster sources appear promptly; a source not responding within the time bound is dropped and reported rather than blocking the page.
- **Stale listing**: A listing may have been rented/removed at the source; on redirect the tenant may reach a dead page. Availability is not guaranteed and this is communicated.
- **Missing / broken photo**: A card photo that is missing or fails to load falls back to a category-icon placeholder — never a broken-image glyph.
- **Missing fields**: A card renders gracefully (placeholders) when optional fields are absent.
- **Single-source group**: A flat found on only one source renders a single plain source chip (name only, no repeated price) and no "Also listed on" comparison row.
- **Too many best deals**: If more than ~1 card per screen qualifies as a best deal, the discount threshold is tightened rather than letting the accent lose meaning.
- **Ambiguous location**: Not applicable — location is chosen from a predefined area list, so the selected area is always unambiguous.
- **Empty seed feed**: If the seeded feed has no listings for the search, the empty state is shown rather than an error.

## Requirements *(mandatory)*

### Functional Requirements

#### Search & aggregation

- **FR-001**: System MUST let a tenant search rentals by location, budget range, and BHK. Location MUST be selected via autocomplete/picker from a predefined list of supported areas. Budget MUST be a range with a lower and upper bound.
- **FR-002**: System MUST aggregate matching listings from multiple source platforms. For the MVP, listings originate from a seeded (manually created) data feed; the system MUST be structured so this feed can later be replaced by an AI-agent-driven source returning structured JSON, without changing the tenant-facing search experience.
- **FR-003**: System MUST return results as **grouped listings** — each physical flat that appears on multiple sources is merged into a single group. The search result the UI consumes is a set of groups, not raw listings. (The grouping/duplicate-detection algorithm is a backend concern; the feature only requires the API to deliver already-grouped results.)
- **FR-004**: For each group, System MUST identify the cheapest source and expose all sources for that flat, each with its own price and deep link, sorted ascending by price.
- **FR-005**: System MUST let a tenant open a group on its source platform: clicking the card opens the cheapest source's original listing; clicking a specific source chip opens that source's listing. Redirects open in a new tab and preserve the FlatFlo results view.
- **FR-006**: System MUST paginate results in pages of approximately 20 groups and MUST let the tenant load subsequent pages, with ordering applied consistently across pages.

#### Best deal (smart layer)

- **FR-007**: System MUST compute, per group, the area-average rent for that group's locality + BHK, and MUST flag a group as a "best deal" only when its cheapest price is at least **10%** below that area average.
- **FR-008**: System MUST show the best-deal signal on a group's card as a badge plus a one-line reason ("{n}% below area average") and a distinct 2px accent border, and MUST show no such badge/border for groups below the threshold.

#### Sorting

- **FR-009**: System MUST support sort modes: **best deal (default)**, price low–high, price high–low, and newest, and MUST reflect the active sort in the results meta line. Ordering MUST be consistent across pages.

#### Results UI (see Results UI section for full detail)

- **FR-010**: System MUST present a sticky filter bar (location, budget, BHK, furnishing, a "Filters" control showing active filter count, and a "Map" toggle entry point) that remains visible on scroll; editing any field triggers a new search without a full page reload and updates the URL query params.
- **FR-011**: System MUST present a results meta line showing the group count, the duplicates-merged count (`sum(sources per group) − group count`), and the current sort mode.
- **FR-012**: System MUST render each group as a card showing a photo (with placeholder fallback), title, a single metadata line (area · furnishing · floor), the cheapest price rendered prominently with a "per month" caption, and — when the group has more than one source — an "Also listed on" row of equal-weight source chips each showing source name + that source's price.
- **FR-013**: System MUST provide filters for budget range, BHK, and furnishing, MUST show an active-filter count, and MUST let the tenant clear applied filters.
- **FR-014**: System MUST provide a "Map" toggle entry point in the filter bar. (The map view screen itself is deferred; the toggle is the in-scope entry point.)

#### States, robustness, and presentation

- **FR-015**: System MUST show skeleton/loading placeholders matching the card layout while results load, without blocking the filter bar.
- **FR-016**: System MUST present the empty state ("No flats match these filters" + a suggestion to widen budget or BHK) when a search or filter combination yields no groups.
- **FR-017**: System MUST render results from succeeding sources when some sources fail (partial failure), optionally noting reduced source coverage in the meta line, and MUST show a full-width error state with retry when all sources fail.
- **FR-018**: System MUST fall back to a category-icon placeholder for missing or failed card photos (never a broken-image glyph) and MUST lazy-load below-the-fold photos.
- **FR-019**: System MUST present prices in INR and areas in a consistent unit (sq ft) so listings are directly comparable, and MUST always caption price as "per month".
- **FR-020**: System MUST communicate that listing availability is not guaranteed live and that a redirected listing may no longer be available.
- **FR-021**: System MUST show first results promptly and MUST NOT block on a slow source: the first page MUST appear within approximately 3 seconds, and any source not responding within approximately 8 seconds MUST be dropped (and its absence reflected) rather than delaying results.

#### Accessibility & copy

- **FR-022**: System MUST make cards keyboard-navigable with a visible focus ring, MUST give each source chip its own focus stop and an accessible label (e.g., "View this listing on NoBroker, ₹24,000 per month"), and MUST NOT rely on color/icon alone to convey source or best-deal status.
- **FR-023**: System MUST use the reference copy (sentence case, no exclamation marks, no "successfully"/"please" filler) for the meta line, best-deal badge and subtext, source-chip prefix, empty state, and price caption.

### Key Entities *(include if feature involves data)*

- **Listing (raw, pre-grouping)**: A single rental advertisement from one source platform. Attributes: source id/name, deep-link URL, title, locality, monthly price (INR), area (sq ft), BHK, furnishing, optional floor, photos (source-hosted, may be empty/fail), last-updated date, and coordinates. **Identity**: uniquely identified by source platform + original listing URL/ID.
- **ListingGroup**: A set of raw listings judged to be the same physical flat, merged into one — this is what the UI renders. Attributes: canonical title, locality, area, BHK, furnishing, optional floor, coordinates, the list of source offers (sorted ascending by price), cheapest price, area-average price (for the locality+BHK), a best-deal flag and (when set) discount percentage, and a primary photo (from the cheapest source or first available).
- **Source Offer**: One source's offer within a group. Attributes: source id/name, deep-link URL, price, last-updated date.
- **Search Query**: The tenant's request. Attributes: location (from the supported-areas list), budget range (min + max), BHK, optional furnishing filter, sort mode, and page. Reflected in the URL query params.
- **Supported Area**: A predefined location a tenant can search within (e.g., Goregaon East, Malad West, Thane); backs the location picker and constrains valid searches.
- **Source Platform**: An external rental site a listing originated from (e.g., MagicBricks, NoBroker, Housing.com); provides the badge/name and deep-link capability. In the MVP, attached to seeded data.
- **Data Feed**: The origin of listings. In the MVP a seeded (manual) feed; later an AI-agent-driven JSON source. Both map to the same raw-Listing shape so the experience is unchanged.

## Results UI (Search Results Card & List)

Detailed presentation spec for the aggregated results view (post-search, pre-redirect), transcribed from the attached `flatflo-results-card-spec.md`. This is the core MVP screen.

### Layout structure
Single-column card list:
1. **Sticky filter bar** (top, persists on scroll)
2. **Results meta line** (count + dedup note + sort mode)
3. **ListingGroup cards** (one per de-duplicated flat), then pagination

No map split in this view (the map screen is a separate feature; only the toggle entry point lives here).

### Sticky filter bar
Left → right: location chip (opens location picker) · budget range (opens range input) · BHK selector · furnishing selector · divider · "Filters" button (shows active non-default filter count as a badge, e.g. `Filters 3`; no badge at 0) · "Map" toggle (right-aligned).
- Editing any field triggers a new search without full page reload and updates URL query params (shareable/bookmarkable).
- On mobile, individual fields collapse into a single "Edit search" summary button that opens a full-screen sheet.

### Results meta line
Plain text row below the filter bar, e.g. `142 flats found, 38 duplicates merged` (left) and `Sorted by best deal` (right).
- Left: total group count + duplicate-merge count = `sum(sources.length across groups) − group count`. This line exists to make aggregation visible — do not cut it for space.
- Right: current sort mode (best deal default; price low–high; price high–low; newest).

### Listing card (one per ListingGroup)
Desktop (≥768px): photo (≈160×140, object-fit cover) on the left; on the right — best-deal badge row (only if best deal), title + prominent right-aligned price, metadata line, and an "Also listed on" chip row.
- **Photo**: primary photo; on missing/failed load, fall back to a category-icon placeholder (never a broken-image glyph).
- **Best-deal badge**: rendered only when the group is a best deal; small pill + one-line reason ("{pct}% below area average"). This is the **only** badge type — no "hot/new/trending". Badge scarcity is the point.
- **Title / metadata**: canonical title, then a single line `{area} sq ft · {furnishing} · floor {n}`.
- **Price**: largest weight on the card, right-aligned, from the cheapest source; always caption "per month".
- **Source chips**: rendered only when a group has >1 source, prefixed "Also listed on"; one chip per additional source showing source name + that source's price. Chips are equal visual weight (no greying/shrinking of non-cheapest) — transparent comparison, not steering. A single-source group shows one plain chip (name only).
- **Best-deal visual distinction**: a 2px accent **border** (not a background color change) — the only card allowed to break the default border weight. If >~1 best-deal card appears per screen, tighten the discount threshold.

### Card interaction
- Whole card is clickable → redirects to the cheapest source's URL by default (new tab; FlatFlo tab preserved).
- Each source chip is independently clickable → redirects to that specific source's URL, overriding the default.

### States
| State | Behavior |
|---|---|
| Loading | 3–5 skeleton cards matching card layout (shimmer optional); do not block the filter bar. |
| Empty results | No cards. "No flats match these filters" + suggestion to widen budget or BHK (an invitation to adjust, not an apology). |
| Partial source failure | Render results from succeeding sources; optionally note "Results from N of M sources" in the meta line when significant. |
| Error (all sources down) | Full-width error state with a retry action. |

### Image handling
- Lazy-load below-the-fold photos.
- Each photo has an onerror fallback to the category-icon placeholder — never a broken-image icon.
- If a source's photos fail broadly (hotlink/referrer/signed-URL expiry), treat per-card as "missing photo"; do not message the specific cause.
- Grouped cards: photos across sources may differ; the primary photo (from the cheapest source) is best-effort, no guarantee of visual consistency across chips.

### Accessibility
- Cards are keyboard-navigable (focusable, visible focus ring) since the whole card is a click target.
- Each source chip has its own focus stop and an accessible label, e.g. "View this listing on NoBroker, ₹24,000 per month" — not color/icon alone.
- Best-deal status is conveyed by text ("{pct}% below area average"), not color/icon alone.
- Title + metadata line must read sensibly in sequence for screen readers (price/photo are not the only distinguishers).

### Copy reference
Sentence case throughout; no exclamation marks; no "successfully"/"please" filler.

| Element | Copy |
|---|---|
| Meta line | `{count} flats found, {dupCount} duplicates merged` |
| Best deal badge | `Best deal` |
| Best deal subtext | `{pct}% below area average` |
| Source chip prefix | `Also listed on` |
| Empty state headline | `No flats match these filters` |
| Empty state body | `Try widening your budget or BHK range.` |
| Price caption | `per month` |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A tenant can go from opening the search to seeing combined, de-duplicated multi-platform results in a single interaction, without visiting any external site first.
- **SC-002**: When the same flat exists on multiple sources in the data, it is presented as a single card in at least 95% of such cases (no visible duplicate cards for the same flat), with its sources compared on that card.
- **SC-003**: The results meta line accurately reflects the number of groups and the number of duplicates merged for every result set.
- **SC-004**: Best-deal badges appear only on groups whose cheapest price is ≥10% below the locality+BHK area average, and the displayed discount percentage matches the underlying data in 100% of cases.
- **SC-005**: First results appear within approximately 3 seconds for the majority of searches, and no search is blocked longer than approximately 8 seconds by a slow source.
- **SC-006**: When a listing (or a specific source chip) is selected, at least 95% of redirects land on the correct, specific original listing on the correct source.
- **SC-007**: When some sources are unavailable, tenants still receive results from the remaining sources; when all are unavailable, they see an error with retry rather than an empty state.
- **SC-008**: In usability testing, tenants report the unified, de-duplicated search saves meaningful time versus checking multiple apps (target: 80% agree) and complete a representative search-to-redirect task without assistance.

## Assumptions

- **Scope**: This MVP covers rental listings only (no buy/sell) and the discovery + comparison + redirect flow, including duplicate grouping and the best-deal signal. FlatFlo does not host listings, process bookings, or take payments.
- **Grouped data from the API**: The results view consumes already-grouped `ListingGroup` data; the duplicate-detection algorithm and the area-average computation are backend responsibilities (satisfied over the seeded feed for the MVP).
- **Area average source**: For the MVP, area-average rent per locality+BHK is derived from the seeded feed itself; a more authoritative source is a later concern.
- **Geography**: Mumbai-first (areas such as Goregaon, Malad, Thane); the design should not hard-block expansion to other cities.
- **Anonymous use**: Usable without an account; saved searches, favorites, and alerts are out of scope.
- **Currency/units**: Prices in INR, area in sq ft, rent is monthly ("per month").
- **Platforms**: Listings carry a source-platform label from a small set of major Indian rental platforms; in the MVP attached to seeded data.
- **Seeded data for MVP**: The MVP runs on a manually created dummy feed. The AI-agent-driven ("Claude search") JSON source and the eventual golden source are later phases; manual seeding is temporary.
- **Map view screen deferred**: Only the map toggle entry point is in scope; the map screen and synced list-map hover are a separate later feature.
- **Deferred**: Listing detail/intermediate page, area insight/rent-trend charts, and saved searches/alerts are out of scope.
- **Best-effort freshness**: Listing data is best-effort and may be stale; live availability is not guaranteed and this is communicated.

## Dependencies

- A seeded (manually created) listing data feed for the MVP, structured to the raw-Listing shape and pre-groupable into `ListingGroup`s (FR-002, FR-003).
- A grouping/duplicate-detection capability and an area-average computation that produce `ListingGroup`s with cheapest price, area average, and best-deal flag (FR-003, FR-004, FR-007).
- Ability to construct deep links into each source platform's individual listing pages (FR-005).
- (Later phase) An AI-agent-driven source that retrieves listings and returns them as structured JSON, replacing the seeded feed (FR-002).
