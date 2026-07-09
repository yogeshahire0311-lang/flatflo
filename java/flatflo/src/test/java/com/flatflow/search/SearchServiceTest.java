package com.flatflow.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.flatflow.grouping.AreaAverageCalculator;
import com.flatflow.grouping.SeedListingGrouper;
import com.flatflow.listing.AreaType;
import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import com.flatflow.listing.Listing;
import com.flatflow.listing.ListingSource;
import com.flatflow.listing.ListingStatus;
import com.flatflow.listing.SupportedArea;
import com.flatflow.search.dto.ListingGroupDto;
import com.flatflow.search.dto.SearchResponseDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchServiceTest {

    private static Listing listing(String id, String society, String locality, BhkType bhk,
            int price, int areaSqFt, String source) {
        return listing(id, society, locality, bhk, price, areaSqFt, source,
                Furnishing.SEMI_FURNISHED, "2026-07-01");
    }

    private static Listing listing(String id, String society, String locality, BhkType bhk,
            int price, int areaSqFt, String source, Furnishing furnishing, String lastUpdated) {
        return new Listing(id, society, locality, bhk, price, price, areaSqFt, AreaType.CARPET,
                furnishing, List.of(), List.of(), ListingStatus.AVAILABLE,
                source, "https://" + source + "/" + id,
                List.of("https://img/" + id + ".jpg"), 3, lastUpdated, 19.0, 72.0);
    }

    private SearchService serviceFor(List<Listing> listings) {
        ListingSource source = () -> listings;
        SourceAggregator aggregator = new SourceAggregator(
                List.of(new SourceAggregator.NamedSource("SeedFeed", source)), Duration.ofSeconds(8));
        return new SearchService(aggregator, new SeedListingGrouper(new AreaAverageCalculator()));
    }

    private SearchQuery query(BhkType bhk) {
        return new SearchQuery(SupportedArea.GOREGAON_EAST, null, null, bhk, null, SortMode.BEST_DEAL, 0);
    }

    private SearchQuery query(Integer budgetMin, Integer budgetMax, Furnishing furnishing, SortMode sort) {
        return new SearchQuery(
                SupportedArea.GOREGAON_EAST, budgetMin, budgetMax, BhkType.TWO_BHK, furnishing, sort, 0);
    }

    /** Cheapest prices of the returned groups, in result order, for concise ordering assertions. */
    private static List<Integer> prices(SearchResponseDto res) {
        return res.results().stream().map(ListingGroupDto::cheapestPrice).toList();
    }

    @Test
    void filtersByLocationAndBhkAndMergesDuplicates() {
        List<Listing> listings = List.of(
                // Same flat, two sources -> one group, dupCount contribution 1.
                listing("a1", "Siddharth", "Goregaon East", BhkType.TWO_BHK, 32000, 650, "NoBroker"),
                listing("a2", "Siddharth", "Goregaon East", BhkType.TWO_BHK, 34500, 650, "MagicBricks"),
                // Different flat, same area/bhk.
                listing("b1", "Oberoi", "Goregaon East", BhkType.TWO_BHK, 52000, 720, "Housing.com"),
                // Different BHK -> excluded.
                listing("c1", "Link", "Goregaon East", BhkType.ONE_BHK, 27000, 440, "NoBroker"),
                // Different locality -> excluded.
                listing("d1", "Lake", "Thane", BhkType.TWO_BHK, 40000, 900, "NoBroker"));

        SearchResponseDto res = serviceFor(listings).search(query(BhkType.TWO_BHK));

        assertThat(res.count()).isEqualTo(2);           // 2 groups match Goregaon East + 2BHK
        assertThat(res.dupCount()).isEqualTo(1);        // 3 offers across 2 groups - 2 = 1
        assertThat(res.results()).hasSize(2);
        assertThat(res.sort()).isEqualTo("BEST_DEAL");
        assertThat(res.pageSize()).isEqualTo(20);
    }

    @Test
    void paginatesInPagesOfTwenty() {
        List<Listing> listings = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            // Distinct societies -> 25 separate groups.
            listings.add(listing("g" + i, "Soc" + i, "Goregaon East", BhkType.TWO_BHK,
                    30000 + i * 100, 600, "NoBroker"));
        }
        SearchService service = serviceFor(listings);

        SearchResponseDto page0 = service.search(query(BhkType.TWO_BHK));
        assertThat(page0.results()).hasSize(20);
        assertThat(page0.count()).isEqualTo(25);
        assertThat(page0.hasMore()).isTrue();

        SearchQuery page1Query = new SearchQuery(
                SupportedArea.GOREGAON_EAST, null, null, BhkType.TWO_BHK, null, SortMode.BEST_DEAL, 1);
        SearchResponseDto page1 = service.search(page1Query);
        assertThat(page1.results()).hasSize(5);
        assertThat(page1.hasMore()).isFalse();
    }

    @Test
    void returnsEmptyStateWhenNoMatches() {
        List<Listing> listings = List.of(
                listing("d1", "Lake", "Thane", BhkType.TWO_BHK, 40000, 900, "NoBroker"));

        SearchResponseDto res = serviceFor(listings).search(query(BhkType.TWO_BHK));

        assertThat(res.results()).isEmpty();
        assertThat(res.count()).isZero();
        assertThat(res.dupCount()).isZero();
        assertThat(res.hasMore()).isFalse();
    }

    @Test
    void filtersByBudgetRange() {
        // Three distinct 2BHK groups in Goregaon East at 30k / 40k / 50k.
        List<Listing> listings = List.of(
                listing("p1", "Alpha", "Goregaon East", BhkType.TWO_BHK, 30000, 600, "NoBroker"),
                listing("p2", "Beta", "Goregaon East", BhkType.TWO_BHK, 40000, 600, "NoBroker"),
                listing("p3", "Gamma", "Goregaon East", BhkType.TWO_BHK, 50000, 600, "NoBroker"));
        SearchService service = serviceFor(listings);

        // Min only: drop the 30k flat.
        assertThat(prices(service.search(query(35000, null, null, SortMode.PRICE_ASC))))
                .containsExactly(40000, 50000);
        // Max only: drop the 50k flat.
        assertThat(prices(service.search(query(null, 45000, null, SortMode.PRICE_ASC))))
                .containsExactly(30000, 40000);
        // Both bounds: keep only the middle flat.
        assertThat(prices(service.search(query(35000, 45000, null, SortMode.PRICE_ASC))))
                .containsExactly(40000);
    }

    @Test
    void filtersByFurnishing() {
        List<Listing> listings = List.of(
                listing("f1", "Alpha", "Goregaon East", BhkType.TWO_BHK, 30000, 600, "NoBroker",
                        Furnishing.UNFURNISHED, "2026-07-01"),
                listing("f2", "Beta", "Goregaon East", BhkType.TWO_BHK, 40000, 600, "NoBroker",
                        Furnishing.FULLY_FURNISHED, "2026-07-01"));
        SearchService service = serviceFor(listings);

        assertThat(prices(service.search(query(null, null, Furnishing.FULLY_FURNISHED, SortMode.PRICE_ASC))))
                .containsExactly(40000);
        // No furnishing filter -> both groups returned.
        assertThat(prices(service.search(query(null, null, null, SortMode.PRICE_ASC))))
                .containsExactly(30000, 40000);
    }

    @Test
    void sortsByPriceAscDescAndNewest() {
        List<Listing> listings = List.of(
                listing("s1", "Alpha", "Goregaon East", BhkType.TWO_BHK, 50000, 600, "NoBroker",
                        Furnishing.SEMI_FURNISHED, "2026-06-01"),
                listing("s2", "Beta", "Goregaon East", BhkType.TWO_BHK, 30000, 600, "NoBroker",
                        Furnishing.SEMI_FURNISHED, "2026-07-15"),
                listing("s3", "Gamma", "Goregaon East", BhkType.TWO_BHK, 40000, 600, "NoBroker",
                        Furnishing.SEMI_FURNISHED, "2026-05-20"));
        SearchService service = serviceFor(listings);

        assertThat(prices(service.search(query(null, null, null, SortMode.PRICE_ASC))))
                .containsExactly(30000, 40000, 50000);
        assertThat(prices(service.search(query(null, null, null, SortMode.PRICE_DESC))))
                .containsExactly(50000, 40000, 30000);
        // NEWEST orders by lastUpdated desc: s2 (07-15), s1 (06-01), s3 (05-20).
        assertThat(prices(service.search(query(null, null, null, SortMode.NEWEST))))
                .containsExactly(30000, 50000, 40000);
    }

    @Test
    void bestDealSortSurfacesBestDealsFirstThenDiscountDescThenPriceAsc() {
        // Six distinct 2BHK groups in Goregaon East. Every raw listing contributes to the
        // area average: (42+48+66+66+66+72)k / 6 = 60,000 -> best-deal threshold 54,000.
        //   42k -> best deal, 30% below avg   |   48k -> best deal, 20% below avg
        //   66k / 66k / 66k / 72k -> not best deals
        List<Listing> listings = List.of(
                listing("d1", "Alpha", "Goregaon East", BhkType.TWO_BHK, 66000, 600, "NoBroker"),
                listing("d2", "Beta", "Goregaon East", BhkType.TWO_BHK, 48000, 600, "NoBroker"),
                listing("d3", "Gamma", "Goregaon East", BhkType.TWO_BHK, 72000, 600, "NoBroker"),
                listing("d4", "Delta", "Goregaon East", BhkType.TWO_BHK, 42000, 600, "NoBroker"),
                listing("d5", "Epsilon", "Goregaon East", BhkType.TWO_BHK, 66000, 600, "NoBroker"),
                listing("d6", "Zeta", "Goregaon East", BhkType.TWO_BHK, 66000, 600, "NoBroker"));

        SearchResponseDto res = serviceFor(listings).search(query(BhkType.TWO_BHK));

        // Best deals first (42k before 48k = higher discount first), then the rest by price asc.
        assertThat(prices(res)).containsExactly(42000, 48000, 66000, 66000, 66000, 72000);

        // The two leaders are flagged with the correct discount %; the rest are not best deals.
        assertThat(res.results().get(0).isBestDeal()).isTrue();
        assertThat(res.results().get(0).bestDealDiscountPct()).isEqualTo(30);
        assertThat(res.results().get(1).isBestDeal()).isTrue();
        assertThat(res.results().get(1).bestDealDiscountPct()).isEqualTo(20);
        assertThat(res.results().get(2).isBestDeal()).isFalse();
        assertThat(res.results().get(2).bestDealDiscountPct()).isNull();
    }

    @Test
    void formatsPriceAsInr() {
        assertThat(SearchService.inr(32000)).isEqualTo("₹32,000");
        assertThat(SearchService.inr(9000)).isEqualTo("₹9,000");
    }
}
