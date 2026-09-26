package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveInsightsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private TimeDistributionResponse timeDistribution;
    private ProductivityHistogramResponse productivityHistogram;
    private WeeklyComparisonResponse weeklyComparison;
    private HabitCorrelationResponse habitCorrelations;
    private UserStatsResponse userStats;
    private BigDecimal totalExpensesThisWeek;
}
