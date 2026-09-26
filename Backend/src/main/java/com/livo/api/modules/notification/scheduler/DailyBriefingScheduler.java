package com.livo.api.modules.notification.scheduler;

import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.notification.service.NotificationService;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Dispatches automated morning briefings (8:00 AM local time) and evening reflections (9:00 PM local time).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyBriefingScheduler {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    /**
     * Evaluates every 30 minutes to capture 8:00 AM and 9:00 PM across all global timezones,
     * including half-hour offset zones (e.g. India UTC+5:30).
     */
    @Scheduled(cron = "0 0,30 * * * *")
    public void processScheduledDailyBriefings() {
        dispatchBriefingsForInstant(Instant.now());
    }

    public void dispatchBriefingsForInstant(Instant referenceInstant) {
        sendMorningBriefings(referenceInstant);
        sendEveningReviews(referenceInstant);
    }

    /**
     * Sends morning briefings. When referenceInstant is supplied, only users whose local time is 8:00 AM
     * and who haven't yet received today's briefing are processed.
     * When referenceInstant is null (e.g. in test suites), all users are processed using their local date.
     */
    public void sendMorningBriefings() {
        sendMorningBriefings(null);
    }

    public void sendMorningBriefings(Instant referenceInstant) {
        List<UserEntity> users = userRepository.findAllByDeletedAtIsNull();

        for (UserEntity user : users) {
            ZoneId zoneId = resolveZoneId(user.getTimezone());
            LocalDate today = (referenceInstant != null)
                    ? referenceInstant.atZone(zoneId).toLocalDate()
                    : LocalDate.now(zoneId);

            if (referenceInstant != null) {
                LocalTime localTime = referenceInstant.atZone(zoneId).toLocalTime();
                if (localTime.getHour() != 8) {
                    continue;
                }

                Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
                if (notificationRepository.existsNotificationSince(user.getId(), NotificationType.SYSTEM, "Good morning", startOfDay)) {
                    continue;
                }
            }

            List<TaskEntity> tasksToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(user.getId(), today);
            long pendingCount = tasksToday.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).count();

            if (pendingCount > 0) {
                notificationService.sendSystemNotification(
                        user.getId(),
                        NotificationType.SYSTEM,
                        "Good morning, " + (user.getFullName() != null ? user.getFullName() : "there") + "!",
                        "You have " + pendingCount + " tasks scheduled for today. Make today count!",
                        "PLAN",
                        null
                );
            }
        }
    }

    /**
     * Sends evening reviews. When referenceInstant is supplied, only users whose local time is 9:00 PM (hour == 21)
     * and who haven't yet received today's evening review are processed.
     * When referenceInstant is null (e.g. in test suites), all users are processed using their local date.
     */
    public void sendEveningReviews() {
        sendEveningReviews(null);
    }

    public void sendEveningReviews(Instant referenceInstant) {
        List<UserEntity> users = userRepository.findAllByDeletedAtIsNull();

        for (UserEntity user : users) {
            ZoneId zoneId = resolveZoneId(user.getTimezone());
            LocalDate today = (referenceInstant != null)
                    ? referenceInstant.atZone(zoneId).toLocalDate()
                    : LocalDate.now(zoneId);

            if (referenceInstant != null) {
                LocalTime localTime = referenceInstant.atZone(zoneId).toLocalTime();
                if (localTime.getHour() != 21) {
                    continue;
                }

                Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
                if (notificationRepository.existsNotificationSince(user.getId(), NotificationType.SYSTEM, "Evening Review", startOfDay)) {
                    continue;
                }
            }

            List<TaskEntity> tasksToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(user.getId(), today);
            long completedCount = tasksToday.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

            if (completedCount > 0) {
                notificationService.sendSystemNotification(
                        user.getId(),
                        NotificationType.SYSTEM,
                        "Evening Review",
                        "Great job today! You completed " + completedCount + " tasks. Take time to relax and recharge.",
                        "PLAN",
                        null
                );
            }
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
