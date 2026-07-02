package com.flatflow.grouping;

import com.flatflow.grouping.AreaAverageCalculator.AreaKey;
import com.flatflow.listing.BhkType;
import com.flatflow.listing.Listing;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * MVP {@link ListingGrouper}: groups raw listings by a deterministic dedup key
 * (normalized locality + BHK + area bucket + society), then decorates each group
 * with cheapest price, the locality+BHK area average, and the best-deal signal
 * (cheapest at least 10% below the average — spec FR-007).
 *
 * <p>The dedup key is intentionally simple and deterministic; a fuzzy/geo matcher
 * can replace this behind the {@link ListingGrouper} seam without API/UI changes.
 */
@Component
public class SeedListingGrouper implements ListingGrouper {

    /** Area tolerance (sq ft) for treating two listings as the same flat. */
    private static final int AREA_BUCKET = 25;

    /** A group's cheapest price qualifies as a best deal at or below this share of the area average. */
    private static final double BEST_DEAL_RATIO = 0.90;

    private final AreaAverageCalculator areaAverageCalculator;

    public SeedListingGrouper(AreaAverageCalculator areaAverageCalculator) {
        this.areaAverageCalculator = areaAverageCalculator;
    }

    @Override
    public List<ListingGroup> group(List<Listing> listings) {
        Map<AreaKey, Integer> areaAverages = areaAverageCalculator.averagesByArea(listings);

        // Preserve encounter order for stable, testable output.
        Map<String, List<Listing>> byKey = new LinkedHashMap<>();
        for (Listing l : listings) {
            byKey.computeIfAbsent(dedupKey(l), k -> new ArrayList<>()).add(l);
        }

        List<ListingGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Listing>> entry : byKey.entrySet()) {
            groups.add(toGroup(entry.getKey(), entry.getValue(), areaAverages));
        }
        return groups;
    }

    private ListingGroup toGroup(String groupId, List<Listing> members, Map<AreaKey, Integer> areaAverages) {
        List<SourceOffer> offers = members.stream()
                .map(l -> new SourceOffer(l.sourcePlatform(), l.sourceUrl(), l.rentMin(), l.lastUpdated()))
                .sorted(Comparator.comparingInt(SourceOffer::price))
                .toList();

        int cheapestPrice = offers.get(0).price();

        // The representative listing is the cheapest offer's source listing.
        Listing representative = members.stream()
                .min(Comparator.comparingInt(Listing::rentMin))
                .orElse(members.get(0));

        int areaAverage = areaAverages.getOrDefault(AreaKey.of(representative), cheapestPrice);
        boolean isBestDeal = cheapestPrice <= areaAverage * BEST_DEAL_RATIO;
        Integer discountPct = isBestDeal
                ? (int) Math.round((areaAverage - cheapestPrice) * 100.0 / areaAverage)
                : null;

        String newestUpdated = members.stream()
                .map(Listing::lastUpdated)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String primaryPhoto = (representative.photos() == null || representative.photos().isEmpty())
                ? null
                : representative.photos().get(0);

        return new ListingGroup(
                groupId,
                title(representative),
                representative.locality(),
                representative.areaSqFt(),
                representative.bhkType(),
                representative.furnishing(),
                representative.floor(),
                representative.lat(),
                representative.lng(),
                offers,
                cheapestPrice,
                areaAverage,
                isBestDeal,
                discountPct,
                primaryPhoto,
                newestUpdated);
    }

    private static String title(Listing l) {
        return "%s in %s".formatted(bhkLabel(l.bhkType()), l.locality());
    }

    static String bhkLabel(BhkType bhk) {
        return switch (bhk) {
            case ONE_BHK -> "1 BHK";
            case TWO_BHK -> "2 BHK";
            case THREE_BHK -> "3 BHK";
        };
    }

    private static String dedupKey(Listing l) {
        String society = l.society() == null ? "" : l.society().trim().toLowerCase();
        int areaBucket = l.areaSqFt() / AREA_BUCKET;
        return String.join("|",
                normalize(l.locality()),
                l.bhkType().name(),
                society,
                String.valueOf(areaBucket));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
