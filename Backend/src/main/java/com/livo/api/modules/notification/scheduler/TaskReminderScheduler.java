package com.livo.api.modules.notification.scheduler;

import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.service.NotificationService;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks for tasks due in the upcoming window per user timezone and sends timely reminders.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void checkUpcomingTaskDeadlines() {
        checkUpcomingTaskDeadlines(Instant.now());
    }

    public void checkUpcomingTaskDeadlines(Instant referenceInstant) {
        Instant nowInstant = (referenceInstant != null) ? referenceInstant : Instant.now();
        List<String> activeTimezones = userRepository.findDistinctActiveTimezones();
        Set<String> allTimezones = new LinkedHashSet<>(activeTimezones);
        allTimezones.add("Asia/Kolkata"); // Default fallback timezone

        int totalReminders = 0;

        for (String tz : allTimezones) {
            ZoneId zoneId = resolveZoneId(tz);
            LocalDate userToday = nowInstant.atZone(zoneId).toLocalDate();
            LocalTime userNow = nowInstant.atZone(zoneId).toLocalTime().withNano(0);
            LocalTime windowEnd = userNow.plusMinutes(30).withNano(0);
            boolean isDefault = "Asia/Kolkata".equalsIgnoreCase(tz);

            List<TaskEntity> pendingTasks;
            if (userNow.isAfter(windowEnd)) {
                // Window spans midnight (e.g. 23:45 to 00:15)
                LocalDate tomorrow = userToday.plusDays(1);
                pendingTasks = taskRepository.findUpcomingTasksForReminderInTimezoneSpanningMidnight(
                        tz, isDefault, userToday, tomorrow, userNow, windowEnd);
            } else {
                pendingTasks = taskRepository.findUpcomingTasksForReminderInTimezone(
                        tz, isDefault, userToday, userNow, windowEnd);
            }

            for (TaskEntity task : pendingTasks) {
                notificationService.sendSystemNotification(
                        task.getUserId(),
                        NotificationType.REMINDER,
                        "Upcoming Task: " + task.getTitle(),
                        "This task is due at " + task.getDueTime() + ". Stay focused!",
                        "TASK",
                        task.getId()
                );
                totalReminders++;
            }
        }

        if (totalReminders > 0) {
            log.info("Dispatched {} upcoming task reminders across timezones", totalReminders);
        }
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
