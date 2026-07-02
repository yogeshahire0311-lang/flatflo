// Typed models mirroring the backend contract (see specs/.../contracts/search-api.md).

export type SortMode = 'BEST_DEAL' | 'PRICE_ASC' | 'PRICE_DESC' | 'NEWEST';

export type Bhk = 'ONE_BHK' | 'TWO_BHK' | 'THREE_BHK';

export type Furnishing = 'UNFURNISHED' | 'SEMI_FURNISHED' | 'FULLY_FURNISHED';

export interface Area {
  id: string;
  name: string;
}

export interface SourceOffer {
  sourcePlatform: string;
  sourceUrl: string;
  priceDisplay: string;
  price: number;
  accessibleLabel: string;
}

export interface ListingGroup {
  groupId: string;
  title: string;
  metaLine: string;
  locality: string;
  priceDisplay: string;
  cheapestPrice: number;
  isBestDeal: boolean;
  bestDealDiscountPct: number | null;
  primaryPhotoUrl: string | null;
  newestUpdated: string | null;
  sources: SourceOffer[];
  available: boolean;
}

export interface SourceStatus {
  sourcePlatform: string;
  reachable: boolean;
}

export interface SearchResponse {
  results: ListingGroup[];
  count: number;
  dupCount: number;
  sort: SortMode;
  page: number;
  pageSize: number;
  hasMore: boolean;
  sources: SourceStatus[];
}

/** The current search criteria held by the search page and reflected in the URL. */
export interface SearchCriteria {
  location: string;
  bhk: Bhk;
  budgetMin: number | null;
  budgetMax: number | null;
  furnishing: Furnishing | null;
  sort: SortMode;
  page: number;
}
