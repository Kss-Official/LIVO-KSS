package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.entity.enums.AiPermissionLevel;
import com.livo.api.modules.ai.entity.enums.AiRecommendationPriority;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import com.livo.api.modules.ai.entity.enums.AiRelatedEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponse {

    private UUID id;
    private AiRecommendationType type;
    private String title;
    private String reason;
    private AiRecommendationPriority priority;
    private AiPermissionLevel permissionLevel;
    private AiRelatedEntityType relatedEntityType;
    private UUID relatedEntityId;
    private String actionType;
    private Map<String, Object> actionPayload;
    private Instant createdAt;

    public static AiRecommendationResponse fromEntity(AiRecommendationEntity entity) {
        if (entity == null) return null;
        return AiRecommendationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .reason(entity.getReason())
                .priority(entity.getRecommendationPriority())
                .permissionLevel(entity.getPermissionLevel())
                .relatedEntityType(entity.getRelatedEntityType())
                .relatedEntityId(entity.getRelatedEntityId())
                .actionType(entity.getActionType())
                .actionPayload(entity.getActionPayload())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
