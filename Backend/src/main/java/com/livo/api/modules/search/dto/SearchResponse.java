package com.livo.api.modules.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated global search response with relevance-ranked items")
public class SearchResponse {

    @Schema(description = "Original query string")
    private String query;

    @Schema(description = "Domain filter applied")
    private SearchDomain domainFilter;

    @Schema(description = "Total number of matching results")
    private int totalResults;

    @Schema(description = "List of matching search items ordered by relevance score descending")
    private List<SearchResultItem> items;
}
