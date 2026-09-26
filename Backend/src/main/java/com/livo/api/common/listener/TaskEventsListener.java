package com.livo.api.common.listener;

import com.livo.api.common.event.TaskCompletedEvent;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.service.NotificationService;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventsListener {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final NotificationService notificationService;

    @Async("boundedTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.info("Handling TaskCompletedEvent for task {} user {}", event.taskId(), event.userId());

        // 1. Update linked goal progress if task is linked to a goal
        if (event.goalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(event.goalId(), event.userId()).ifPresent(goal -> {
                List<TaskEntity> linkedTasks = taskRepository.findAllByUserIdAndGoalIdAndDeletedAtIsNull(event.userId(), event.goalId());
                if (!linkedTasks.isEmpty()) {
                    long completedCount = linkedTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                            .count();
                    BigDecimal progress = BigDecimal.valueOf((double) completedCount / linkedTasks.size() * 100.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    goal.setCurrentValue(progress);
                    goalRepository.save(goal);
                    log.info("Updated linked goal {} progress to {}% for user {}", goal.getId(), progress, event.userId());
                }
            });
        }

        // 2. Check for daily productivity milestone (e.g., 5 tasks completed today)
        LocalDate today = LocalDate.now();
        List<TaskEntity> completedToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(event.userId(), today).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .toList();

        if (completedToday.size() == 5) {
            notificationService.sendSystemNotification(
                    event.userId(),
                    NotificationType.SYSTEM,
                    "Productivity Milestone!",
                    "You've completed 5 tasks today! Excellent momentum.",
                    "TASK",
                    event.taskId()
            );
        }
    }
}
