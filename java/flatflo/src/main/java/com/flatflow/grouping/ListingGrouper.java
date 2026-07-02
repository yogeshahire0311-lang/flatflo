package com.flatflow.grouping;

import com.flatflow.listing.Listing;
import java.util.List;

/**
 * Turns raw {@link Listing}s into de-duplicated {@link ListingGroup}s
 * (spec FR-003). The grouping/duplicate-detection algorithm is a backend
 * concern hidden behind this seam, so a smarter matcher can replace the MVP
 * implementation without changing the API or UI.
 */
public interface ListingGrouper {

    /** Group raw listings into merged flats with cheapest price and best-deal signal. */
    List<ListingGroup> group(List<Listing> listings);
}
