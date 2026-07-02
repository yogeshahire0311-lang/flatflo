package com.flatflow.grouping;

/**
 * One source platform's offer for a flat within a {@link ListingGroup}.
 * Carries the price and the deep link used when the tenant chooses this source.
 */
public record SourceOffer(
        String sourcePlatform,
        String sourceUrl,
        int price,
        String lastUpdated
) {
}
