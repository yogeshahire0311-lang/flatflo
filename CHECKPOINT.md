# FlatFlo — Work Checkpoint

> Latest checkpoint on top. Older ones are appended under History.

## Current — 2026-07-03

**Branch:** `flatflo-0000` · **Last commit:** `d7b5b46 MVP ready with dummy data` · **Working tree:** clean (only untracked `.claude/agents/`)

### Done
- Completed **US4 — Refine and sort results** (Phase 6 of Unified Rental Search, [specs/001-unified-rental-search](specs/001-unified-rental-search/)); committed in `d7b5b46`.
- **Backend** — wired budget-range + furnishing filters into [SearchService.java](java/flatflo/src/main/java/com/flatflow/search/SearchService.java) (were stubbed): budget filters on group's cheapest price; furnishing exact-match, null = any. Added 3 tests to [SearchServiceTest.java](java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java) (budget bounds, furnishing, PRICE_ASC/DESC/NEWEST). **17 backend tests green.**
- **Frontend** ([angular/src/app/search/](angular/src/app/search/)) — furnishing selector + active-filter badge + sort threaded through [filter-bar](angular/src/app/search/filter-bar/filter-bar.ts); sort dropdown emitting `sortChange` in [results-meta](angular/src/app/search/results-meta/results-meta.ts); [search-page](angular/src/app/search/search-page/search-page.ts) re-queries instantly on sort (page reset) + furnishing round-trips through URL. New [filter-bar.spec.ts](angular/src/app/search/filter-bar/filter-bar.spec.ts) + extended [search-page.spec.ts](angular/src/app/search/search-page/search-page.spec.ts). **13 Angular specs green, build clean.**
- **Bug fixed** — [filter-bar.ts](angular/src/app/search/filter-bar/filter-bar.ts) used `(ngSubmit)` but never imported `FormsModule` (imported nowhere in app), so the **Search button was inert in US1** — latent pre-existing defect. Fixed by importing `FormsModule`.
- **Verified live** — `gradlew bootRun` + `ng serve`; confirmed via curl that dedup, best-deal, furnishing filter, budget filter, PRICE_DESC sort, and 400-on-`budgetMin>budgetMax` all work.
- Added a **checkpoint-keeper** custom agent at [.claude/agents/checkpoint-keeper.md](.claude/agents/checkpoint-keeper.md) to maintain this file (not yet committed).

### Next step
- **Reconcile [tasks.md](specs/001-unified-rental-search/tasks.md) checkboxes** — all boxes are still unchecked even though US1–US4 are implemented. Mark the completed tasks done so the file matches reality.

### Notes
- Refer buddies to subscribe to **FreeDev** and note the tokens.
- Two dev servers may still be running from this session: `gradlew bootRun` (:8080) and `ng serve` (:4200).
- The **checkpoint-keeper agent needs a session restart** to become available (agent defs load at startup); this checkpoint was written directly as a one-time fallback.

### Open questions / TODOs
- **Deferred from US4:** Map-toggle entry point (rest of T045) and mobile "Edit search" full-screen sheet (T053).
- **Phase 7 Polish not started** (T048–T056): UI states, per-source status, image fallback, a11y pass, copy pass, timeout/partial-source seam, swap-seam test. T048 (UI states) is the natural first pick after tasks.md reconcile.

---

## History
