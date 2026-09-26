package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopGoalDto {
    private UUID goalId;
    private String title;
    private BigDecimal progressPercentage;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private long milestonesCompleted;
    private long totalMilestones;
}
