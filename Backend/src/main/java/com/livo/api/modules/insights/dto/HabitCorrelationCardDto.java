package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitCorrelationCardDto {
    private UUID habitId;
    private String habitTitle;
    private String iconKey;
    private String colorHex;
    private int sampleDaysEvaluated;
    private double taskCompletionRateOnHabitDays;
    private double taskCompletionRateOnNonHabitDays;
    private double liftPercentage; // e.g. +28.5%
    private String insightText; // "On days you completed Morning Meditation, your task completion rate was 28.5% higher."
}
