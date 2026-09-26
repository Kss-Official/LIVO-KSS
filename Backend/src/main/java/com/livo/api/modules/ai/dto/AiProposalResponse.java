package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
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
public class AiProposalResponse {

    private UUID id;
    private UUID conversationId;
    private String actionType;
    private Map<String, Object> proposalPayload;
    private AiProposalStatus status;
    private Instant confirmedAt;
    private Instant revertedAt;
    private Instant expiresAt;
    private Instant createdAt;

    public static AiProposalResponse fromEntity(AiProposalEntity entity) {
        if (entity == null) return null;
        return AiProposalResponse.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .actionType(entity.getActionType())
                .proposalPayload(entity.getProposalPayload())
                .status(entity.getStatus())
                .confirmedAt(entity.getConfirmedAt())
                .revertedAt(entity.getRevertedAt())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
