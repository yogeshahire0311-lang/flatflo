import { Component, computed, effect, input, signal } from '@angular/core';
import { ListingGroup, SourceOffer } from '../models';

/**
 * A single grouped result card (spec FR-012): photo, title, meta line,
 * cheapest price with a "per month" caption, and a source-comparison row.
 *
 * US2 redirect: the whole card is a keyboard-focusable link that opens the
 * cheapest source in a new tab (results view preserved); each source chip is an
 * independent link that opens its own source, overriding the card default.
 *
 * US3 best deal: when the group is flagged {@link ListingGroup.isBestDeal}, the
 * card shows a "Best deal" badge, a "{pct}% below area average" subtext, and a
 * 2px accent border (FR-007). Nothing is shown otherwise.
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

  /** Set when the photo URL fails to load, so we fall back to the placeholder (FR-018, T050). */
  protected readonly imageFailed = signal(false);

  /** Show the real photo only when there's a URL that hasn't failed to load. */
  protected readonly showPhoto = computed(() => !!this.group().primaryPhotoUrl && !this.imageFailed());

  constructor() {
    // Reset the failure flag when the card is reused for a different group.
    effect(() => {
      this.group().primaryPhotoUrl;
      this.imageFailed.set(false);
    });
  }

  /** The cheapest source (offers are pre-sorted by price ascending) — the card default. */
  protected readonly cheapest = computed<SourceOffer>(() => this.group().sources[0]);

  /** True when this group qualifies as a best deal (US3, FR-007). */
  protected readonly isBestDeal = computed(() => this.group().isBestDeal);

  /** "{pct}% below area average" subtext, shown only for best-deal groups. */
  protected readonly bestDealSubtext = computed(() => {
    const pct = this.group().bestDealDiscountPct;
    return pct == null ? null : `${pct}% below area average`;
  });

  /** Additional sources beyond the cheapest (shown in the "Also listed on" row). */
  protected readonly otherSources = computed(() => this.group().sources.slice(1));
  protected readonly hasOtherSources = computed(() => this.group().sources.length > 1);

  /** Open a source listing in a new tab, keeping the results tab intact (FR-005). */
  protected openSource(url: string): void {
    window.open(url, '_blank', 'noopener');
  }

  /**
   * Open a specific source from its chip, overriding the card default. Stops
   * propagation so the card handler doesn't also fire, and prevents the anchor's
   * native navigation so we control the new-tab open (the `href` stays for
   * accessibility and middle/ctrl-click).
   */
  protected onChipClick(event: MouseEvent, url: string): void {
    event.stopPropagation();
    event.preventDefault();
    this.openSource(url);
  }

  /** Fall back to the placeholder when the photo can't load (broken/unreachable URL). */
  protected onImageError(): void {
    this.imageFailed.set(true);
  }

  /** Activate the card default (cheapest source) via keyboard (Enter/Space). */
  protected onCardKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.openSource(this.cheapest().sourceUrl);
    }
  }
}
