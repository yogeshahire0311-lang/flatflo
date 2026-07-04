import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Area, Bhk, Furnishing, SearchCriteria, SortMode } from '../models';

/**
 * Sticky search/filter bar (US1 + US4): location picker (from supported areas),
 * optional budget range, BHK and furnishing selectors, and an active-filter count
 * badge. Emits the criteria on submit; the active sort is owned by the page and
 * threaded through here so pressing Search preserves it (sort itself is changed
 * from the results-meta control).
 */
@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './filter-bar.html',
  styleUrl: './filter-bar.css',
})
export class FilterBar {
  readonly areas = input.required<Area[]>();
  /** The page's current sort, preserved when the bar re-submits. */
  readonly sort = input<SortMode>('BEST_DEAL');

  readonly search = output<SearchCriteria>();

  protected readonly location = signal<string>('');
  protected readonly bhk = signal<Bhk>('TWO_BHK');
  protected readonly budgetMin = signal<number | null>(null);
  protected readonly budgetMax = signal<number | null>(null);
  protected readonly furnishing = signal<Furnishing | null>(null);

  protected readonly canSearch = computed(() => this.location().length > 0);

  /** Mobile: whether the full-screen "Edit search" sheet is open (UI spec §4). */
  protected readonly sheetOpen = signal(false);

  /** One-line recap of the current criteria for the collapsed mobile summary. */
  protected readonly summary = computed(() => {
    const areaName = this.areas().find((a) => a.id === this.location())?.name;
    const bhkLabel = this.bhkOptions.find((o) => o.value === this.bhk())?.label ?? '';
    return areaName ? `${areaName} · ${bhkLabel}` : 'Set your search';
  });

  protected openSheet(): void {
    this.sheetOpen.set(true);
  }

  protected closeSheet(): void {
    this.sheetOpen.set(false);
  }

  /** Count of non-default refinement filters set (budget bounds + furnishing) — FR-014. */
  protected readonly activeFilterCount = computed(() => {
    let n = 0;
    if (this.budgetMin() != null) n++;
    if (this.budgetMax() != null) n++;
    if (this.furnishing() != null) n++;
    return n;
  });

  protected readonly bhkOptions: { value: Bhk; label: string }[] = [
    { value: 'ONE_BHK', label: '1 BHK' },
    { value: 'TWO_BHK', label: '2 BHK' },
    { value: 'THREE_BHK', label: '3 BHK' },
  ];

  protected readonly furnishingOptions: { value: Furnishing; label: string }[] = [
    { value: 'UNFURNISHED', label: 'Unfurnished' },
    { value: 'SEMI_FURNISHED', label: 'Semi-furnished' },
    { value: 'FULLY_FURNISHED', label: 'Fully furnished' },
  ];

  protected onLocation(value: string): void {
    this.location.set(value);
  }

  protected onBhk(value: string): void {
    this.bhk.set(value as Bhk);
  }

  protected onBudgetMin(value: string): void {
    this.budgetMin.set(value ? Number(value) : null);
  }

  protected onBudgetMax(value: string): void {
    this.budgetMax.set(value ? Number(value) : null);
  }

  protected onFurnishing(value: string): void {
    this.furnishing.set(value ? (value as Furnishing) : null);
  }

  protected submit(): void {
    if (!this.canSearch()) {
      return;
    }
    this.search.emit({
      location: this.location(),
      bhk: this.bhk(),
      budgetMin: this.budgetMin(),
      budgetMax: this.budgetMax(),
      furnishing: this.furnishing(),
      sort: this.sort(),
      page: 0,
    });
    // On mobile the fields live in a sheet; searching dismisses it back to results.
    this.closeSheet();
  }
}
