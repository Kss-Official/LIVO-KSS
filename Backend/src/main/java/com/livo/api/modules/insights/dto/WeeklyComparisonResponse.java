package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyComparisonResponse {
    private LocalDate currentWeekStart;
    private LocalDate currentWeekEnd;
    private LocalDate previousWeekStart;
    private LocalDate previousWeekEnd;
    private double overallProductivityDelta;
    @Builder.Default
    private List<WeekComparisonMetricDto> metrics = new ArrayList<>();
}
