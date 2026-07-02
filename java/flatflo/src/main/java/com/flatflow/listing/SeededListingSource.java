package com.flatflow.listing;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link ListingSource} backed by the bundled {@code listings-seed.json}
 * fixture, loaded into memory once at construction. Temporary stand-in for the
 * MVP; a later AI-agent-driven source will replace it (spec FR-002/FR-013).
 */
@Component
public class SeededListingSource implements ListingSource {

    private static final Logger logger = LoggerFactory.getLogger(SeededListingSource.class);
    private static final String SEED_PATH = "listings-seed.json";

    private final List<Listing> listings;

    public SeededListingSource(ObjectMapper objectMapper) {
        this.listings = load(objectMapper);
        logger.info("Loaded {} seeded listings from {}", listings.size(), SEED_PATH);
    }

    private static List<Listing> load(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource(SEED_PATH).getInputStream()) {
            return List.copyOf(objectMapper.readValue(in, new TypeReference<List<Listing>>() {}));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load seed feed: " + SEED_PATH, e);
        }
    }

    @Override
    public List<Listing> findAll() {
        return listings;
    }
}
