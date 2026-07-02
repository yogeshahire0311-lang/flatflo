package com.flatflow.listing;

import java.util.List;

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
        String sourceUrl
) {
}
