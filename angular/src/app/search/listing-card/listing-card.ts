import { Component, computed, input } from '@angular/core';
import { ListingGroup } from '../models';

/**
 * A single grouped result card (spec FR-012): photo, title, meta line,
 * cheapest price with a "per month" caption, and a source-comparison row.
 *
 * US1 renders the card and its chips. Card/chip redirect (US2), the best-deal
 * badge (US3), and image fallback + accessibility (Polish) are layered on later.
 */
@Component({
  selector: 'app-listing-card',
  standalone: true,
  imports: [],
  templateUrl: './listing-card.html',
  styleUrl: './listing-card.css',
})
export class ListingCard {
  readonly group = input.required<ListingGroup>();

  /** Additional sources beyond the cheapest (shown in the "Also listed on" row). */
  protected readonly otherSources = computed(() => this.group().sources.slice(1));
  protected readonly hasOtherSources = computed(() => this.group().sources.length > 1);
}
