# FlatFlo — Work Checkpoint

> Latest checkpoint on top. Older ones are appended under History.

## Current — 2026-07-03 (evening)

**Branch:** `flatflo-0000` · **Last commit:** `d7b5b46 MVP ready with dummy data` · **Working tree:** uncommitted docs (CHECKPOINT.md, tasks.md, .claude/agents/)

### Done
- **Reconciled [tasks.md](specs/001-unified-rental-search/tasks.md) checkboxes to reality** (39/56 done) and added a status banner at the top. This surfaced a correction (see Notes): US2 and US3-presentation were **never actually built**.
- Verified per-phase state directly against the code: Setup ✅, Foundational ✅, US1 ✅, US4 ✅ (except Map toggle), US2 ❌, US3 presentation ❌, Polish ❌.
- Freed the dev-server ports (:8080/:4200 confirmed down).

### Next step
- **Implement US2 — redirect (T033–T036)** in [listing-card](angular/src/app/search/listing-card/): make the whole card a keyboard-focusable target opening the cheapest source's `sourceUrl` in a new tab; make each source chip its own link overriding the card default; add the "availability not guaranteed" notice. Then US3 presentation (T038/T040: best-deal badge + "% below area average" + accent border).

### Notes
- ⚠️ **Correction to the previous checkpoint:** it claimed "US1–US4 implemented." Not true — **US2 (redirect) and US3 (best-deal badge presentation) are NOT built.** [listing-card.ts](angular/src/app/search/listing-card/listing-card.ts) is US1-only (its own doc comment says redirect/badge/a11y are "layered on later"); grep found no `_blank`/`window.open`/`href` and no badge markup. Backend best-deal *computation* (T013/T015) and default sort (T039) ARE done — only the UI presentation is missing.
- Refer buddies to subscribe to **FreeDev** and note the tokens.
- The **checkpoint-keeper agent is now available** (session restart happened) — use it for future checkpoints instead of writing this file by hand.

### Open questions / TODOs
- **Deferred from US4:** Map-toggle entry point (rest of T045) and mobile "Edit search" full-screen sheet (T053).
- **Phase 7 Polish not started** (T048–T056): UI states, per-source status, image `onerror` fallback (T050 has lazy-load only), a11y pass, copy pass, timeout/partial-source seam, swap-seam test.
- Commit the reconciled docs + checkpoint-keeper agent (pending).

---

## History

### 2026-07-03

**Branch:** `flatflo-0000` · **Last commit:** `d7b5b46 MVP ready with dummy data` · **Working tree:** clean (only untracked `.claude/agents/`)

**Done**
- Completed **US4 — Refine and sort results** (Phase 6 of Unified Rental Search, [specs/001-unified-rental-search](specs/001-unified-rental-search/)); committed in `d7b5b46`.
- **Backend** — wired budget-range + furnishing filters into [SearchService.java](java/flatflo/src/main/java/com/flatflow/search/SearchService.java) (were stubbed). Added 3 tests to [SearchServiceTest.java](java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java). 17 backend tests green.
- **Frontend** — furnishing selector + active-filter badge + sort in filter-bar; sort dropdown in results-meta; search-page re-queries instantly on sort + furnishing URL round-trip. New filter-bar.spec + extended search-page.spec. 13 Angular specs green.
- **Bug fixed** — filter-bar used `(ngSubmit)` without importing `FormsModule`, so the Search button was inert in US1. Fixed.
- Verified live via curl (dedup, best-deal, filters, sort, 400 validation).
- Added the **checkpoint-keeper** agent.

**Next step (as recorded then)**: reconcile tasks.md checkboxes — *done in the current checkpoint above.*
