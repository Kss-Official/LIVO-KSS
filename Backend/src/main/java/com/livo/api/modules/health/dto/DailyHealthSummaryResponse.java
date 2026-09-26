package com.livo.api.modules.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyHealthSummaryResponse {

    private LocalDate date;
    private long totalEntries;
    private int totalWorkoutMinutes;
    private Double totalSleepHours;
    private Map<String, Long> entriesByType;
    private List<HealthEntryResponse> entries;
}
