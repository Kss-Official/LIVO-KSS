package com.livo.api.modules.notification.scheduler;

import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.notification.service.NotificationService;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MidnightRolloverScheduler {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Data
    @Builder
    public static class RolloverResult {
        private int totalOverdueFound;
        private int usersNotified;
    }

    /**
     * Scheduled every 30 minutes to capture midnight rollovers in all timezones,
     * including half-hour offset zones (e.g. Asia/Kolkata UTC+5:30).
     */
    @Scheduled(cron = "0 0,30 * * * *")
    public void runScheduledMidnightRollover() {
        log.info("Executing scheduled timezone-aware midnight rollover");
        executeTimezoneAwareRollover(Instant.now());
    }

    /**
     * Evaluates users whose local time is currently within midnight hour (00:00 - 00:59)
     * and rolls over tasks overdue as of their local date.
     */
    @Transactional
    public RolloverResult executeTimezoneAwareRollover(Instant referenceInstant) {
        Instant now = referenceInstant != null ? referenceInstant : Instant.now();
        List<String> activeTimezones = userRepository.findDistinctActiveTimezones();
        Set<String> allTimezones = new LinkedHashSet<>(activeTimezones);
        allTimezones.add("Asia/Kolkata");

        int totalOverdue = 0;
        int totalUsersNotified = 0;

        for (String tz : allTimezones) {
            ZoneId zoneId = resolveZoneId(tz);
            LocalTime localTime = now.atZone(zoneId).toLocalTime();

            // Only process timezones where local time is the midnight hour (00:00 - 00:59)
            if (localTime.getHour() != 0) {
                continue;
            }

            LocalDate localDate = now.atZone(zoneId).toLocalDate();
            boolean isDefault = "Asia/Kolkata".equalsIgnoreCase(tz);
            List<TaskEntity> overdueTasks = taskRepository.findOverdueTasksForRolloverInTimezone(tz, isDefault, localDate);

            if (overdueTasks.isEmpty()) {
                continue;
            }

            totalOverdue += overdueTasks.size();
            Map<UUID, List<TaskEntity>> tasksByUser = overdueTasks.stream()
                    .collect(Collectors.groupingBy(TaskEntity::getUserId));

            Instant localMidnight = localDate.atStartOfDay(zoneId).toInstant();

            for (Map.Entry<UUID, List<TaskEntity>> entry : tasksByUser.entrySet()) {
                UUID userId = entry.getKey();
                List<TaskEntity> userTasks = entry.getValue();

                // Prevent sending duplicate OVERDUE notifications if scheduler runs multiple times during midnight hour
                if (notificationRepository.existsNotificationSince(userId, NotificationType.OVERDUE, "Overdue Tasks Reminder", localMidnight)) {
                    continue;
                }

                UUID firstTaskId = userTasks.get(0).getId();
                String title = "Overdue Tasks Reminder";
                String body = String.format("You have %d overdue task(s) from previous days. Tap to reschedule or complete.", userTasks.size());

                try {
                    notificationService.sendSystemNotification(
                            userId,
                            NotificationType.OVERDUE,
                            title,
                            body,
                            "TASK",
                            firstTaskId
                    );
                    totalUsersNotified++;
                } catch (Exception e) {
                    log.error("Failed to dispatch rollover notification for user {}: {}", userId, e.getMessage());
                }
            }
        }

        log.info("Timezone-aware midnight rollover completed: {} overdue tasks, {} users notified", totalOverdue, totalUsersNotified);
        return RolloverResult.builder()
                .totalOverdueFound(totalOverdue)
                .usersNotified(totalUsersNotified)
                .build();
    }

    /**
     * Backward-compatible manual/test rollover engine with explicit reference date.
     */
    @Transactional
    public RolloverResult executeRollover(LocalDate referenceDate) {
        LocalDate cutoff = referenceDate != null ? referenceDate : LocalDate.now();

        List<TaskEntity> allTasks = taskRepository.findOverdueTasksForRollover(cutoff);

        if (allTasks.isEmpty()) {
            log.info("Midnight rollover: No overdue tasks found prior to {}", cutoff);
            return RolloverResult.builder()
                    .totalOverdueFound(0)
                    .usersNotified(0)
                    .build();
        }

        Map<UUID, List<TaskEntity>> tasksByUser = allTasks.stream()
                .collect(Collectors.groupingBy(TaskEntity::getUserId));

        int usersNotified = 0;
        for (Map.Entry<UUID, List<TaskEntity>> entry : tasksByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<TaskEntity> userOverdue = entry.getValue();

            UUID firstTaskId = userOverdue.get(0).getId();
            String title = "Overdue Tasks Reminder";
            String body = String.format("You have %d overdue task(s) from previous days. Tap to reschedule or complete.", userOverdue.size());

            try {
                notificationService.sendSystemNotification(
                        userId,
                        NotificationType.OVERDUE,
                        title,
                        body,
                        "TASK",
                        firstTaskId
                );
                usersNotified++;
            } catch (Exception e) {
                log.error("Failed to dispatch rollover notification for user {}: {}", userId, e.getMessage());
            }
        }

        log.info("Midnight rollover completed: {} overdue tasks found across {} users", allTasks.size(), usersNotified);
        return RolloverResult.builder()
                .totalOverdueFound(allTasks.size())
                .usersNotified(usersNotified)
                .build();
    }

    private static ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("Asia/Kolkata");
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            return ZoneId.of("Asia/Kolkata");
        }
    }
}
