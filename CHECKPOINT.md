# FlatFlo — Work Checkpoint

> Latest checkpoint on top. Older ones are appended under History.

## Current — 2026-07-05

**Branch:** `flatflo-0000` · **Last commit:** `eb9e3d6 US3: best-deal presentation — badge, % subtext, accent border` · **Working tree:** 1 uncommitted file (`.claude/settings.json`, intentional)

### Done
- Implemented **US3 — best-deal presentation** (Phase 5 of [specs/001-unified-rental-search](specs/001-unified-rental-search/), tasks T037/T038/T040), committed as `eb9e3d6` (6 files):
  - **T040 (presentation)** in [listing-card](angular/src/app/search/listing-card/): added `isBestDeal()` and `bestDealSubtext()` computeds to [listing-card.ts](angular/src/app/search/listing-card/listing-card.ts) ("{pct}% below area average", null when not a deal); [listing-card.html](angular/src/app/search/listing-card/listing-card.html) renders a **"Best deal" badge + subtext** in a new title-line, `[class.best-deal]` on the card, and folds the best-deal fact into the `aria-label` for a11y text conveyance; [listing-card.css](angular/src/app/search/listing-card/listing-card.css) adds a **2px `#059669` accent border** (kept on hover) plus green badge/subtext styles.
  - **T038 (component spec)** — 3 tests: badge/subtext/border render iff `isBestDeal`; nothing renders for a non-deal group; accessible label conveys the deal as text.
  - **T037 (backend sort test)** in [SearchServiceTest.java](java/flatflo/src/test/java/com/flatflow/search/SearchServiceTest.java) — 6 groups with a controlled 60k area average; asserts `BEST_DEAL` orders best deals first by discount desc (30% then 20%), then the rest by price asc, with correct `isBestDeal`/`bestDealDiscountPct` flags.
- Reconciled [tasks.md](specs/001-unified-rental-search/tasks.md): T037/T038/T040 checked; status banner updated (US2 + US3 done, T050 done); "Recommended next" now points at Phase 7 Polish.
- Confirmed the prior checkpoint's "fix T050 first" sub-task was already resolved: commit `e8c3d45` had committed US2 **and** fixed the T050 image `onerror` glitch, so the only real remaining work was US3.
- **Verification:** Angular **20/20 specs pass**, `ng build` clean; backend full `gradlew test` **BUILD SUCCESSFUL**.

### Next step
- **Start Phase 7 Polish (T048–T056)** — UI states, per-source status, a11y pass, copy pass, timeout/partial-source seam, swap-seam test. Exact entry point left open; the **a11y pass (T051)** and **copy pass (T052)** are natural first picks while the card work is fresh in mind.

### Notes
- Clean stopping point — no blockers, no pending decisions. US3 is fully done, committed, and both test suites are green.
- `.claude/settings.json` is intentionally left uncommitted (unrelated change, still modified in the working tree).
- No dev servers were started this session (tests run via `gradlew test` and `ng test --watch=false`).
- **T050 image `onerror` fallback is now DONE** (was PARTIAL in the prior checkpoint) — fixed in commit `e8c3d45`.

### Open questions / TODOs
- **Deferred ("someday", not prioritized ahead of Polish):** T045 Map-toggle entry point (rest of US4) and T053 mobile "Edit search" full-screen sheet.

---

## History

### 2026-07-04

**Branch:** `flatflo-0000` · **Last commit:** `76e369e Reconcile tasks.md to real state; correct checkpoint` · **Working tree:** 6 uncommitted files + untracked `mockups/`

#### Done
- Implemented **US2 — Redirect to the original listing** (T033–T036, Phase 4 of [specs/001-unified-rental-search](specs/001-unified-rental-search/)), all in [listing-card](angular/src/app/search/listing-card/) + [search-page](angular/src/app/search/search-page/):
  - **T034** — whole `listing-card` is a keyboard-focusable link (role="link", tabindex=0, Enter/Space activation) that opens the cheapest source's `sourceUrl` in a new tab via `window.open(url, '_blank', 'noopener')`; results view preserved.
  - **T035** — each "Also listed on" chip is a real `<a target="_blank" rel="noopener">` opening its own source; `onChipClick` stops propagation + `preventDefault` to override the card default.
  - **T036** — one-time "Listings link out to their source; availability isn't guaranteed." notice above the results in search-page (not per-card).
  - **T033** — 3 US2 specs added to [listing-card.spec.ts](angular/src/app/search/listing-card/listing-card.spec.ts). **16 Angular specs green, ng build clean.**
- **Design refinement (user-driven, during manual testing):** primary price now captioned "per month · from {source}" (chose Option 2 from [mockups/card-source-options.html](mockups/card-source-options.html)). Removed the redundant single-source plain chip — single source now conveyed only by the caption; multi-source cards keep their "Also listed on" chips. Spec updated.

#### Next step
- **Commit US2** (deleting the throwaway `mockups/` aid), then implement **US3 — best-deal badge presentation** (T037/T038/T040): badge + "{pct}% below area average" subtext + 2px accent border in listing-card when `isBestDeal`. Backend best-deal computation + default sort already done.

#### Notes
- ⚠️ **Manual pass NOT fully clean — image glitch to fix first (T050, currently PARTIAL):** the card with a broken property photo (Evershine Heights, seed URL `https://broken.example.com/missing-image.jpg`) renders a **broken image**. The 🏠 placeholder only shows when `primaryPhotoUrl` is null, not when a non-null URL fails to load. Need an `<img onerror>` fallback to the category placeholder so a broken image is never shown. **Do this before US3.**
- **US2 is not yet committed** (6 modified files + untracked `mockups/`).
- Two dev servers still running from this session: `gradlew bootRun` (:8080) and `ng serve` (:4200, hot-reload).
- `mockups/card-source-options.html` is a throwaway visual aid — **delete it when committing US2.**

#### Open questions / TODOs
- **Immediate sub-task before US3:** fix T050 `<img onerror>` fallback (see Notes) — never render a broken image.
- **Deferred from US4:** Map-toggle entry point (rest of T045) and mobile "Edit search" full-screen sheet (T053).
- **Phase 7 Polish not started** (T048–T056): UI states, per-source status, a11y pass, copy pass, timeout/partial-source seam, swap-seam test.

---

### 2026-07-03 (evening)

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
