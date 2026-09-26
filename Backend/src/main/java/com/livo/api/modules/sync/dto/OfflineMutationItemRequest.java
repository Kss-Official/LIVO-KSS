package com.livo.api.modules.sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMutationItemRequest {

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
    private String idempotencyKey;

    @NotBlank(message = "Domain entity is required (e.g. TASK, GOAL, HABIT, EVENT, EXPENSE)")
    @Size(max = 50, message = "Domain entity cannot exceed 50 characters")
    private String domainEntity;

    @NotBlank(message = "Action is required (CREATE, UPDATE, DELETE)")
    @Size(max = 20, message = "Action cannot exceed 20 characters")
    private String action;

    @NotNull(message = "Client timestamp is required")
    private Instant clientTimestamp;

    private UUID entityId;

    private Long baseVersion;

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();
}
