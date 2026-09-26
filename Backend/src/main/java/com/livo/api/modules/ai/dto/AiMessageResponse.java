package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.AiMessageEntity;
import com.livo.api.modules.ai.entity.enums.AiMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageResponse {

    private UUID id;
    private UUID conversationId;
    private AiMessageRole role;
    private String content;
    private String operatingMode;
    private Map<String, Object> cardsJson;
    private List<String> suggestedRepliesJson;
    private Instant createdAt;

    public static AiMessageResponse fromEntity(AiMessageEntity entity) {
        if (entity == null) return null;
        return AiMessageResponse.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .role(entity.getRole())
                .content(entity.getContent())
                .operatingMode(entity.getOperatingMode())
                .cardsJson(entity.getCardsJson())
                .suggestedRepliesJson(entity.getSuggestedRepliesJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
