package com.livo.api.modules.sync.dto;

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
public class MutationResultResponse {

    private String idempotencyKey;
    private String domainEntity;
    private String action;
    private OfflineMutationStatus status;
    private UUID entityId;
    private Long serverVersion;
    private String message;
    private Map<String, Object> conflictDetails;
    private Instant appliedAt;
}
