import { Component, input, output } from '@angular/core';

/**
 * Full-width error with a retry action (FR-017), shown when the request itself
 * fails — notably a 503 when all listing sources are unreachable. Distinct from
 * the empty state (which means the request succeeded with zero matches).
 */
@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [],
  templateUrl: './error-state.html',
  styleUrl: './error-state.css',
})
export class ErrorState {
  /** Message to display; the page passes the server message for a 503. */
  readonly message = input<string>('Something went wrong — try again.');

  /** Emitted when the user clicks retry; the page re-runs the last search. */
  readonly retry = output<void>();
}
