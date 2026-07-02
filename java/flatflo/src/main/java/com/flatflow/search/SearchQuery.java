package com.flatflow.search;

import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import com.flatflow.listing.SupportedArea;

/**
 * A tenant's search request. {@code budgetMin}/{@code budgetMax} and
 * {@code furnishing} are optional; {@code sort} defaults to
 * {@link SortMode#BEST_DEAL} and {@code page} is zero-based.
 */
public record SearchQuery(
        SupportedArea location,
        Integer budgetMin,
        Integer budgetMax,
        BhkType bhk,
        Furnishing furnishing,
        SortMode sort,
        int page
) {
    public SearchQuery {
        if (sort == null) {
            sort = SortMode.BEST_DEAL;
        }
        if (page < 0) {
            page = 0;
        }
    }
}
