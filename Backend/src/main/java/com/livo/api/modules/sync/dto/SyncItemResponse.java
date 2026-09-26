package com.livo.api.modules.sync.dto;

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
public class SyncItemResponse {

    private UUID id;
    private String domainEntity;
    private Long version;
    private boolean deleted;
    private Instant deletedAt;
    private Instant updatedAt;
    private Map<String, Object> data;
}
