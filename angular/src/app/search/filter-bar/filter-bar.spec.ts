import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FilterBar } from './filter-bar';
import { Area, SearchCriteria } from '../models';

const AREAS: Area[] = [{ id: 'GOREGAON_EAST', name: 'Goregaon East' }];

describe('FilterBar', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [FilterBar] }).compileComponents();
  });

  function render() {
    const fixture = TestBed.createComponent(FilterBar);
    (fixture.componentRef as ComponentRef<FilterBar>).setInput('areas', AREAS);
    fixture.detectChanges();
    return fixture;
  }

  it('emits the chosen furnishing in the criteria on submit', () => {
    const fixture = render();
    const el = fixture.nativeElement as HTMLElement;
    let emitted: SearchCriteria | undefined;
    fixture.componentInstance.search.subscribe((c) => (emitted = c));

    // A location is required before submit does anything; the first select is Location.
    const location = el.querySelector('select') as HTMLSelectElement;
    location.value = 'GOREGAON_EAST';
    location.dispatchEvent(new Event('change'));
    // Furnishing is the select inside the "Furnishing" field label.
    const furnishingSelect = Array.from(el.querySelectorAll('.field')).find((f) =>
      f.querySelector('span')?.textContent?.includes('Furnishing'),
    )!.querySelector('select') as HTMLSelectElement;
    furnishingSelect.value = 'FULLY_FURNISHED';
    furnishingSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    (el.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));

    expect(emitted?.furnishing).toBe('FULLY_FURNISHED');
    expect(emitted?.location).toBe('GOREGAON_EAST');
  });

  it('shows the active-filter badge with the correct count, hidden at zero', () => {
    const fixture = render();
    const el = fixture.nativeElement as HTMLElement;

    // No refinement filters yet -> no badge.
    expect(el.querySelector('.filter-badge')).toBeNull();

    // Set budget min + furnishing -> count is 2.
    const min = Array.from(el.querySelectorAll('.field')).find((f) =>
      f.querySelector('span')?.textContent?.includes('Budget min'),
    )!.querySelector('input') as HTMLInputElement;
    min.value = '30000';
    min.dispatchEvent(new Event('input'));

    const furnishingSelect = Array.from(el.querySelectorAll('.field')).find((f) =>
      f.querySelector('span')?.textContent?.includes('Furnishing'),
    )!.querySelector('select') as HTMLSelectElement;
    furnishingSelect.value = 'SEMI_FURNISHED';
    furnishingSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(el.querySelector('.filter-badge')?.textContent).toContain('2');
  });
});
