package com.flatflow.listing;

import java.util.List;

/**
 * Supplies raw {@link Listing}s to FlatFlo.
 *
 * <p>The MVP implementation ({@link SeededListingSource}) loads a bundled seed
 * file. This interface is the seam that lets the seeded feed be replaced later
 * by an AI-agent-driven source returning the same shape, without changing the
 * search flow (spec FR-002).
 */
public interface ListingSource {

    /** Return all raw listings currently available from this source. */
    List<Listing> findAll();
}
