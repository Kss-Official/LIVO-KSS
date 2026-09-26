package com.livo.api.modules.attachment.dto;

import com.livo.api.modules.attachment.entity.AttachmentEntity;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
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
public class AttachmentResponse {

    private UUID id;
    private UUID userId;
    private AttachmentEntityType entityType;
    private UUID entityId;
    private String storageKey;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;
    private String formattedSize;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static AttachmentResponse fromEntity(AttachmentEntity entity) {
        if (entity == null) {
            return null;
        }

        String formatted = formatBytes(entity.getFileSizeBytes());

        return AttachmentResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .storageKey(entity.getStorageKey())
                .fileUrl(entity.getFileUrl())
                .fileName(entity.getFileName())
                .mimeType(entity.getMimeType())
                .fileSizeBytes(entity.getFileSizeBytes())
                .formattedSize(formatted)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static String formatBytes(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
