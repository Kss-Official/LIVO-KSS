package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.AiConversationEntity;
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
public class AiConversationResponse {

    private UUID id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    public static AiConversationResponse fromEntity(AiConversationEntity entity) {
        if (entity == null) return null;
        return AiConversationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
