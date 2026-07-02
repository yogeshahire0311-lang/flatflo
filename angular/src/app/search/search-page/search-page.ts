import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchService } from '../search.service';
import { Area, Bhk, Furnishing, SearchCriteria, SearchResponse, SortMode } from '../models';
import { FilterBar } from '../filter-bar/filter-bar';
import { ResultsMeta } from '../results-meta/results-meta';
import { ListingCard } from '../listing-card/listing-card';

/**
 * Container for the results screen (US1). Loads supported areas for the picker,
 * runs a search when the filter bar submits, renders the meta line + grouped
 * cards, and reflects the current criteria in the URL query params (FR-010).
 */
@Component({
  selector: 'app-search-page',
  standalone: true,
  imports: [FilterBar, ResultsMeta, ListingCard],
  templateUrl: './search-page.html',
  styleUrl: './search-page.css',
})
export class SearchPage {
  private readonly searchService = inject(SearchService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly areas = signal<Area[]>([]);
  protected readonly response = signal<SearchResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly searched = signal(false);
  protected readonly criteria = signal<SearchCriteria | null>(null);

  constructor() {
    this.searchService.getAreas().subscribe((res) => this.areas.set(res.areas));

    // Restore a search from URL params (shareable/bookmarkable).
    const params = this.route.snapshot.queryParamMap;
    const location = params.get('location');
    if (location) {
      const restored: SearchCriteria = {
        location,
        bhk: (params.get('bhk') as Bhk) ?? 'TWO_BHK',
        budgetMin: params.get('budgetMin') ? Number(params.get('budgetMin')) : null,
        budgetMax: params.get('budgetMax') ? Number(params.get('budgetMax')) : null,
        furnishing: (params.get('furnishing') as Furnishing) ?? null,
        sort: (params.get('sort') as SortMode) ?? 'BEST_DEAL',
        page: params.get('page') ? Number(params.get('page')) : 0,
      };
      this.runSearch(restored);
    }
  }

  protected onSearch(criteria: SearchCriteria): void {
    this.runSearch(criteria);
  }

  /** Sort changed from the results-meta control — re-query immediately (US4, page reset). */
  protected onSortChange(sort: SortMode): void {
    const current = this.criteria();
    if (!current) {
      return;
    }
    this.runSearch({ ...current, sort, page: 0 });
  }

  private runSearch(criteria: SearchCriteria): void {
    this.criteria.set(criteria);
    this.loading.set(true);
    this.searched.set(true);

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        location: criteria.location,
        bhk: criteria.bhk,
        budgetMin: criteria.budgetMin ?? null,
        budgetMax: criteria.budgetMax ?? null,
        furnishing: criteria.furnishing ?? null,
        sort: criteria.sort,
        page: criteria.page,
      },
      queryParamsHandling: 'merge',
    });

    this.searchService.search(criteria).subscribe({
      next: (res) => {
        this.response.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.response.set(null);
        this.loading.set(false);
      },
    });
  }
}
