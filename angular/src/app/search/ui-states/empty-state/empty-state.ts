import { Component } from '@angular/core';

/**
 * Shown when a search succeeds but matches no groups (FR-016). Distinct from the
 * error state: the request completed, there simply are no results — so we invite
 * the user to widen their filters rather than retry.
 */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [],
  templateUrl: './empty-state.html',
  styleUrl: './empty-state.css',
})
export class EmptyState {}
