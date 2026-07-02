import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Area, SearchCriteria, SearchResponse } from './models';

/** Talks to the backend search API (`/api/areas`, `/api/search`). */
@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly http = inject(HttpClient);

  getAreas(): Observable<{ areas: Area[] }> {
    return this.http.get<{ areas: Area[] }>('/api/areas');
  }

  search(criteria: SearchCriteria): Observable<SearchResponse> {
    let params = new HttpParams()
      .set('location', criteria.location)
      .set('bhk', criteria.bhk)
      .set('sort', criteria.sort)
      .set('page', String(criteria.page));

    if (criteria.budgetMin != null) {
      params = params.set('budgetMin', String(criteria.budgetMin));
    }
    if (criteria.budgetMax != null) {
      params = params.set('budgetMax', String(criteria.budgetMax));
    }
    if (criteria.furnishing != null) {
      params = params.set('furnishing', criteria.furnishing);
    }

    return this.http.get<SearchResponse>('/api/search', { params });
  }
}
