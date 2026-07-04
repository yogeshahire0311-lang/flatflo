import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ResultsMeta } from './results-meta';
import { SortMode, SourceStatus } from '../models';

describe('ResultsMeta', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ResultsMeta] }).compileComponents();
  });

  function render(sources: SourceStatus[]): HTMLElement {
    const fixture = TestBed.createComponent(ResultsMeta);
    const ref = fixture.componentRef as ComponentRef<ResultsMeta>;
    ref.setInput('count', 5);
    ref.setInput('dupCount', 2);
    ref.setInput('sort', 'BEST_DEAL' as SortMode);
    ref.setInput('sources', sources);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('always renders the count/duplicates summary', () => {
    const el = render([{ sourcePlatform: 'SeedFeed', reachable: true }]);
    expect(el.querySelector('.summary')?.textContent).toContain('5 flats found, 2 duplicates merged');
  });

  it('shows "Results from N of M sources" when some sources are unreachable', () => {
    const el = render([
      { sourcePlatform: 'NoBroker', reachable: true },
      { sourcePlatform: 'MagicBricks', reachable: false },
    ]);
    expect(el.querySelector('.coverage')?.textContent).toContain('Results from 1 of 2 sources');
  });

  it('hides the coverage note when all sources are reachable', () => {
    const el = render([
      { sourcePlatform: 'NoBroker', reachable: true },
      { sourcePlatform: 'MagicBricks', reachable: true },
    ]);
    expect(el.querySelector('.coverage')).toBeNull();
  });

  it('hides the coverage note when no source status is provided', () => {
    const el = render([]);
    expect(el.querySelector('.coverage')).toBeNull();
  });
});
