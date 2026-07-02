package com.flatflow.grouping;

import com.flatflow.listing.BhkType;
import com.flatflow.listing.Listing;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Computes the average rent per (locality, BHK) across a set of raw listings.
 * Used to decide whether a group's cheapest price qualifies as a "best deal"
 * (spec FR-007). For the MVP the average is derived from the seeded feed itself.
 */
@Component
public class AreaAverageCalculator {

    /** Key uniquely identifying an area segment for averaging. */
    public record AreaKey(String locality, BhkType bhk) {
        public static AreaKey of(Listing l) {
            return new AreaKey(l.locality(), l.bhkType());
        }
    }

    /**
     * Mean {@code rentMin} per (locality, BHK), rounded to the nearest rupee.
     * Every raw listing contributes (not de-duplicated), which reflects the
     * market rate rather than the merged-card count.
     */
    public Map<AreaKey, Integer> averagesByArea(List<Listing> listings) {
        return listings.stream()
                .collect(Collectors.groupingBy(
                        AreaKey::of,
                        Collectors.averagingInt(Listing::rentMin)))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (int) Math.round(e.getValue())));
    }
}
