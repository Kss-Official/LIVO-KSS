package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Domain intent types for Universal Quick Capture")
public enum UniversalAddDomain {
    TASK,
    EVENT,
    EXPENSE,
    HABIT,
    GOAL,
    LEARNING,
    TRIP
}
