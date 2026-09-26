package com.livo.api.modules.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogLearningSessionRequest {

    @NotNull(message = "Learning item ID is required")
    private UUID learningItemId;

    private LocalDate sessionDate;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    private Integer durationMinutes;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
