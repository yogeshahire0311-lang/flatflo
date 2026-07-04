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

  it('renders title, price, "per month" caption, and the cheapest source name', () => {
    const el = render(group());
    expect(el.querySelector('.title')?.textContent).toContain('2 BHK in Goregaon East');
    expect(el.querySelector('.price .amount')?.textContent).toContain('₹32,000');
    const caption = el.querySelector('.price .caption')?.textContent;
    expect(caption).toContain('per month');
    // The primary price is captioned with its source (Option 2).
    expect(caption).toContain('from');
    expect(el.querySelector('.price .caption .src')?.textContent).toContain('NoBroker');
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

  it('shows no source chips for a single-source group (the source is in the price caption)', () => {
    const el = render(group());
    expect(el.querySelector('.sources-prefix')).toBeNull();
    // Single source is conveyed by the "from {source}" caption, not a redundant chip.
    expect(el.querySelectorAll('.chip').length).toBe(0);
    expect(el.querySelector('.price .caption .src')?.textContent).toContain('NoBroker');
  });

  it('falls back to a placeholder when there is no photo', () => {
    const el = render(group({ primaryPhotoUrl: null }));
    expect(el.querySelector('.photo img')).toBeNull();
    expect(el.querySelector('.photo-placeholder')).not.toBeNull();
  });

  it('falls back to the placeholder when the photo URL fails to load (T050)', () => {
    const fixture = TestBed.createComponent(ListingCard);
    (fixture.componentRef as ComponentRef<ListingCard>).setInput(
      'group',
      group({ primaryPhotoUrl: 'https://broken.example.com/missing.jpg' }),
    );
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    // Initially the img renders; simulate a load failure.
    const img = el.querySelector('.photo img') as HTMLImageElement;
    expect(img).not.toBeNull();
    img.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    // Now it shows the placeholder, never a broken image.
    expect(el.querySelector('.photo img')).toBeNull();
    expect(el.querySelector('.photo-placeholder')).not.toBeNull();
  });

  it('opens the cheapest source in a new tab when the card body is clicked (US2)', () => {
    const openSpy = spyOn(window, 'open');
    const el = render(
      group({
        sources: [
          { sourcePlatform: 'NoBroker', sourceUrl: 'u-nb', priceDisplay: '₹32,000', price: 32000, accessibleLabel: 'l1' },
          { sourcePlatform: 'MagicBricks', sourceUrl: 'u-mb', priceDisplay: '₹34,500', price: 34500, accessibleLabel: 'l2' },
        ],
      }),
    );

    (el.querySelector('.listing-card') as HTMLElement).click();

    expect(openSpy).toHaveBeenCalledWith('u-nb', '_blank', 'noopener');
  });

  it('opens a specific source in a new tab when its chip is clicked, not the card default (US2)', () => {
    const openSpy = spyOn(window, 'open');
    const el = render(
      group({
        sources: [
          { sourcePlatform: 'NoBroker', sourceUrl: 'u-nb', priceDisplay: '₹32,000', price: 32000, accessibleLabel: 'l1' },
          { sourcePlatform: 'MagicBricks', sourceUrl: 'u-mb', priceDisplay: '₹34,500', price: 34500, accessibleLabel: 'l2' },
        ],
      }),
    );

    const chip = el.querySelector('a.chip') as HTMLAnchorElement;
    // The chip carries its own source URL as a real href (middle/ctrl-click friendly).
    expect(chip.getAttribute('href')).toBe('u-mb');
    expect(chip.getAttribute('target')).toBe('_blank');

    // A left-click opens that chip's source (not the card default) in a new tab.
    chip.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(openSpy).toHaveBeenCalledWith('u-mb', '_blank', 'noopener');
    expect(openSpy).not.toHaveBeenCalledWith('u-nb', '_blank', 'noopener');
  });

  it('shows the best-deal badge, "% below area average" subtext, and accent border when isBestDeal (US3)', () => {
    const el = render(group({ isBestDeal: true, bestDealDiscountPct: 14 }));

    expect(el.querySelector('.badge')?.textContent).toContain('Best deal');
    expect(el.querySelector('.best-deal-subtext')?.textContent).toContain('14% below area average');
    // Accent border is applied via the best-deal class on the card.
    expect((el.querySelector('.listing-card') as HTMLElement).classList).toContain('best-deal');
  });

  it('shows no badge, subtext, or accent border for a non-best-deal group (US3)', () => {
    const el = render(group({ isBestDeal: false, bestDealDiscountPct: null }));

    expect(el.querySelector('.badge')).toBeNull();
    expect(el.querySelector('.best-deal-subtext')).toBeNull();
    expect((el.querySelector('.listing-card') as HTMLElement).classList).not.toContain('best-deal');
  });

  it('conveys the best deal in the card accessible label as text (US3, FR-022)', () => {
    const el = render(group({ isBestDeal: true, bestDealDiscountPct: 14 }));
    const label = (el.querySelector('.listing-card') as HTMLElement).getAttribute('aria-label') ?? '';
    expect(label).toContain('best deal');
    expect(label).toContain('14% below area average');
  });

  it('exposes the card as a keyboard-focusable link that activates on Enter (US2)', () => {
    const openSpy = spyOn(window, 'open');
    const el = render(group());
    const card = el.querySelector('.listing-card') as HTMLElement;

    expect(card.getAttribute('role')).toBe('link');
    expect(card.getAttribute('tabindex')).toBe('0');

    card.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(openSpy).toHaveBeenCalledWith('u-nb', '_blank', 'noopener');
  });
});
