package com.livo.api.modules.notification.dto;

import com.livo.api.modules.notification.entity.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Notification title is required")
    @Size(max = 150, message = "Notification title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Notification body is required")
    @Size(max = 500, message = "Notification body cannot exceed 500 characters")
    private String body;

    @Size(max = 20, message = "Related entity type cannot exceed 20 characters")
    private String relatedEntityType;

    private UUID relatedEntityId;

    @Builder.Default
    private boolean sendPush = true;
}
