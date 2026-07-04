import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SkeletonState } from './skeleton-state';

describe('SkeletonState', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SkeletonState] }).compileComponents();
  });

  function render(count?: number): HTMLElement {
    const fixture = TestBed.createComponent(SkeletonState);
    if (count != null) {
      (fixture.componentRef as ComponentRef<SkeletonState>).setInput('count', count);
    }
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders four placeholder cards by default', () => {
    const el = render();
    expect(el.querySelectorAll('.skeleton-card').length).toBe(4);
  });

  it('honors the count input', () => {
    const el = render(5);
    expect(el.querySelectorAll('.skeleton-card').length).toBe(5);
  });

  it('exposes a single status region with an accessible label', () => {
    const el = render();
    const status = el.querySelector('[role="status"]');
    expect(status).not.toBeNull();
    expect(status?.querySelector('.sr-only')?.textContent).toContain('Searching');
  });
});
