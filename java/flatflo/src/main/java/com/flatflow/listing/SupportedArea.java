package com.flatflow.listing;

import java.util.Optional;

/**
 * The predefined locations a tenant can search within. Backs the location
 * picker (via {@code GET /api/areas}) and constrains valid searches so an
 * unrecognized location can never be submitted.
 *
 * <p>The {@link #displayName()} matches the {@code locality} used in seeded
 * listings, so a selected area maps directly to matching listings.
 */
public enum SupportedArea {
    GOREGAON_EAST("Goregaon East"),
    MALAD_WEST("Malad West"),
    THANE("Thane");

    private final String displayName;

    SupportedArea(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** Resolve an area by its enum id (e.g., "GOREGAON_EAST"), case-insensitively. */
    public static Optional<SupportedArea> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (SupportedArea area : values()) {
            if (area.name().equalsIgnoreCase(id.trim())) {
                return Optional.of(area);
            }
        }
        return Optional.empty();
    }
}
