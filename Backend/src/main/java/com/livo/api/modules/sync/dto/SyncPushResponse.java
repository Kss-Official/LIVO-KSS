package com.livo.api.modules.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncPushResponse {

    private List<MutationResultResponse> results;
    private int appliedCount;
    private int conflictCount;
    private int rejectedCount;
    private Instant serverTimestamp;
}
