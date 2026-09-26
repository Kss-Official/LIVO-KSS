package com.livo.api.modules.health.dto;

import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHealthEntryRequest {

    private UUID goalId;

    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private HealthType healthType;

    private LocalDate entryDate;

    private LocalTime entryTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    private Integer durationMins;

    private HealthIntensity intensity;

    private Map<String, Object> metricsJson;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
