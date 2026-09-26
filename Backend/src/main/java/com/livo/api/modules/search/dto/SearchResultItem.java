package com.livo.api.modules.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual search result item across domains with relevance ranking")
public class SearchResultItem {

    @Schema(description = "Entity unique identifier")
    private UUID id;

    @Schema(description = "Domain category of the item")
    private SearchDomain domainType;

    @Schema(description = "Entity title or header")
    private String title;

    @Schema(description = "Extracted context snippet containing or surrounding the match")
    private String snippet;

    @Schema(description = "Status or category of the entity (e.g. TODO, WORK, DAILY)")
    private String categoryOrStatus;

    @Schema(description = "Relevant date or timestamp string (due date, start date, etc.)")
    private String dateOrTime;

    @Schema(description = "Relevance score normalized between 0.0 and 1.0")
    private double relevanceScore;
}
