package com.livo.api.modules.sync.service;

import com.livo.api.modules.sync.dto.MutationResultResponse;
import com.livo.api.modules.sync.dto.OfflineMutationResponse;
import com.livo.api.modules.sync.dto.ResolveConflictRequest;
import com.livo.api.modules.sync.dto.SyncPullResponse;
import com.livo.api.modules.sync.dto.SyncPushRequest;
import com.livo.api.modules.sync.dto.SyncPushResponse;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SyncService {

    SyncPushResponse pushMutations(UUID userId, SyncPushRequest request);

    SyncPullResponse pullChanges(UUID userId, Instant since);

    List<OfflineMutationResponse> getMutations(UUID userId, OfflineMutationStatus status);

    MutationResultResponse resolveConflict(UUID userId, ResolveConflictRequest request);
}
