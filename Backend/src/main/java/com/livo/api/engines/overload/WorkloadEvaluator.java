package com.livo.api.engines.overload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;

/**
 * 100% Deterministic Workload Capacity Evaluator.
 * CapacityRatio = (PlannedWorkHours / AvailableWakingHours) * 100%
 * Labels: LIGHT (<60%), BALANCED (60-85%), HEAVY (85-100%), OVERLOADED (>100%)
 */
@Component
public class WorkloadEvaluator {

    public enum WorkloadLabel {
        LIGHT,
        BALANCED,
        HEAVY,
        OVERLOADED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkloadAssessment {
        private BigDecimal plannedHours;
        private BigDecimal availableHours;
        private double capacityPercentage;
        private WorkloadLabel label;
        private boolean isOverloaded;
        private String recommendation;
    }

    public WorkloadAssessment evaluate(
            LocalTime wakeTime,
            LocalTime sleepTime,
            long committedRoutineMinutes,
            long plannedWorkMinutes,
            BigDecimal userMaxPlannedHours
    ) {
        LocalTime effectiveWake = (wakeTime != null) ? wakeTime : LocalTime.of(7, 0);
        LocalTime effectiveSleep = (sleepTime != null) ? sleepTime : LocalTime.of(23, 0);

        long totalWakingMinutes = Duration.between(effectiveWake, effectiveSleep).toMinutes();
        if (totalWakingMinutes <= 0) totalWakingMinutes = 960; // 16 hours fallback

        long availableMinutes = Math.max(60, totalWakingMinutes - committedRoutineMinutes);
        BigDecimal availableHours = BigDecimal.valueOf(availableMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal plannedHours = BigDecimal.valueOf(plannedWorkMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        double capacityPercentage = (availableMinutes > 0)
                ? (plannedWorkMinutes * 100.0) / availableMinutes
                : 100.0;

        capacityPercentage = Math.round(capacityPercentage * 10.0) / 10.0;

        WorkloadLabel label;
        boolean isOverloaded;
        String recommendation;

        if (capacityPercentage < 60.0) {
            label = WorkloadLabel.LIGHT;
            isOverloaded = false;
            recommendation = "Your day has ample buffer. Great opportunity to advance a high-impact goal.";
        } else if (capacityPercentage <= 85.0) {
            label = WorkloadLabel.BALANCED;
            isOverloaded = false;
            recommendation = "Optimal workload balance. Sustainable velocity across all scheduled items.";
        } else if (capacityPercentage <= 100.0) {
            label = WorkloadLabel.HEAVY;
            isOverloaded = false;
            recommendation = "Tight schedule with minimal rest gaps. Protect your deep work focus blocks.";
        } else {
            label = WorkloadLabel.OVERLOADED;
            isOverloaded = true;
            recommendation = "Overloaded day! Planned commitments exceed available waking hours. Consider shifting flexible tasks.";
        }

        return WorkloadAssessment.builder()
                .plannedHours(plannedHours)
                .availableHours(availableHours)
                .capacityPercentage(capacityPercentage)
                .label(label)
                .isOverloaded(isOverloaded)
                .recommendation(recommendation)
                .build();
    }
}
