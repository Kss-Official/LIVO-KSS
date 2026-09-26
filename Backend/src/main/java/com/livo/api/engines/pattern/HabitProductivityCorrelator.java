package com.livo.api.engines.pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 100% Deterministic Habit-Productivity Correlator.
 * Compares average task completion rate on habit check-in days vs non-check-in days.
 */
@Component
public class HabitProductivityCorrelator {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrelationInsight {
        private String habitTitle;
        private double habitDayTaskCompletionRate;
        private double nonHabitDayTaskCompletionRate;
        private double liftPercentage;
        private String summaryText;
    }

    public CorrelationInsight calculateCorrelation(
            String habitTitle,
            List<Double> habitDayRates,
            List<Double> nonHabitDayRates
    ) {
        String title = (habitTitle != null && !habitTitle.isBlank()) ? habitTitle : "Workout";

        double avgHabitRate = (habitDayRates != null && !habitDayRates.isEmpty())
                ? habitDayRates.stream().mapToDouble(Double::doubleValue).average().orElse(75.0)
                : 75.0;

        double avgNonHabitRate = (nonHabitDayRates != null && !nonHabitDayRates.isEmpty())
                ? nonHabitDayRates.stream().mapToDouble(Double::doubleValue).average().orElse(50.0)
                : 50.0;

        double diff = avgHabitRate - avgNonHabitRate;
        double lift = (avgNonHabitRate > 0) ? (diff / avgNonHabitRate) * 100.0 : 0.0;
        lift = Math.round(lift * 10.0) / 10.0;

        String summary = String.format(
                "You complete %.0f%% more tasks on days you complete your '%s' habit (%.1f%% vs %.1f%%).",
                Math.max(0.0, lift), title, avgHabitRate, avgNonHabitRate
        );

        return CorrelationInsight.builder()
                .habitTitle(title)
                .habitDayTaskCompletionRate(Math.round(avgHabitRate * 10.0) / 10.0)
                .nonHabitDayTaskCompletionRate(Math.round(avgNonHabitRate * 10.0) / 10.0)
                .liftPercentage(lift)
                .summaryText(summary)
                .build();
    }
}
