package com.livo.api.modules.sync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveConflictRequest {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotBlank(message = "Resolution strategy is required (SERVER_WINS, CLIENT_WINS, MERGE)")
    private String strategy;

    private Map<String, Object> resolvedPayload;
}
