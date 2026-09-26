package com.livo.api.modules.insights.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.insights.dto.*;
import com.livo.api.modules.insights.service.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "Endpoints for time allocation, peak productivity histograms, week-over-week comparisons, and habit correlations")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping
    @Operation(summary = "Get Comprehensive Insights Feed", description = "Returns full productivity analytics including time distribution, 24h histogram, weekly comparison, habit correlations, and profile stats")
    public ResponseEntity<ApiResponse<ComprehensiveInsightsResponse>> getComprehensiveInsights(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ComprehensiveInsightsResponse response = insightsService.getComprehensiveInsights(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/time-distribution")
    @Operation(summary = "Get Time Distribution by Category", description = "Returns time tracked and planned across Work, Personal, Learning, Health, Travel, and Others")
    public ResponseEntity<ApiResponse<TimeDistributionResponse>> getTimeDistribution(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TimeDistributionResponse response = insightsService.getTimeDistribution(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/productivity-histogram")
    @Operation(summary = "Get 24-Hour Productivity Histogram", description = "Returns hourly task completion and focus minutes histogram with peak focus window in user's timezone")
    public ResponseEntity<ApiResponse<ProductivityHistogramResponse>> getProductivityHistogram(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ProductivityHistogramResponse response = insightsService.getProductivityHistogram(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/week-comparison")
    @Operation(summary = "Get Week-Over-Week Comparison", description = "Compares current week vs previous week across tasks, focus hours, habit check-ins, and expenses")
    public ResponseEntity<ApiResponse<WeeklyComparisonResponse>> getWeeklyComparison(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        WeeklyComparisonResponse response = insightsService.getWeeklyComparison(currentUser.getId(), referenceDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/correlations")
    @Operation(summary = "Get Habit-Productivity Correlations", description = "Calculates task completion lift on days when habits were completed vs not completed")
    public ResponseEntity<ApiResponse<HabitCorrelationResponse>> getHabitCorrelations(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer days
    ) {
        HabitCorrelationResponse response;
        if (startDate != null && endDate != null) {
            response = insightsService.getHabitCorrelations(currentUser.getId(), startDate, endDate);
        } else {
            response = insightsService.getHabitCorrelations(currentUser.getId(), days != null ? days : 30);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user-stats")
    @Operation(summary = "Get User Profile Productivity Stats", description = "Returns total completed tasks, active goals, day streak, learning time, and weekly % change")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @CurrentUser UserPrincipal currentUser
    ) {
        UserStatsResponse response = insightsService.getUserStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
