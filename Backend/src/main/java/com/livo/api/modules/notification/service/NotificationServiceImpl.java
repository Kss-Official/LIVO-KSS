package com.livo.api.modules.notification.service;

import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.notification.dto.CreateNotificationRequest;
import com.livo.api.modules.notification.dto.NotificationResponse;
import com.livo.api.modules.notification.dto.UnreadCountResponse;
import com.livo.api.modules.notification.entity.NotificationEntity;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public NotificationResponse createNotification(UUID userId, CreateNotificationRequest request) {
        NotificationEntity entity = NotificationEntity.builder()
                .type(request.getType())
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .build();

        entity.setUserId(userId);
        entity.setVersion(1L);

        NotificationEntity saved = notificationRepository.save(entity);
        log.info("Created notification {} for user {}: type={}", saved.getId(), userId, saved.getType());

        if (request.isSendPush()) {
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", saved.getId().toString());
            data.put("type", saved.getType().name());
            if (saved.getRelatedEntityType() != null) {
                data.put("relatedEntityType", saved.getRelatedEntityType());
            }
            if (saved.getRelatedEntityId() != null) {
                data.put("relatedEntityId", saved.getRelatedEntityId().toString());
            }
            pushNotificationService.sendPushNotification(userId, saved.getTitle(), saved.getBody(), data);
        }

        return NotificationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID userId, Boolean unreadOnly, NotificationType type, Integer limit) {
        List<NotificationEntity> notifications;

        if (Boolean.TRUE.equals(unreadOnly)) {
            if (type != null) {
                notifications = notificationRepository.findAllByUserIdAndTypeAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(userId, type);
            } else {
                notifications = notificationRepository.findAllByUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
            }
        } else {
            if (type != null) {
                notifications = notificationRepository.findAllByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(userId, type);
            } else {
                notifications = notificationRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
            }
        }

        int maxLimit = (limit != null && limit > 0) ? limit : 50;
        return notifications.stream()
                .limit(maxLimit)
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification.setVersion(notification.getVersion() != null ? notification.getVersion() + 1 : 1L);
            NotificationEntity updated = notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", notificationId, userId);
            return NotificationResponse.fromEntity(updated);
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<NotificationEntity> unreadList = notificationRepository.findAllByUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        if (unreadList.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        unreadList.forEach(n -> {
            n.setReadAt(now);
            n.setVersion(n.getVersion() != null ? n.getVersion() + 1 : 1L);
        });

        notificationRepository.saveAll(unreadList);
        log.info("Marked {} unread notifications as read for user {}", unreadList.size(), userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID userId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        notification.setDeletedAt(Instant.now());
        notification.setVersion(notification.getVersion() != null ? notification.getVersion() + 1 : 1L);
        notificationRepository.save(notification);
        log.info("Soft-deleted notification {} for user {}", notificationId, userId);
    }

    @Override
    @Transactional
    public NotificationResponse sendSystemNotification(UUID userId, NotificationType type, String title, String body, String relatedType, UUID relatedId) {
        return createNotification(userId, CreateNotificationRequest.builder()
                .type(type)
                .title(title)
                .body(body)
                .relatedEntityType(relatedType)
                .relatedEntityId(relatedId)
                .sendPush(true)
                .build());
    }
}
