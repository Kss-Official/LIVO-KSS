package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekComparisonMetricDto {
    private String metricKey; // "TASKS_COMPLETED", "FOCUS_HOURS", "HABIT_CHECKINS", "TOTAL_EXPENSES"
    private String label; // "Tasks Completed", "Focus Hours", "Habit Check-ins", "Total Expenses"
    private BigDecimal currentWeekValue;
    private BigDecimal previousWeekValue;
    private double percentageChange; // e.g. +15.5 or -8.2
    private String trend; // "UP", "DOWN", "STABLE"
    private boolean isPositiveImpact; // true if UP for tasks/habits, or DOWN for expenses
}
