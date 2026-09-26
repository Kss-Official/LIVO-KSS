package com.livo.api.modules.notification.service;

import com.livo.api.modules.notification.dto.CreateNotificationRequest;
import com.livo.api.modules.notification.dto.NotificationResponse;
import com.livo.api.modules.notification.dto.UnreadCountResponse;
import com.livo.api.modules.notification.entity.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(UUID userId, CreateNotificationRequest request);

    List<NotificationResponse> getNotifications(UUID userId, Boolean unreadOnly, NotificationType type, Integer limit);

    UnreadCountResponse getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID userId, UUID notificationId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID userId, UUID notificationId);

    NotificationResponse sendSystemNotification(UUID userId, NotificationType type, String title, String body, String relatedType, UUID relatedId);
}
