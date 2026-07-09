package com.flatflow.search;

/**
 * Thrown when every configured listing source fails or times out, so there are
 * no results at all (distinct from a valid search that simply matched nothing).
 * Mapped to HTTP 503 {@code ALL_SOURCES_UNAVAILABLE} so the UI shows the
 * full-width error + retry rather than the empty state (spec FR-017).
 */
public class AllSourcesUnavailableException extends RuntimeException {

    public AllSourcesUnavailableException(String message) {
        super(message);
    }
}
