package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalsProgressSummary {
    private long activeGoalsCount;
    private long totalMilestones;
    private long completedMilestones;
    private double progressPercentage;
    private String label; // e.g. "Goals 1/3" or "33% Overall"
    @Builder.Default
    private List<TopGoalDto> topGoals = new ArrayList<>();
}
