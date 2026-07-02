import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ListingCard } from './listing-card';
import { ListingGroup } from '../models';

function group(overrides: Partial<ListingGroup> = {}): ListingGroup {
  return {
    groupId: 'g1',
    title: '2 BHK in Goregaon East',
    metaLine: '650 sq ft · Semi-furnished · floor 4',
    locality: 'Goregaon East',
    priceDisplay: '₹32,000',
    cheapestPrice: 32000,
    isBestDeal: false,
    bestDealDiscountPct: null,
    primaryPhotoUrl: 'https://img/g1.jpg',
    newestUpdated: '2026-07-01',
    available: true,
    sources: [
      { sourcePlatform: 'NoBroker', sourceUrl: 'u-nb', priceDisplay: '₹32,000', price: 32000, accessibleLabel: 'View this listing on NoBroker, ₹32,000 per month' },
    ],
    ...overrides,
  };
}

describe('ListingCard', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ListingCard] }).compileComponents();
  });

  function render(g: ListingGroup): HTMLElement {
    const fixture = TestBed.createComponent(ListingCard);
    (fixture.componentRef as ComponentRef<ListingCard>).setInput('group', g);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders title, price and "per month" caption', () => {
    const el = render(group());
    expect(el.querySelector('.title')?.textContent).toContain('2 BHK in Goregaon East');
    expect(el.querySelector('.price .amount')?.textContent).toContain('₹32,000');
    expect(el.querySelector('.price .caption')?.textContent).toContain('per month');
  });

  it('shows an "Also listed on" row with a chip per additional source', () => {
    const el = render(
      group({
        sources: [
          { sourcePlatform: 'NoBroker', sourceUrl: 'u-nb', priceDisplay: '₹32,000', price: 32000, accessibleLabel: 'l1' },
          { sourcePlatform: 'MagicBricks', sourceUrl: 'u-mb', priceDisplay: '₹34,500', price: 34500, accessibleLabel: 'l2' },
        ],
      }),
    );
    expect(el.querySelector('.sources-prefix')?.textContent).toContain('Also listed on');
    // One chip for the single additional (non-cheapest) source.
    expect(el.querySelectorAll('.chip').length).toBe(1);
    expect(el.querySelector('.chip')?.textContent).toContain('MagicBricks');
  });

  it('renders a single plain chip for a single-source group', () => {
    const el = render(group());
    expect(el.querySelector('.sources-prefix')).toBeNull();
    const chips = el.querySelectorAll('.chip');
    expect(chips.length).toBe(1);
    expect(chips[0].textContent).toContain('NoBroker');
  });

  it('falls back to a placeholder when there is no photo', () => {
    const el = render(group({ primaryPhotoUrl: null }));
    expect(el.querySelector('.photo img')).toBeNull();
    expect(el.querySelector('.photo-placeholder')).not.toBeNull();
  });
});
