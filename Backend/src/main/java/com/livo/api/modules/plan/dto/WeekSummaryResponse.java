package com.livo.api.modules.plan.dto;

import com.livo.api.engines.overload.WorkloadEvaluator.WorkloadLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekSummaryResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayCapacityItem {
        private LocalDate date;
        private String dayOfWeek;
        private BigDecimal plannedHours;
        private BigDecimal availableHours;
        private double capacityPercentage;
        private WorkloadLabel capacityLabel;
        private int taskCount;
        private int eventCount;
        private boolean isOverloaded;
    }

    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayCapacityItem> days;
    private double averageCapacityPercentage;
}
