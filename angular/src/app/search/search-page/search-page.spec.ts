import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
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
  getAreas() {
    return of(this.areas);
  }
  search(criteria: SearchCriteria) {
    this.calls.push(criteria);
    return of(this.response);
  }
}

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
    expect(el.querySelector('.empty h2')?.textContent).toContain('No flats match these filters');
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
