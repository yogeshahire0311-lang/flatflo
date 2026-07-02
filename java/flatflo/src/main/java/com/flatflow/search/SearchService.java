package com.flatflow.search;

import com.flatflow.grouping.ListingGroup;
import com.flatflow.grouping.ListingGrouper;
import com.flatflow.grouping.SourceOffer;
import com.flatflow.listing.ListingSource;
import com.flatflow.search.dto.ListingGroupDto;
import com.flatflow.search.dto.SearchResponseDto;
import com.flatflow.search.dto.SourceOfferDto;
import com.flatflow.search.dto.SourceStatusDto;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Runs a search over the aggregated feed: takes grouped listings from the
 * {@link ListingGrouper}, filters them, orders them by the requested
 * {@link SortMode}, paginates, and maps to boundary DTOs. Stateless.
 */
@Service
public class SearchService {

    static final int PAGE_SIZE = 20;

    private final ListingSource listingSource;
    private final ListingGrouper grouper;

    public SearchService(ListingSource listingSource, ListingGrouper grouper) {
        this.listingSource = listingSource;
        this.grouper = grouper;
    }

    public SearchResponseDto search(SearchQuery query) {
        List<ListingGroup> all = grouper.group(listingSource.findAll());

        List<ListingGroup> matched = all.stream()
                .filter(g -> g.locality().equalsIgnoreCase(query.location().displayName()))
                .filter(g -> g.bhk() == query.bhk())
                .filter(g -> withinBudget(g, query))
                .filter(g -> query.furnishing() == null || g.furnishing() == query.furnishing())
                .sorted(comparatorFor(query.sort()))
                .toList();

        int count = matched.size();
        int dupCount = matched.stream().mapToInt(g -> g.offers().size()).sum() - count;

        int from = Math.min(query.page() * PAGE_SIZE, count);
        int to = Math.min(from + PAGE_SIZE, count);
        List<ListingGroupDto> pageResults = matched.subList(from, to).stream()
                .map(SearchService::toDto)
                .toList();

        boolean hasMore = to < count;

        // The MVP has a single always-available seeded source.
        List<SourceStatusDto> sourceStatus = List.of(new SourceStatusDto("SeedFeed", true));

        return new SearchResponseDto(
                pageResults, count, dupCount, query.sort().name(),
                query.page(), PAGE_SIZE, hasMore, sourceStatus);
    }

    /**
     * A group is in budget when its cheapest offer sits within the requested
     * bounds; an absent bound is unconstrained (FR-013).
     */
    private static boolean withinBudget(ListingGroup g, SearchQuery query) {
        int price = g.cheapestPrice();
        return (query.budgetMin() == null || price >= query.budgetMin())
                && (query.budgetMax() == null || price <= query.budgetMax());
    }

    private static Comparator<ListingGroup> comparatorFor(SortMode sort) {
        return switch (sort) {
            case BEST_DEAL -> Comparator
                    .comparing(ListingGroup::isBestDeal, Comparator.reverseOrder())
                    .thenComparing(g -> g.bestDealDiscountPct() == null ? 0 : g.bestDealDiscountPct(), Comparator.reverseOrder())
                    .thenComparingInt(ListingGroup::cheapestPrice);
            case PRICE_ASC -> Comparator.comparingInt(ListingGroup::cheapestPrice);
            case PRICE_DESC -> Comparator.comparingInt(ListingGroup::cheapestPrice).reversed();
            case NEWEST -> Comparator.comparing(
                    ListingGroup::newestUpdated,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    static ListingGroupDto toDto(ListingGroup g) {
        List<SourceOfferDto> offers = g.offers().stream()
                .map(SearchService::toOfferDto)
                .toList();

        return new ListingGroupDto(
                g.groupId(),
                g.title(),
                metaLine(g),
                g.locality(),
                inr(g.cheapestPrice()),
                g.cheapestPrice(),
                g.isBestDeal(),
                g.bestDealDiscountPct(),
                g.primaryPhoto(),
                g.newestUpdated(),
                offers,
                true);
    }

    private static SourceOfferDto toOfferDto(SourceOffer o) {
        String priceDisplay = inr(o.price());
        String label = "View this listing on %s, %s per month".formatted(o.sourcePlatform(), priceDisplay);
        return new SourceOfferDto(o.sourcePlatform(), o.sourceUrl(), priceDisplay, o.price(), label);
    }

    private static String metaLine(ListingGroup g) {
        String base = "%d sq ft · %s".formatted(g.areaSqFt(), furnishingLabel(g.furnishing().name()));
        return g.floor() == null ? base : base + " · floor " + g.floor();
    }

    private static String furnishingLabel(String furnishing) {
        return switch (furnishing) {
            case "UNFURNISHED" -> "Unfurnished";
            case "SEMI_FURNISHED" -> "Semi-furnished";
            case "FULLY_FURNISHED" -> "Fully furnished";
            default -> furnishing;
        };
    }

    /** Format an INR amount with thousands grouping, e.g. 32000 to "Rs 32,000" style "₹32,000". */
    static String inr(int amount) {
        return "₹" + String.format("%,d", amount);
    }
}
