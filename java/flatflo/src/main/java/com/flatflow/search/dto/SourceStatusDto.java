package com.flatflow.search.dto;

/** Per-source reachability for a search, so the UI can report partial coverage (spec FR-017). */
public record SourceStatusDto(String sourcePlatform, boolean reachable) {
}
