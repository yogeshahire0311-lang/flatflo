import { TestBed } from '@angular/core/testing';
import { EmptyState } from './empty-state';

describe('EmptyState', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [EmptyState] }).compileComponents();
  });

  it('shows the no-matches heading and a widen suggestion', () => {
    const fixture = TestBed.createComponent(EmptyState);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('h2')?.textContent).toContain('No flats match these filters');
    expect(el.querySelector('p')?.textContent).toContain('widening');
  });
});
