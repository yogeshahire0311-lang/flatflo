package com.flatflow.search.dto;

import java.util.List;

/**
 * A grouped flat as rendered by the UI (spec FR-012). Display strings
 * ({@code priceDisplay}, {@code metaLine}) are formatted at the boundary in INR
 * and sq ft (spec FR-019). {@code bestDealDiscountPct} is present only when
 * {@code isBestDeal} is true. {@code newestUpdated} backs the "newest" sort (FR-009).
 */
public record ListingGroupDto(
        String groupId,
        String title,
        String metaLine,
        String locality,
        String priceDisplay,
        int cheapestPrice,
        boolean isBestDeal,
        Integer bestDealDiscountPct,
        String primaryPhotoUrl,
        String newestUpdated,
        List<SourceOfferDto> sources,
        boolean available
) {
}
