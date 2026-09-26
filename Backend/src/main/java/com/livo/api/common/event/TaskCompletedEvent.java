package com.livo.api.common.event;

import java.util.UUID;

/**
 * Domain event published when a task is marked as completed.
 */
public record TaskCompletedEvent(
        UUID taskId,
        UUID userId,
        UUID goalId,
        String taskTitle
) {
}
