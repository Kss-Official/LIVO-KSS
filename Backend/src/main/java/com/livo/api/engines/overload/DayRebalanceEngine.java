package com.livo.api.engines.overload;

import com.livo.api.modules.plan.dto.ScheduleBlockResponse;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic Day Rebalance Engine.
 * Identifies flexible lower-priority tasks on overloaded days and generates shift recommendations.
 */
@Component
public class DayRebalanceEngine {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalanceShift {
        private UUID taskId;
        private UUID scheduleBlockId;
        private String title;
        private TaskPriority priority;
        private LocalDate originalDate;
        private LocalDate proposedDate;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalanceProposal {
        private LocalDate targetDate;
        private boolean rebalanceRequired;
        private List<RebalanceShift> recommendedShifts;
        private String explanation;
    }

    public RebalanceProposal generateRebalancePlan(
            LocalDate targetDate,
            boolean isOverloaded,
            List<TaskResponse> tasks,
            List<ScheduleBlockResponse> blocks
    ) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        List<RebalanceShift> shifts = new ArrayList<>();

        if (!isOverloaded) {
            return RebalanceProposal.builder()
                    .targetDate(date)
                    .rebalanceRequired(false)
                    .recommendedShifts(List.of())
                    .explanation("Day is comfortably balanced. No task rebalancing needed.")
                    .build();
        }

        LocalDate tomorrow = date.plusDays(1);

        // 1. Check unlocked schedule blocks first
        if (blocks != null) {
            for (ScheduleBlockResponse block : blocks) {
                if (!block.isLocked() && "PERSONAL".equalsIgnoreCase(block.getCategory())) {
                    shifts.add(RebalanceShift.builder()
                            .scheduleBlockId(block.getId())
                            .title(block.getTitle())
                            .originalDate(date)
                            .proposedDate(tomorrow)
                            .reason("Unlocked personal block shifted to tomorrow to relieve day overload")
                            .build());
                    if (shifts.size() >= 2) break;
                }
            }
        }

        // 2. Check flexible lower-priority tasks
        if (tasks != null && shifts.size() < 2) {
            for (TaskResponse task : tasks) {
                if (task.getPriority() == TaskPriority.LOW || task.getPriority() == TaskPriority.MEDIUM) {
                    shifts.add(RebalanceShift.builder()
                            .taskId(task.getId())
                            .title(task.getTitle())
                            .priority(task.getPriority())
                            .originalDate(date)
                            .proposedDate(tomorrow)
                            .reason("Flexible " + task.getPriority() + " priority task shifted to tomorrow to recover focus margin")
                            .build());
                    if (shifts.size() >= 2) break;
                }
            }
        }

        String explanation = shifts.isEmpty()
                ? "All tasks are high priority or locked. Consider extending daily waking boundary or dropping optional tasks."
                : "Shifted " + shifts.size() + " flexible items to tomorrow (" + tomorrow + ") to restore balanced capacity.";

        return RebalanceProposal.builder()
                .targetDate(date)
                .rebalanceRequired(true)
                .recommendedShifts(shifts)
                .explanation(explanation)
                .build();
    }
}
