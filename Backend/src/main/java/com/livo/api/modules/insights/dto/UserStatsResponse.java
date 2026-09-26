package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private long tasksCompletedTotal;
    private double tasksCompletedWeekChangePct;
    private long activeGoalsTotal;
    private double activeGoalsWeekChangePct;
    private int currentDayStreak;
    private double streakWeekChangePct;
    private long learningTimeMinutesTotal;
    private double learningTimeWeekChangePct;
}
