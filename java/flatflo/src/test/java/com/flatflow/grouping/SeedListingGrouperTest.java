package com.flatflow.grouping;

import static org.assertj.core.api.Assertions.assertThat;

import com.flatflow.listing.AreaType;
import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import com.flatflow.listing.Listing;
import com.flatflow.listing.ListingStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeedListingGrouperTest {

    private final SeedListingGrouper grouper = new SeedListingGrouper(new AreaAverageCalculator());

    private static Listing listing(
            String id, String society, String locality, BhkType bhk,
            int price, int areaSqFt, String source, String url, String lastUpdated) {
        return new Listing(
                id, society, locality, bhk, price, price, areaSqFt, AreaType.CARPET,
                Furnishing.SEMI_FURNISHED, List.of(), List.of(), ListingStatus.AVAILABLE,
                source, url, List.of("https://img/" + id + ".jpg"), 3, lastUpdated, 19.0, 72.0);
    }

    @Test
    void mergesSameFlatFromMultipleSourcesIntoOneGroup() {
        // Same flat (same locality+bhk+society, areas within one bucket), two sources.
        List<Listing> raw = List.of(
                listing("a-nb", "Siddharth Nagar", "Goregaon East", BhkType.TWO_BHK, 34500, 655, "MagicBricks", "u-mb", "2026-07-01"),
                listing("a-mb", "Siddharth Nagar", "Goregaon East", BhkType.TWO_BHK, 32000, 650, "NoBroker", "u-nb", "2026-06-30"));

        List<ListingGroup> groups = grouper.group(raw);

        assertThat(groups).hasSize(1);
        ListingGroup g = groups.get(0);
        assertThat(g.offers()).hasSize(2);
        // Offers sorted ascending by price; cheapest surfaced.
        assertThat(g.offers().get(0).price()).isEqualTo(32000);
        assertThat(g.offers().get(1).price()).isEqualTo(34500);
        assertThat(g.cheapestPrice()).isEqualTo(32000);
        // newestUpdated is the max lastUpdated across offers.
        assertThat(g.newestUpdated()).isEqualTo("2026-07-01");
    }

    @Test
    void keepsDistinctFlatsSeparate() {
        List<Listing> raw = List.of(
                listing("b1", "Alpha", "Malad West", BhkType.ONE_BHK, 24000, 420, "NoBroker", "u1", "2026-07-01"),
                listing("b2", "Beta", "Malad West", BhkType.ONE_BHK, 27000, 440, "MagicBricks", "u2", "2026-07-01"));

        assertThat(grouper.group(raw)).hasSize(2);
    }

    @Test
    void computesAreaAverageAndFlagsBestDealAtTenPercentBoundary() {
        // Two distinct single-source flats; average = 100000, threshold = 90000.
        List<Listing> raw = List.of(
                listing("x", "Xavier", "BoundaryTown", BhkType.TWO_BHK, 90000, 600, "NoBroker", "ux", "2026-07-01"),
                listing("y", "Yellow", "BoundaryTown", BhkType.TWO_BHK, 110000, 600, "Housing.com", "uy", "2026-07-01"));

        List<ListingGroup> groups = grouper.group(raw);
        ListingGroup x = groups.stream().filter(g -> g.cheapestPrice() == 90000).findFirst().orElseThrow();
        ListingGroup y = groups.stream().filter(g -> g.cheapestPrice() == 110000).findFirst().orElseThrow();

        assertThat(x.areaAveragePrice()).isEqualTo(100000);
        // Exactly at the 10% threshold counts as a best deal.
        assertThat(x.isBestDeal()).isTrue();
        assertThat(x.bestDealDiscountPct()).isEqualTo(10);
        // Above the average is never a best deal, and carries no discount.
        assertThat(y.isBestDeal()).isFalse();
        assertThat(y.bestDealDiscountPct()).isNull();
    }

    @Test
    void doesNotFlagBestDealJustAboveThreshold() {
        // average = 100000, threshold = 90000; cheapest 90001 is just above.
        List<Listing> raw = List.of(
                listing("p", "Papa", "EdgeTown", BhkType.TWO_BHK, 90001, 600, "NoBroker", "up", "2026-07-01"),
                listing("q", "Quebec", "EdgeTown", BhkType.TWO_BHK, 109999, 600, "Housing.com", "uq", "2026-07-01"));

        ListingGroup p = grouper.group(raw).stream()
                .filter(g -> g.cheapestPrice() == 90001).findFirst().orElseThrow();

        assertThat(p.areaAveragePrice()).isEqualTo(100000);
        assertThat(p.isBestDeal()).isFalse();
    }

    @Test
    void roundsDiscountPercentToNearestInteger() {
        // average = (32000+34500+52000+46000+44000)/5 = 41700; cheapest 32000.
        // discount = (41700-32000)/41700 * 100 = 23.26% -> 23
        List<Listing> raw = List.of(
                listing("a1", "Alpha", "PctTown", BhkType.TWO_BHK, 32000, 650, "NoBroker", "ua", "2026-07-01"),
                listing("b1", "Beta", "PctTown", BhkType.TWO_BHK, 34500, 650, "MagicBricks", "ub", "2026-07-01"),
                listing("c1", "Gamma", "PctTown", BhkType.TWO_BHK, 52000, 650, "Housing.com", "uc", "2026-07-01"),
                listing("d1", "Delta", "PctTown", BhkType.TWO_BHK, 46000, 650, "NoBroker", "ud", "2026-07-01"),
                listing("e1", "Epsilon", "PctTown", BhkType.TWO_BHK, 44000, 650, "MagicBricks", "ue", "2026-07-01"));

        ListingGroup cheapest = grouper.group(raw).stream()
                .filter(g -> g.cheapestPrice() == 32000).findFirst().orElseThrow();

        assertThat(cheapest.areaAveragePrice()).isEqualTo(41700);
        assertThat(cheapest.isBestDeal()).isTrue();
        assertThat(cheapest.bestDealDiscountPct()).isEqualTo(23);
    }
}
