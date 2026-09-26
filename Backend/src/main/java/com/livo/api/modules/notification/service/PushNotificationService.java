package com.livo.api.modules.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    /**
     * Asynchronously dispatches a push notification to all active devices of the given user,
     * verifying that notifications are enabled and the user is not currently in quiet hours.
     */
    @Async
    public void sendPushNotification(UUID userId, String title, String body, Map<String, String> data) {
        dispatchPush(userId, title, body, data, Instant.now());
    }

    /**
     * Evaluates user preferences, timezone, quiet hours, and active device tokens,
     * and dispatches the FCM multicast message if allowed.
     *
     * @return true if the push notification was dispatched (or simulated in test mode), false if suppressed or skipped.
     */
    public boolean dispatchPush(UUID userId, String title, String body, Map<String, String> data, Instant referenceInstant) {
        if (!isPushAllowed(userId, referenceInstant)) {
            return false;
        }

        List<UserDeviceTokenEntity> activeTokens = userDeviceTokenRepository.findAllByUserIdAndIsActiveTrue(userId);

        if (activeTokens.isEmpty()) {
            log.debug("No active FCM tokens found for user {}, skipping push notification", userId);
            return false;
        }

        List<String> tokenStrings = activeTokens.stream()
                .map(UserDeviceTokenEntity::getDeviceToken)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();

        if (tokenStrings.isEmpty()) {
            return false;
        }

        Map<String, String> payloadData = (data != null) ? new HashMap<>(data) : new HashMap<>();
        payloadData.put("userId", userId.toString());

        if (FirebaseApp.getApps().isEmpty()) {
            log.info("FCM fallback mode: Simulated push alert to {} tokens for user {}: title='{}', body='{}'",
                    tokenStrings.size(), userId, title, body);
            return true;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokenStrings)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(payloadData)
                    .build();

            FirebaseMessaging.getInstance().sendEachForMulticastAsync(message);
            log.info("Dispatched FCM multicast message to {} devices for user {}", tokenStrings.size(), userId);
            return true;
        } catch (Exception e) {
            log.warn("Failed to dispatch FCM push notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if push notifications are allowed for the given user at the current instant.
     */
    public boolean isPushAllowed(UUID userId) {
        return isPushAllowed(userId, Instant.now());
    }

    /**
     * Checks if push notifications are allowed for the given user at a specific reference instant.
     * Respects notificationsEnabled toggle and quiet hours converted to the user's timezone.
     */
    public boolean isPushAllowed(UUID userId, Instant referenceInstant) {
        if (userId == null) {
            return false;
        }

        // 1. Verify user exists and is not deleted
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        if (user == null) {
            log.debug("Push notification suppressed: user {} not found or deleted", userId);
            return false;
        }

        // 2. Check user preferences
        UserPreferenceEntity preferences = userPreferenceRepository.findByUserId(userId).orElse(null);
        if (preferences != null && !preferences.isNotificationsEnabled()) {
            log.info("Push notification suppressed for user {}: notifications are disabled in preferences", userId);
            return false;
        }

        // 3. Resolve quiet hours boundaries
        LocalTime quietStart = (preferences != null && preferences.getNotificationQuietStart() != null)
                ? preferences.getNotificationQuietStart()
                : LocalTime.of(22, 0);
        LocalTime quietEnd = (preferences != null && preferences.getNotificationQuietEnd() != null)
                ? preferences.getNotificationQuietEnd()
                : LocalTime.of(7, 0);

        // 4. Resolve user timezone
        String timezone = (user.getTimezone() != null && !user.getTimezone().isBlank())
                ? user.getTimezone().trim()
                : "Asia/Kolkata";

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            log.warn("Invalid timezone '{}' for user {}, falling back to Asia/Kolkata", timezone, userId);
            zoneId = ZoneId.of("Asia/Kolkata");
        }

        // 5. Convert reference instant to user's local time
        Instant instant = (referenceInstant != null) ? referenceInstant : Instant.now();
        LocalTime userLocalTime = instant.atZone(zoneId).toLocalTime();

        // 6. Check quiet hours
        if (isQuietHours(userLocalTime, quietStart, quietEnd)) {
            log.info("Push notification suppressed for user {}: current time {} ({}) falls within quiet hours ({} to {})",
                    userId, userLocalTime, zoneId, quietStart, quietEnd);
            return false;
        }

        return true;
    }

    /**
     * Determines whether the given local time falls within quiet hours.
     * Correctly handles overnight spans (e.g. 22:00 to 07:00) as well as same-day windows (e.g. 13:00 to 15:00).
     */
    public static boolean isQuietHours(LocalTime time, LocalTime quietStart, LocalTime quietEnd) {
        if (time == null || quietStart == null || quietEnd == null) {
            return false;
        }
        if (quietStart.equals(quietEnd)) {
            return false;
        }
        if (quietStart.isAfter(quietEnd)) {
            // Overnight window (e.g. 22:00 to 07:00)
            return !time.isBefore(quietStart) || time.isBefore(quietEnd);
        } else {
            // Same-day window (e.g. 13:00 to 15:00)
            return !time.isBefore(quietStart) && time.isBefore(quietEnd);
        }
    }
}
