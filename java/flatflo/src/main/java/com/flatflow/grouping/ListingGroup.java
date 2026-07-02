package com.flatflow.grouping;

import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import java.util.List;

/**
 * A set of raw listings judged to be the same physical flat, merged into one
 * result — this is what the UI renders (spec FR-003).
 *
 * <p>{@code offers} are sorted ascending by price; {@code cheapestPrice} is the
 * first offer's price. {@code isBestDeal} is true only when the cheapest price
 * is at least 10% below the locality+BHK {@code areaAveragePrice} (spec FR-007);
 * {@code bestDealDiscountPct} is populated only when {@code isBestDeal} is true.
 */
public record ListingGroup(
        String groupId,
        String title,
        String locality,
        int areaSqFt,
        BhkType bhk,
        Furnishing furnishing,
        Integer floor,
        double lat,
        double lng,
        List<SourceOffer> offers,
        int cheapestPrice,
        int areaAveragePrice,
        boolean isBestDeal,
        Integer bestDealDiscountPct,
        String primaryPhoto,
        String newestUpdated
) {
}
