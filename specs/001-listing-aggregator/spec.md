# Feature Specification: Listing Aggregator

**Feature Branch**: `001-listing-aggregator`

**Created**: 2026-07-02

**Status**: Draft

**Input**: User description: "Aggregate listings from multiple platforms (NoBroker, MagicBricks, 99acres, Housing, etc.). Show price, summary, and source. Redirect users to the original platform. Do NOT host listings initially."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse aggregated listings in one place (Priority: P1)

A renter looking for a flat wants to see listings from multiple property platforms (NoBroker, MagicBricks, 99acres, Housing, etc.) side by side, without visiting each platform separately, so they can compare options faster.

**Why this priority**: This is the core value proposition of the product — without aggregation and display, there is no product. Every other capability builds on this.

**Independent Test**: Can be fully tested by loading the aggregator and confirming that listings sourced from at least two different platforms appear together in a single browsable list, each showing price, summary, and source platform.

**Acceptance Scenarios**:

1. **Given** listings exist from multiple source platforms, **When** a user opens the aggregator, **Then** they see a combined list of listings, each showing at minimum a price, a short summary, and the name of the source platform.
2. **Given** a source platform has no listings matching current criteria, **When** a user views the aggregated list, **Then** listings from the remaining available platforms are still shown without error.
3. **Given** a listing's source data is temporarily unavailable, **When** the aggregated list is displayed, **Then** the unavailable listing is omitted rather than shown with broken or missing information.

---

### User Story 2 - View listing details and go to the original platform (Priority: P1)

A renter finds a listing of interest in the aggregated view and wants to see the full details and proceed with contacting the lister, so they click through and are redirected to the original listing on the source platform.

**Why this priority**: Since listings are not hosted, redirecting to the source is the only way a user can act on a listing (view full details, contact the lister, etc.). Without this, the aggregator is a dead end.

**Independent Test**: Can be fully tested by selecting any listing in the aggregated view and confirming the user is taken to the corresponding listing page on the originating platform.

**Acceptance Scenarios**:

1. **Given** a user is viewing the aggregated list, **When** they select a listing, **Then** they are redirected to that listing's page on its original source platform.
2. **Given** a listing's original source link is broken or no longer valid, **When** a user attempts to open it, **Then** the user is shown a clear message that the listing may no longer be available, rather than a silent failure or generic error.

---

### User Story 3 - Search and filter aggregated listings (Priority: P2)

A renter wants to narrow the aggregated listings by criteria such as location and price range, so they only see listings relevant to their search.

**Why this priority**: Search/filter significantly improves usability once aggregation works, but the product still delivers value with an unfiltered combined list (P1). This is an enhancement to findability, not the core aggregation mechanic.

**Independent Test**: Can be fully tested by applying a location and/or price filter and confirming that only matching listings from across all source platforms are displayed.

**Acceptance Scenarios**:

1. **Given** aggregated listings from multiple platforms, **When** a user filters by location, **Then** only listings matching that location (regardless of source platform) are shown.
2. **Given** aggregated listings from multiple platforms, **When** a user filters by a price range, **Then** only listings whose price falls within that range are shown.
3. **Given** a user applies filters that match no listings, **When** the results are displayed, **Then** the user sees a clear "no results" message rather than an empty or broken screen.

---

### User Story 4 - Avoid duplicate listings across platforms (Priority: P3)

A renter browsing the aggregator does not want to see what is effectively the same flat listed separately just because it was posted on more than one source platform.

**Why this priority**: Duplicate listings degrade trust and usability but do not block the core value of aggregation and redirection. This is a quality-of-life refinement layered on top of P1/P2.

**Independent Test**: Can be fully tested by aggregating listings that represent the same underlying property from two different source platforms and confirming the user sees it presented as a single entry (or clearly grouped) rather than as unrelated duplicates.

**Acceptance Scenarios**:

1. **Given** the same underlying property is listed on two different source platforms with matching key details (e.g., address and price), **When** the aggregated list is displayed, **Then** the listings are grouped or flagged as likely duplicates rather than shown as fully independent entries.
2. **Given** two listings have similar but not matching details (e.g., different price or address), **When** the aggregated list is displayed, **Then** they are treated as distinct listings.

---

### Edge Cases

- What happens when a source platform changes its listing page structure or stops providing data, breaking data collection from that source?
- How does the system handle a source platform being temporarily down or unreachable when the user tries to redirect to it?
- What happens when a listing's price or key details are missing or malformed in the source data?
- How does the system handle a source platform blocking or rate-limiting automated data collection?
- What happens when a listing is removed from the source platform after having already been shown in the aggregator?
- How does the system indicate to the user that they are leaving the aggregator and going to a third-party site?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST collect listing data from multiple external property platforms, including at minimum NoBroker, MagicBricks, 99acres, and Housing.
- **FR-002**: System MUST display, for each aggregated listing, at minimum: price, a short summary of the property, and the name of the source platform.
- **FR-003**: System MUST NOT host full listing content (e.g., full photo galleries, full descriptions, contact/lister details) — only enough summary information to help a user decide whether to view the original listing.
- **FR-004**: System MUST allow a user to navigate from an aggregated listing directly to that listing's original page on the source platform.
- **FR-005**: System MUST clearly indicate to the user, before or during redirection, that they are being sent to an external third-party platform.
- **FR-006**: System MUST omit a listing from the aggregated view if its underlying source data is unavailable or incomplete for the required summary fields (price, summary, source).
- **FR-007**: System MUST refresh aggregated listing data at least once per day so that users do not repeatedly see listings that are no longer available on the source platform.
- **FR-008**: System MUST allow users to filter aggregated listings by location and by price range.
- **FR-009**: System MUST detect and group likely-duplicate listings (the same underlying property posted on multiple source platforms) using shared identifying details (e.g., address, price).
- **FR-010**: System MUST present a clear message to the user when a redirect target is no longer available, rather than failing silently.
- **FR-011**: System MUST limit initial geographic coverage to Thane at launch, with expansion to other cities/regions considered post-launch.

### Key Entities

- **Listing**: A single rental property posting sourced from one external platform. Key attributes: price, summary/description snippet, source platform name, link to the original listing, location, and a last-seen/refreshed timestamp.
- **Source Platform**: An external property listing website (e.g., NoBroker, MagicBricks, 99acres, Housing) from which listing data is collected. Key attributes: platform name, base site URL, and collection status (active/unavailable).
- **Duplicate Group**: A set of two or more Listings from different Source Platforms judged to represent the same underlying property, grouped together for display purposes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can view listings from at least 3 different source platforms in a single combined list within 3 seconds of opening the aggregator.
- **SC-002**: A user can go from an aggregated listing to the corresponding original platform page in one action (e.g., a single click/tap).
- **SC-003**: Fewer than 5% of listings shown to users lead to a "listing no longer available" outcome when the user redirects to the source.
- **SC-004**: At least 90% of true duplicate listings (same property, multiple platforms) are correctly grouped rather than shown as separate entries.
- **SC-005**: Users applying a location or price filter see only matching results, with zero incorrect (non-matching) listings in the filtered results.

## Assumptions

- Listing data is collected from publicly accessible pages of source platforms; no authenticated/paid data-partner access is assumed for v1.
- The aggregator is a discovery and redirection layer only — all lease/rental transactions, contact with listers, and payments continue to happen on the source platforms.
- "Summary" for a listing means a short human-readable snippet (e.g., property type, bedrooms, rough location) sufficient to help a user decide whether to click through, not a full reproduction of the source listing.
- Source platforms' publicly available listing pages provide enough structured or semi-structured information (price, address, description) to support collection.
- Users are primarily browsing on web/mobile-web; no native mobile app is assumed for v1.
- Initial launch covers Thane only; other cities are out of scope until validated in this market.
- Daily refresh of source data is an acceptable freshness level for v1; more frequent refresh can be considered later if staleness becomes a user complaint.
