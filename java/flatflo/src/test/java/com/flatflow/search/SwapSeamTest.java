package com.flatflow.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.flatflow.grouping.ListingGroup;
import com.flatflow.grouping.ListingGrouper;
import com.flatflow.grouping.SourceOffer;
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
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves the two backend seams (spec FR-002 source, FR-003 grouper) are
 * replaceable without touching {@link SearchService}, {@link SearchController},
 * or the boundary DTOs. Here both are swapped for stand-ins that bear no
 * relation to the seeded feed or the real dedup algorithm — an alternate
 * {@link ListingSource} feeding raw listings and an alternate
 * {@link ListingGrouper} that hand-builds the groups — and the same search flow
 * still produces a well-formed {@link SearchResponseDto}.
 */
class SwapSeamTest {

    private static Listing rawListing(String id) {
        // Content is irrelevant: the alternate grouper below ignores it entirely,
        // standing in for a future matcher with its own algorithm.
        return new Listing(id, "Society " + id, "Andheri West", BhkType.ONE_BHK, 999, 999, 100,
                AreaType.CARPET, Furnishing.UNFURNISHED, List.of(), List.of(), ListingStatus.AVAILABLE,
                "AltSource", "https://alt/" + id, List.of(), 1, "2026-07-01", 0.0, 0.0);
    }

    /** An alternate grouper that emits a fixed group regardless of its input. */
    private static ListingGrouper fixedGrouper() {
        return listings -> List.of(new ListingGroup(
                "grp-1", "Sea View Apartments", "Goregaon East", 720, BhkType.TWO_BHK,
                Furnishing.SEMI_FURNISHED, 4, 19.16, 72.85,
                List.of(
                        new SourceOffer("AltA", "https://alta/1", 42000, "2026-07-02"),
                        new SourceOffer("AltB", "https://altb/1", 45000, "2026-07-01")),
                42000, 50000, true, 16, "https://img/1.jpg", "2026-07-02"));
    }

    @Test
    void alternateSourceAndGrouperFlowThroughUnchanged() {
        // Seam 1: an arbitrary alternate source (not the seeded feed).
        ListingSource altSource = () -> List.of(rawListing("x1"), rawListing("x2"));
        SourceAggregator aggregator = new SourceAggregator(
                List.of(new SourceAggregator.NamedSource("AltSource", altSource)),
                Duration.ofSeconds(2));

        // Seam 2: an arbitrary alternate grouper (not SeedListingGrouper).
        SearchService service = new SearchService(aggregator, fixedGrouper());

        SearchQuery query = new SearchQuery(
                SupportedArea.GOREGAON_EAST, null, null, BhkType.TWO_BHK, null, SortMode.BEST_DEAL, 0);

        SearchResponseDto response = service.search(query);

        // Same DTO shape, no controller/DTO change required for either swap.
        assertThat(response.count()).isEqualTo(1);
        assertThat(response.dupCount()).isEqualTo(1); // 2 offers merged into 1 group
        assertThat(response.results()).hasSize(1);
        ListingGroupDto group = response.results().get(0);
        assertThat(group.cheapestPrice()).isEqualTo(42000);
        assertThat(group.isBestDeal()).isTrue();
        assertThat(response.sources())
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.sourcePlatform()).isEqualTo("AltSource");
                    assertThat(s.reachable()).isTrue();
                });
    }
}
