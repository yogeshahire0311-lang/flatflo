import { Component, computed, input, output } from '@angular/core';
import { SortMode, SourceStatus } from '../models';

/**
 * The results meta line (spec FR-011): total group count + duplicates-merged
 * count on the left, a sort control on the right. Changing the sort emits
 * {@link sortChange} so the page re-queries immediately (US4). Sentence case.
 */
@Component({
  selector: 'app-results-meta',
  standalone: true,
  imports: [],
  templateUrl: './results-meta.html',
  styleUrl: './results-meta.css',
})
export class ResultsMeta {
  readonly count = input.required<number>();
  readonly dupCount = input.required<number>();
  readonly sort = input.required<SortMode>();
  /** Per-source reachability; drives the "N of M sources" coverage note (FR-017). */
  readonly sources = input<SourceStatus[]>([]);

  readonly sortChange = output<SortMode>();

  protected readonly summary = computed(
    () => `${this.count()} flats found, ${this.dupCount()} duplicates merged`,
  );

  private readonly total = computed(() => this.sources().length);
  private readonly reachable = computed(() => this.sources().filter((s) => s.reachable).length);

  /** Only shown on partial failure (some sources unreachable); hidden when all are reachable. */
  protected readonly coverage = computed(() =>
    this.total() > 0 && this.reachable() < this.total()
      ? `Results from ${this.reachable()} of ${this.total()} sources`
      : null,
  );

  protected readonly sortOptions: { value: SortMode; label: string }[] = [
    { value: 'BEST_DEAL', label: 'Sorted by best deal' },
    { value: 'PRICE_ASC', label: 'Sorted by price: low to high' },
    { value: 'PRICE_DESC', label: 'Sorted by price: high to low' },
    { value: 'NEWEST', label: 'Sorted by newest' },
  ];

  protected onSort(value: string): void {
    this.sortChange.emit(value as SortMode);
  }
}
