package com.livo.api.modules.insights.service;

import com.livo.api.modules.insights.dto.*;

import java.time.LocalDate;
import java.util.UUID;

public interface InsightsService {

    ComprehensiveInsightsResponse getComprehensiveInsights(UUID userId, LocalDate startDate, LocalDate endDate);

    TimeDistributionResponse getTimeDistribution(UUID userId, LocalDate startDate, LocalDate endDate);

    ProductivityHistogramResponse getProductivityHistogram(UUID userId, LocalDate startDate, LocalDate endDate);

    WeeklyComparisonResponse getWeeklyComparison(UUID userId, LocalDate referenceDate);

    HabitCorrelationResponse getHabitCorrelations(UUID userId, int days);

    HabitCorrelationResponse getHabitCorrelations(UUID userId, LocalDate startDate, LocalDate endDate);

    UserStatsResponse getUserStats(UUID userId);
}
