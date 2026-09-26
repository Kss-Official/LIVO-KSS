package com.livo.api.modules.sync.dto;

import com.livo.api.modules.sync.entity.OfflineMutationEntity;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
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
public class OfflineMutationResponse {

    private UUID id;
    private UUID userId;
    private String idempotencyKey;
    private String domainEntity;
    private String action;
    private Map<String, Object> payload;
    private Instant clientTimestamp;
    private UUID entityId;
    private Long baseVersion;
    private OfflineMutationStatus status;
    private Instant appliedAt;

    public static OfflineMutationResponse fromEntity(OfflineMutationEntity entity) {
        if (entity == null) {
            return null;
        }
        return OfflineMutationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .idempotencyKey(entity.getIdempotencyKey())
                .domainEntity(entity.getDomainEntity())
                .action(entity.getAction())
                .payload(entity.getPayload())
                .clientTimestamp(entity.getClientTimestamp())
                .entityId(entity.getEntityId())
                .baseVersion(entity.getBaseVersion())
                .status(entity.getStatus())
                .appliedAt(entity.getAppliedAt())
                .build();
    }
}
