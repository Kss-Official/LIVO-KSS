package com.livo.api.common.listener;

import com.livo.api.common.event.PushNotificationEvent;
import com.livo.api.modules.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventsListener {

    private final PushNotificationService pushNotificationService;

    @Async("boundedTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPushNotification(PushNotificationEvent event) {
        log.info("Handling PushNotificationEvent for user {} title '{}'", event.userId(), event.title());
        try {
            pushNotificationService.sendPushNotification(event.userId(), event.title(), event.body(), event.dataPayload());
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", event.userId(), e.getMessage());
        }
    }
}
