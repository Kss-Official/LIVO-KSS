package com.livo.api.common.event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published to dispatch mobile push notifications via FCM.
 */
public record PushNotificationEvent(
        UUID userId,
        String title,
        String body,
        Map<String, String> dataPayload
) {
}
