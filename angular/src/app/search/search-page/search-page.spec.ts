import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SearchPage } from './search-page';
import { SearchService } from '../search.service';
import { SearchCriteria, SearchResponse } from '../models';

class SearchServiceStub {
  areas = { areas: [{ id: 'GOREGAON_EAST', name: 'Goregaon East' }] };
  /** Records the criteria of every search() call for assertions. */
  calls: SearchCriteria[] = [];
  response: SearchResponse = {
    results: [
      {
        groupId: 'g1',
        title: '2 BHK in Goregaon East',
        metaLine: '650 sq ft · Semi-furnished · floor 4',
        locality: 'Goregaon East',
        priceDisplay: '₹32,000',
        cheapestPrice: 32000,
        isBestDeal: true,
        bestDealDiscountPct: 26,
        primaryPhotoUrl: null,
        newestUpdated: '2026-07-01',
        available: true,
        sources: [
          { sourcePlatform: 'NoBroker', sourceUrl: 'u-nb', priceDisplay: '₹32,000', price: 32000, accessibleLabel: 'l' },
        ],
      },
    ],
    count: 1,
    dupCount: 1,
    sort: 'BEST_DEAL',
    page: 0,
    pageSize: 20,
    hasMore: false,
    sources: [{ sourcePlatform: 'SeedFeed', reachable: true }],
  };
  /** When set, search() errors with this instead of returning results. */
  error: unknown | null = null;
  getAreas() {
    return of(this.areas);
  }
  search(criteria: SearchCriteria) {
    this.calls.push(criteria);
    return this.error ? throwError(() => this.error) : of(this.response);
  }
}

const CRITERIA: SearchCriteria = {
  location: 'GOREGAON_EAST', bhk: 'TWO_BHK', budgetMin: null, budgetMax: null,
  furnishing: null, sort: 'BEST_DEAL', page: 0,
};

describe('SearchPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchPage],
      providers: [
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: new Map() } } },
      ],
    }).compileComponents();
  });

  it('renders the meta line and grouped cards after a search', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const page = fixture.componentInstance as unknown as {
      onSearch: (c: unknown) => void;
    };
    fixture.detectChanges();

    page.onSearch({
      location: 'GOREGAON_EAST',
      bhk: 'TWO_BHK',
      budgetMin: null,
      budgetMax: null,
      furnishing: null,
      sort: 'BEST_DEAL',
      page: 0,
    });
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.summary')?.textContent).toContain('1 flats found, 1 duplicates merged');
    expect(el.querySelectorAll('app-listing-card').length).toBe(1);
  });

  it('shows the empty state when no groups match', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    svc.response = { ...svc.response, results: [], count: 0, dupCount: 0 };

    const page = fixture.componentInstance as unknown as { onSearch: (c: unknown) => void };
    fixture.detectChanges();
    page.onSearch({
      location: 'GOREGAON_EAST', bhk: 'TWO_BHK', budgetMin: null, budgetMax: null,
      furnishing: null, sort: 'BEST_DEAL', page: 0,
    });
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-empty-state')).not.toBeNull();
    expect(el.querySelector('app-error-state')).toBeNull();
  });

  it('shows a skeleton while loading, without removing the filter bar (FR-015)', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const page = fixture.componentInstance as unknown as { loading: { set: (v: boolean) => void } };
    fixture.detectChanges();

    // Force the loading state (a real request resolves synchronously in the stub).
    page.loading.set(true);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-skeleton-state')).not.toBeNull();
    expect(el.querySelector('app-filter-bar')).not.toBeNull();
  });

  it('shows the error state (with the server message) on a 503 all-sources-down, not the empty state', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    svc.error = new HttpErrorResponse({
      status: 503,
      error: { error: 'ALL_SOURCES_UNAVAILABLE', message: 'No listing sources could be reached.' },
    });

    const page = fixture.componentInstance as unknown as { onSearch: (c: SearchCriteria) => void };
    fixture.detectChanges();
    page.onSearch(CRITERIA);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-error-state')).not.toBeNull();
    expect(el.querySelector('app-empty-state')).toBeNull();
    expect(el.querySelector('app-error-state .message')?.textContent).toContain('No listing sources could be reached');
  });

  it('shows generic error copy for a non-503 failure', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    svc.error = new HttpErrorResponse({ status: 500 });

    const page = fixture.componentInstance as unknown as { onSearch: (c: SearchCriteria) => void };
    fixture.detectChanges();
    page.onSearch(CRITERIA);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-error-state')).not.toBeNull();
    expect(el.querySelector('app-error-state .message')?.textContent).toContain('Something went wrong');
  });

  it('retry re-runs the last search (and recovers once the error clears)', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    svc.error = new HttpErrorResponse({ status: 500 });

    const page = fixture.componentInstance as unknown as {
      onSearch: (c: SearchCriteria) => void;
      onRetry: () => void;
    };
    fixture.detectChanges();
    page.onSearch({ ...CRITERIA, page: 3 });
    fixture.detectChanges();

    svc.error = null; // source recovers
    page.onRetry();
    fixture.detectChanges();

    // The retry re-issued the last criteria verbatim (same page), and results now render.
    expect(svc.calls.length).toBe(2);
    expect(svc.calls[1].page).toBe(3);
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-error-state')).toBeNull();
    expect(el.querySelectorAll('app-listing-card').length).toBe(1);
  });

  it('re-queries with the new sort when the sort control changes (page reset)', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    const page = fixture.componentInstance as unknown as {
      onSearch: (c: SearchCriteria) => void;
      onSortChange: (s: string) => void;
    };
    fixture.detectChanges();

    page.onSearch({
      location: 'GOREGAON_EAST', bhk: 'TWO_BHK', budgetMin: null, budgetMax: null,
      furnishing: null, sort: 'BEST_DEAL', page: 2,
    });
    page.onSortChange('PRICE_ASC');

    // Two searches ran; the second used the new sort with page reset to 0.
    expect(svc.calls.length).toBe(2);
    expect(svc.calls[1].sort).toBe('PRICE_ASC');
    expect(svc.calls[1].page).toBe(0);
  });

  it('is a no-op when sort changes before any search has run', () => {
    const fixture = TestBed.createComponent(SearchPage);
    const svc = TestBed.inject(SearchService) as unknown as SearchServiceStub;
    const page = fixture.componentInstance as unknown as { onSortChange: (s: string) => void };
    fixture.detectChanges();

    page.onSortChange('NEWEST');
    expect(svc.calls.length).toBe(0);
  });
});

describe('SearchPage URL restore', () => {
  it('restores furnishing and sort from query params on load', () => {
    const svc = new SearchServiceStub();
    const params = new Map<string, string>([
      ['location', 'GOREGAON_EAST'],
      ['bhk', 'TWO_BHK'],
      ['furnishing', 'FULLY_FURNISHED'],
      ['sort', 'PRICE_DESC'],
    ]);

    TestBed.configureTestingModule({
      imports: [SearchPage],
      providers: [
        { provide: SearchService, useValue: svc },
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: params } } },
      ],
    });

    const fixture = TestBed.createComponent(SearchPage);
    fixture.detectChanges();

    expect(svc.calls.length).toBe(1);
    expect(svc.calls[0].furnishing).toBe('FULLY_FURNISHED');
    expect(svc.calls[0].sort).toBe('PRICE_DESC');
  });
});
