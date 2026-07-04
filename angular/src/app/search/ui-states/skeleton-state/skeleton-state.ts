import { Component, computed, input } from '@angular/core';

/**
 * Loading placeholder for the results list (FR-015): a few skeleton cards that
 * mirror the {@link ListingCard} geometry while a search is in flight. The
 * filter bar stays interactive because this only occupies the results area.
 *
 * Exposed as a single `role="status"` region with a visually-hidden label so a
 * screen reader hears one "Searching…" announcement, not N silent boxes.
 */
@Component({
  selector: 'app-skeleton-state',
  standalone: true,
  imports: [],
  templateUrl: './skeleton-state.html',
  styleUrl: './skeleton-state.css',
})
export class SkeletonState {
  /** Number of placeholder cards to show (3–5). */
  readonly count = input<number>(4);

  protected readonly placeholders = computed(() =>
    Array.from({ length: this.count() }, (_, i) => i),
  );
}
