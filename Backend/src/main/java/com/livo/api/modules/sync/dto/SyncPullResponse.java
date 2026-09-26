package com.livo.api.modules.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncPullResponse {

    private Instant serverTimestamp;
    private Instant since;
    @Builder.Default
    private List<SyncItemResponse> tasks = new ArrayList<>();
    @Builder.Default
    private List<SyncItemResponse> goals = new ArrayList<>();
    @Builder.Default
    private List<SyncItemResponse> habits = new ArrayList<>();
    @Builder.Default
    private List<SyncItemResponse> events = new ArrayList<>();
    @Builder.Default
    private List<SyncItemResponse> expenses = new ArrayList<>();
    private int totalChanges;
}
