package com.livo.api.engines.priority;

import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 100% Deterministic Priority Scoring Engine.
 * Formula: Score = (0.35 * Urgency) + (0.25 * Goal) + (0.20 * SlotFit) + (0.10 * Energy) + (0.10 * Overdue)
 */
@Component
public class PriorityScoringEngine {

    public double calculateScore(TaskEntity task, LocalDate targetDate) {
        if (task == null) return 0.0;
        LocalDate referenceDate = (targetDate != null) ? targetDate : LocalDate.now();

        // 1. Urgency (35%): URGENT=100, HIGH=75, MEDIUM=50, LOW=25
        double urgencyScore = getUrgencyWeight(task.getPriority());

        // 2. Goal Alignment (25%): 100 if linked to an active goal, 0 otherwise
        double goalScore = (task.getGoalId() != null) ? 100.0 : 0.0;

        // 3. Slot Fit (20%): 100 if has scheduled time or fits default duration, 50 baseline
        double slotScore = (task.getDueTime() != null || (task.getDurationMins() != null && task.getDurationMins() <= 60)) ? 100.0 : 50.0;

        // 4. Energy Fit (10%): Baseline peak focus allocation
        double energyScore = 75.0;

        // 5. Overdue Status (10%): 100 if due date before targetDate and not completed, 0 otherwise
        boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(referenceDate);
        double overdueScore = isOverdue ? 100.0 : 0.0;

        double finalScore = (0.35 * urgencyScore)
                + (0.25 * goalScore)
                + (0.20 * slotScore)
                + (0.10 * energyScore)
                + (0.10 * overdueScore);

        return Math.round(finalScore * 10.0) / 10.0;
    }

    private double getUrgencyWeight(TaskPriority priority) {
        if (priority == null) return 50.0;
        return switch (priority) {
            case URGENT -> 100.0;
            case HIGH -> 75.0;
            case MEDIUM -> 50.0;
            case LOW -> 25.0;
        };
    }
}
