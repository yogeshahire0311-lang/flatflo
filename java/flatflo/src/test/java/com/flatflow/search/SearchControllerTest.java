package com.flatflow.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * API contract tests for the search endpoints over the real seeded feed.
 * Imports the concrete source/grouper/service beans so the slice is wired end to end.
 */
@WebMvcTest(SearchController.class)
@Import({
        SearchService.class,
        com.flatflow.listing.SeededListingSource.class,
        com.flatflow.grouping.SeedListingGrouper.class,
        com.flatflow.grouping.AreaAverageCalculator.class,
        SearchExceptionHandler.class
})
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void areasReturnsSupportedAreas() throws Exception {
        mockMvc.perform(get("/api/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areas[0].id").value("GOREGAON_EAST"))
                .andExpect(jsonPath("$.areas[0].name").value("Goregaon East"));
    }

    @Test
    void searchReturnsGroupedResultsWithMeta() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("location", "GOREGAON_EAST")
                        .param("bhk", "TWO_BHK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sort").value("BEST_DEAL"))
                .andExpect(jsonPath("$.pageSize").value(20))
                // Siddharth Nagar is listed on 2 sources -> at least one duplicate merged.
                .andExpect(jsonPath("$.dupCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.results[0].sources").isArray())
                // Best deal (Siddharth, ~26% below avg) should sort first by default.
                .andExpect(jsonPath("$.results[0].isBestDeal").value(true));
    }

    @Test
    void searchWithNoMatchesReturnsEmptyState() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("location", "THANE")
                        .param("bhk", "ONE_BHK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.results").isEmpty());
    }

    @Test
    void invalidLocationReturns400() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("location", "ATLANTIS")
                        .param("bhk", "TWO_BHK"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_LOCATION"));
    }
}
