package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.AiAuditLogEntity;
import com.livo.api.modules.ai.entity.enums.AiAuditSource;
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
public class AiAuditLogResponse {

    private UUID id;
    private UUID proposalId;
    private String actionType;
    private String entityType;
    private UUID entityId;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private AiAuditSource source;
    private Instant createdAt;

    public static AiAuditLogResponse fromEntity(AiAuditLogEntity entity) {
        if (entity == null) return null;
        return AiAuditLogResponse.builder()
                .id(entity.getId())
                .proposalId(entity.getProposalId())
                .actionType(entity.getActionType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
