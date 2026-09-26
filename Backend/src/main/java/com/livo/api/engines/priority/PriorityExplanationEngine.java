package com.livo.api.engines.priority;

import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 0ms Deterministic Template Engine explaining "Why LIVO picked this task?".
 */
@Component
public class PriorityExplanationEngine {

    public String explain(TaskEntity task, LocalDate targetDate, String goalTitle) {
        if (task == null) return "Fits into your planned schedule";
        LocalDate referenceDate = (targetDate != null) ? targetDate : LocalDate.now();

        if (task.getDueDate() != null && task.getDueDate().isBefore(referenceDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(task.getDueDate(), referenceDate);
            return (daysOverdue > 1)
                    ? "Overdue by " + daysOverdue + " days · Requires immediate attention to get back on track"
                    : "Overdue from yesterday · Prioritized to protect project velocity";
        }

        if (task.getPriority() == TaskPriority.URGENT) {
            return (goalTitle != null && !goalTitle.isBlank())
                    ? "Urgent priority directly aligned with goal '" + goalTitle + "'"
                    : "Urgent priority scheduled for immediate execution today";
        }

        if (task.getPriority() == TaskPriority.HIGH) {
            return (goalTitle != null && !goalTitle.isBlank())
                    ? "High priority advancing goal '" + goalTitle + "' during your peak focus window"
                    : "High priority scheduled for your peak focus window";
        }

        if (goalTitle != null && !goalTitle.isBlank()) {
            return "Directly advances goal '" + goalTitle + "' · Optimal fit for today's open schedule block";
        }

        if (task.getDueTime() != null) {
            return "Scheduled at specific time (" + task.getDueTime() + ") in your daily timeline";
        }

        return "Optimal duration fit for today's available focus window";
    }
}
