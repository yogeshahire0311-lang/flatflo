import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ErrorState } from './error-state';

describe('ErrorState', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ErrorState] }).compileComponents();
  });

  function create() {
    const fixture = TestBed.createComponent(ErrorState);
    return { fixture, ref: fixture.componentRef as ComponentRef<ErrorState> };
  }

  it('shows a default message and a retry button', () => {
    const { fixture } = create();
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('.message')?.textContent?.trim().length).toBeGreaterThan(0);
    expect(el.querySelector('button.retry')).not.toBeNull();
  });

  it('shows a custom message when provided', () => {
    const { fixture, ref } = create();
    ref.setInput('message', 'No listing sources could be reached.');
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('.message')?.textContent).toContain('No listing sources could be reached');
  });

  it('emits retry exactly once when the button is clicked', () => {
    const { fixture } = create();
    let retries = 0;
    fixture.componentInstance.retry.subscribe(() => retries++);
    fixture.detectChanges();

    (fixture.nativeElement as HTMLElement).querySelector('button.retry')!.dispatchEvent(new MouseEvent('click'));
    expect(retries).toBe(1);
  });
});
