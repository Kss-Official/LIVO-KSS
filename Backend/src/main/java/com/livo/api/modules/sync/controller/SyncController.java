package com.livo.api.modules.sync.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.sync.dto.MutationResultResponse;
import com.livo.api.modules.sync.dto.OfflineMutationResponse;
import com.livo.api.modules.sync.dto.ResolveConflictRequest;
import com.livo.api.modules.sync.dto.SyncPullResponse;
import com.livo.api.modules.sync.dto.SyncPushRequest;
import com.livo.api.modules.sync.dto.SyncPushResponse;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
import com.livo.api.modules.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Endpoints for optimistic offline queued mutations, delta synchronization, and client-server conflict reconciliation")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/push")
    @Operation(summary = "Batch push client offline mutations with per-user idempotency and conflict detection")
    public ResponseEntity<ApiResponse<SyncPushResponse>> pushMutations(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SyncPushRequest request
    ) {
        SyncPushResponse response = syncService.pushMutations(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Mutations processed successfully"));
    }

    @GetMapping("/pull")
    @Operation(summary = "Delta sync pull: fetch changes across domains updated since lastSyncTimestamp")
    public ResponseEntity<ApiResponse<SyncPullResponse>> pullChangesGet(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        SyncPullResponse response = syncService.pullChanges(currentUser.getId(), since);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/pull")
    @Operation(summary = "Delta sync pull via POST with optional timestamp parameter")
    public ResponseEntity<ApiResponse<SyncPullResponse>> pullChangesPost(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        SyncPullResponse response = syncService.pullChanges(currentUser.getId(), since);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mutations")
    @Operation(summary = "Get user offline mutation audit trail with optional status filter")
    public ResponseEntity<ApiResponse<List<OfflineMutationResponse>>> getMutations(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) OfflineMutationStatus status
    ) {
        List<OfflineMutationResponse> response = syncService.getMutations(currentUser.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/resolve-conflict")
    @Operation(summary = "Reconcile a concurrency conflict with SERVER_WINS, CLIENT_WINS, or MERGE strategy")
    public ResponseEntity<ApiResponse<MutationResultResponse>> resolveConflict(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ResolveConflictRequest request
    ) {
        MutationResultResponse response = syncService.resolveConflict(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Conflict resolved successfully"));
    }
}
