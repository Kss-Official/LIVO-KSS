package com.livo.api.modules.notification.dto;

import com.livo.api.modules.notification.entity.NotificationEntity;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String body;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private boolean read;
    private Instant readAt;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationResponse fromEntity(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .title(entity.getTitle())
                .body(entity.getBody())
                .relatedEntityType(entity.getRelatedEntityType())
                .relatedEntityId(entity.getRelatedEntityId())
                .read(entity.getReadAt() != null)
                .readAt(entity.getReadAt())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
