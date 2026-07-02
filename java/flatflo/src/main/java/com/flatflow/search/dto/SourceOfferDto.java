package com.flatflow.search.dto;

/**
 * One source's offer as shown on a card chip (spec FR-012). {@code accessibleLabel}
 * satisfies the screen-reader requirement for chips (spec FR-022).
 */
public record SourceOfferDto(
        String sourcePlatform,
        String sourceUrl,
        String priceDisplay,
        int price,
        String accessibleLabel
) {
}
