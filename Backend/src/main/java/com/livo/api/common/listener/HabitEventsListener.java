package com.livo.api.common.listener;

import com.livo.api.common.event.HabitCheckedInEvent;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HabitEventsListener {

    private final GoalRepository goalRepository;
    private final NotificationService notificationService;

    @Async("boundedTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onHabitCheckedIn(HabitCheckedInEvent event) {
        log.info("Handling HabitCheckedInEvent for habit {} user {} streak {}", event.habitId(), event.userId(), event.currentStreak());

        // 1. Check for milestone streaks (7, 14, 30, 100 days) - only when streak newly increased
        int streak = event.currentStreak();
        boolean isMilestone = (streak == 7 || streak == 14 || streak == 30 || streak == 100 || (streak > 100 && streak % 50 == 0));
        if (event.streakIncreased() && isMilestone) {
            notificationService.sendSystemNotification(
                    event.userId(),
                    NotificationType.STREAK,
                    "Streak Milestone Reached!",
                    String.format("Incredible consistency! You hit a %d-day streak on '%s'!", streak, event.habitTitle()),
                    "HABIT",
                    event.habitId()
            );
        }

        // 2. Update linked goal progress if habit is linked to a goal
        if (event.goalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(event.goalId(), event.userId()).ifPresent(goal -> {
                log.info("Habit {} check-in recorded for linked goal {}", event.habitId(), goal.getId());
            });
        }
    }
}
