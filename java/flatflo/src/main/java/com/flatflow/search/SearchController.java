package com.flatflow.search;

import com.flatflow.listing.BhkType;
import com.flatflow.listing.Furnishing;
import com.flatflow.listing.SupportedArea;
import com.flatflow.search.dto.AreaDto;
import com.flatflow.search.dto.SearchResponseDto;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry points for the search screen (spec FR-001..FR-006, FR-011).
 * Anonymous; JSON in/out. Display formatting (INR, "per month") is applied in
 * {@link SearchService} at the DTO boundary (FR-019).
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/areas")
    public Map<String, List<AreaDto>> areas() {
        List<AreaDto> areas = Arrays.stream(SupportedArea.values())
                .map(a -> new AreaDto(a.name(), a.displayName()))
                .toList();
        return Map.of("areas", areas);
    }

    @GetMapping("/search")
    public SearchResponseDto search(
            @RequestParam String location,
            @RequestParam String bhk,
            @RequestParam(required = false) Integer budgetMin,
            @RequestParam(required = false) Integer budgetMax,
            @RequestParam(required = false) String furnishing,
            @RequestParam(required = false, defaultValue = "BEST_DEAL") String sort,
            @RequestParam(required = false, defaultValue = "0") int page) {

        SupportedArea area = SupportedArea.fromId(location)
                .orElseThrow(() -> new InvalidSearchException(
                        "INVALID_LOCATION", "Unknown area id '" + location + "'. Choose from /api/areas."));

        BhkType bhkType = parseEnum(BhkType.class, bhk, "INVALID_BHK", "bhk");
        Furnishing furnishingType = furnishing == null
                ? null
                : parseEnum(Furnishing.class, furnishing, "INVALID_FURNISHING", "furnishing");
        SortMode sortMode = parseEnum(SortMode.class, sort, "INVALID_SORT", "sort");

        if (budgetMin != null && budgetMin < 0) {
            throw new InvalidSearchException("INVALID_BUDGET", "budgetMin must not be negative.");
        }
        if (budgetMax != null && budgetMax < 0) {
            throw new InvalidSearchException("INVALID_BUDGET", "budgetMax must not be negative.");
        }
        if (budgetMin != null && budgetMax != null && budgetMin > budgetMax) {
            throw new InvalidSearchException("INVALID_BUDGET", "budgetMin must not exceed budgetMax.");
        }
        if (page < 0) {
            throw new InvalidSearchException("INVALID_PAGE", "page must not be negative.");
        }

        SearchQuery query = new SearchQuery(area, budgetMin, budgetMax, bhkType, furnishingType, sortMode, page);
        return searchService.search(query);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String error, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSearchException(error, "Invalid " + field + " value '" + value + "'.");
        }
    }
}
