package com.flatflow.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flatflow.listing.AreaType;
import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import com.flatflow.listing.Listing;
import com.flatflow.listing.ListingSource;
import com.flatflow.listing.ListingStatus;
import com.flatflow.search.SourceAggregator.Aggregation;
import com.flatflow.search.SourceAggregator.NamedSource;
import com.flatflow.search.SourceAggregator.SourceStatus;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the aggregation seam (FR-021): a source that answers within the
 * time budget is merged and reported reachable; one that is slow or throws is
 * dropped and reported unreachable; if every source fails the aggregator raises
 * {@link AllSourcesUnavailableException} (→ HTTP 503) rather than returning empty.
 */
class SourceAggregatorTest {

    private static Listing listing(String id, String source) {
        return new Listing(id, "Society " + id, "Goregaon East", BhkType.TWO_BHK, 50000, 50000, 700,
                AreaType.CARPET, Furnishing.SEMI_FURNISHED, List.of(), List.of(), ListingStatus.AVAILABLE,
                source, "https://" + source + "/" + id, List.of(), 3, "2026-07-01", 19.0, 72.0);
    }

    /** A source that blocks past any reasonable timeout, standing in for an unresponsive feed. */
    private static ListingSource slowSource() {
        return () -> {
            try {
                Thread.sleep(Duration.ofSeconds(30).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return List.of(listing("slow", "Slow"));
        };
    }

    private static ListingSource failingSource() {
        return () -> {
            throw new RuntimeException("upstream 500");
        };
    }

    @Test
    void mergesReachableSourcesAndReportsThemUp() {
        ListingSource a = () -> List.of(listing("a1", "A"));
        ListingSource b = () -> List.of(listing("b1", "B"), listing("b2", "B"));

        Aggregation result = new SourceAggregator(
                List.of(new NamedSource("A", a), new NamedSource("B", b)),
                Duration.ofSeconds(2)).aggregate();

        assertThat(result.listings()).hasSize(3);
        assertThat(result.statuses())
                .containsExactly(new SourceStatus("A", true), new SourceStatus("B", true));
    }

    @Test
    void dropsSlowSourceButKeepsAndReportsTheFastOne() {
        ListingSource fast = () -> List.of(listing("f1", "Fast"));

        Aggregation result = new SourceAggregator(
                List.of(new NamedSource("Fast", fast), new NamedSource("Slow", slowSource())),
                Duration.ofMillis(200)).aggregate();

        // Only the fast source's listing survives; the slow one is dropped, not blocking.
        assertThat(result.listings()).extracting(Listing::sourcePlatform).containsExactly("Fast");
        assertThat(result.statuses())
                .containsExactly(new SourceStatus("Fast", true), new SourceStatus("Slow", false));
    }

    @Test
    void dropsFailingSourceAndReportsItDown() {
        ListingSource ok = () -> List.of(listing("ok1", "Ok"));

        Aggregation result = new SourceAggregator(
                List.of(new NamedSource("Ok", ok), new NamedSource("Broken", failingSource())),
                Duration.ofSeconds(2)).aggregate();

        assertThat(result.listings()).extracting(Listing::sourcePlatform).containsExactly("Ok");
        assertThat(result.statuses())
                .containsExactly(new SourceStatus("Ok", true), new SourceStatus("Broken", false));
    }

    @Test
    void throwsWhenEverySourceIsUnavailable() {
        assertThatThrownBy(() -> new SourceAggregator(
                List.of(new NamedSource("Broken", failingSource()),
                        new NamedSource("Slow", slowSource())),
                Duration.ofMillis(200)).aggregate())
                .isInstanceOf(AllSourcesUnavailableException.class);
    }
}
