package com.flatflow.search.dto;

import java.util.List;

/**
 * The search result envelope: a page of grouped results plus meta (spec FR-011)
 * and per-source status (spec FR-017). {@code dupCount} = total offers across
 * matching groups minus the group count.
 */
public record SearchResponseDto(
        List<ListingGroupDto> results,
        int count,
        int dupCount,
        String sort,
        int page,
        int pageSize,
        boolean hasMore,
        List<SourceStatusDto> sources
) {
}
