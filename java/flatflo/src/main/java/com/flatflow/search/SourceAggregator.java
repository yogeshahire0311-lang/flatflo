package com.flatflow.search;

import com.flatflow.listing.Listing;
import com.flatflow.listing.ListingSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Fans a search out across the configured {@link ListingSource}s within a time
 * budget, then merges what came back and records per-source reachability.
 *
 * <p>This is the aggregation seam the performance contract (FR-021) lives behind:
 * a source that doesn't answer within {@link #sourceTimeout} is dropped and
 * marked unreachable rather than blocking the response; if every source fails an
 * {@link AllSourcesUnavailableException} is raised (→ HTTP 503). The MVP wires a
 * single always-available seeded source, so in practice N==M==1 and reachable —
 * but the seam holds unchanged when real, flaky sources replace the seed.
 */
@Component
public class SourceAggregator {

    private static final Logger logger = LoggerFactory.getLogger(SourceAggregator.class);

    /** A configured source with the display name reported in {@code sources[]}. */
    public record NamedSource(String name, ListingSource source) {}

    /** Merged listings from the reachable sources plus per-source status. */
    public record Aggregation(List<Listing> listings, List<SourceStatus> statuses) {}

    /** Per-source reachability, mirrored to the {@code sources[]} response field. */
    public record SourceStatus(String sourcePlatform, boolean reachable) {}

    private final List<NamedSource> sources;
    private final Duration sourceTimeout;

    /** Production wiring: the seeded feed as a single "SeedFeed" source, ~8s drop budget. */
    @Autowired
    public SourceAggregator(ListingSource seededSource) {
        this(List.of(new NamedSource("SeedFeed", seededSource)), Duration.ofSeconds(8));
    }

    /** Explicit wiring for tests (custom sources / shorter timeout). */
    public SourceAggregator(List<NamedSource> sources, Duration sourceTimeout) {
        this.sources = List.copyOf(sources);
        this.sourceTimeout = sourceTimeout;
    }

    /**
     * Query every source concurrently, waiting at most {@link #sourceTimeout} for
     * each. Slow or failing sources are dropped and marked {@code reachable:false}.
     *
     * @throws AllSourcesUnavailableException if no source returns within budget
     */
    public Aggregation aggregate() {
        record Pending(NamedSource src, CompletableFuture<List<Listing>> future) {}

        List<Pending> pending = sources.stream()
                .map(s -> new Pending(s, CompletableFuture.supplyAsync(() -> s.source().findAll())))
                .toList();

        List<Listing> merged = new ArrayList<>();
        List<SourceStatus> statuses = new ArrayList<>();
        int reachableCount = 0;

        for (Pending p : pending) {
            boolean reachable;
            try {
                List<Listing> fromSource = p.future().get(sourceTimeout.toMillis(), TimeUnit.MILLISECONDS);
                merged.addAll(fromSource);
                reachable = true;
                reachableCount++;
            } catch (TimeoutException e) {
                p.future().cancel(true);
                reachable = false;
                logger.warn("Source '{}' timed out after {} — dropping it", p.src().name(), sourceTimeout);
            } catch (Exception e) {
                reachable = false;
                logger.warn("Source '{}' failed — dropping it: {}", p.src().name(), e.getMessage());
            }
            statuses.add(new SourceStatus(p.src().name(), reachable));
        }

        if (reachableCount == 0) {
            throw new AllSourcesUnavailableException("No listing sources could be reached. Please retry.");
        }

        return new Aggregation(merged, statuses);
    }
}
