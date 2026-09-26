package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadSummaryDto {
    private BigDecimal plannedWorkloadHours;
    private BigDecimal maxPlannedHours;
    private BigDecimal availableWakingHours;
    private double capacityPercentage;
    private String workloadStatus; // "LIGHT", "OPTIMAL", "BALANCED", "OVERLOADED"
    private boolean isOverloaded;
}
