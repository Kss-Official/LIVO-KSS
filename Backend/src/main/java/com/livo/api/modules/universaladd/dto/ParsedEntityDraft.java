package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured draft parsed from natural language input, ready for 1-tap confirmation")
public class ParsedEntityDraft {

    @Schema(description = "Detected domain type")
    private UniversalAddDomain domain;

    @Schema(description = "Extracted title or headline")
    private String title;

    @Schema(description = "Additional extracted description or notes")
    private String description;

    @Schema(description = "Inferred category (e.g. WORK, FOOD, PERSONAL, HEALTH)")
    private String category;

    @Builder.Default
    @Schema(description = "Extracted hashtags")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "Inferred due date, start date, or transaction date")
    private LocalDate date;

    @Schema(description = "Inferred time (due time or start time)")
    private LocalTime time;

    @Schema(description = "Inferred end date for multi-day trips or events")
    private LocalDate endDate;

    @Schema(description = "Inferred end time for events")
    private LocalTime endTime;

    @Schema(description = "Inferred financial amount")
    private BigDecimal amount;

    @Builder.Default
    @Schema(description = "Inferred currency")
    private String currency = "INR";

    @Schema(description = "Inferred frequency for habits (e.g. DAILY, WEEKLY)")
    private String frequency;

    @Schema(description = "Inferred location or travel destination")
    private String locationOrDestination;

    @Builder.Default
    @Schema(description = "Inferred priority (LOW, MEDIUM, HIGH, URGENT)")
    private String priority = "MEDIUM";

    @Schema(description = "Parse confidence score (0.0 to 1.0)")
    private double confidenceScore;

    @Schema(description = "Original raw input text")
    private String rawInput;
}
