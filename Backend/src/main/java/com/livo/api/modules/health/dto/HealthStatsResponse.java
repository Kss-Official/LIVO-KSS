package com.livo.api.modules.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalEntries;
    private long totalWorkoutMinutes;
    private long totalWorkoutSessions;
    private double averageWorkoutDurationMins;
    private Map<String, Long> entriesByType;
    private Map<String, Long> intensityBreakdown;
}
