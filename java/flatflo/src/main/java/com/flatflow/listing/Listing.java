package com.flatflow.listing;

import java.util.List;

/**
 * A single raw rental advertisement from one source platform (pre-grouping).
 *
 * <p>Identity is the combination of {@code sourcePlatform} + {@code sourceUrl},
 * which is stable across feed refreshes and is used as the redirect target.
 */
public record Listing(
        String id,
        String society,
        String locality,
        BhkType bhkType,
        int rentMin,
        int rentMax,
        int areaSqFt,
        AreaType areaType,
        Furnishing furnishing,
        List<String> amenities,
        List<String> nearby,
        ListingStatus status,
        String sourcePlatform,
        String sourceUrl,
        List<String> photos,
        Integer floor,
        String lastUpdated,
        double lat,
        double lng
) {
}
