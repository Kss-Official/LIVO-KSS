package com.livo.api.modules.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Domain filter for global search")
public enum SearchDomain {
    ALL,
    TASK,
    GOAL,
    EVENT,
    HABIT,
    LEARNING,
    EXPENSE,
    TRIP
}
