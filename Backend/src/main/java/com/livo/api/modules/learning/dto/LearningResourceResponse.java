package com.livo.api.modules.learning.dto;

import com.livo.api.modules.learning.entity.LearningResourceEntity;
import com.livo.api.modules.learning.entity.enums.LearningResourceType;
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
public class LearningResourceResponse {

    private UUID id;
    private UUID learningItemId;
    private UUID userId;
    private LearningResourceType resourceType;
    private String title;
    private String url;
    private String notes;
    private boolean isCompleted;
    private int sortOrder;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static LearningResourceResponse fromEntity(LearningResourceEntity entity) {
        if (entity == null) {
            return null;
        }
        return LearningResourceResponse.builder()
                .id(entity.getId())
                .learningItemId(entity.getLearningItemId())
                .userId(entity.getUserId())
                .resourceType(entity.getResourceType())
                .title(entity.getTitle())
                .url(entity.getUrl())
                .notes(entity.getNotes())
                .isCompleted(entity.isCompleted())
                .sortOrder(entity.getSortOrder())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
