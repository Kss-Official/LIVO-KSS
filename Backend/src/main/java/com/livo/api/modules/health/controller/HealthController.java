package com.livo.api.modules.health.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.health.dto.CreateHealthEntryRequest;
import com.livo.api.modules.health.dto.DailyHealthSummaryResponse;
import com.livo.api.modules.health.dto.HealthEntryResponse;
import com.livo.api.modules.health.dto.HealthStatsResponse;
import com.livo.api.modules.health.dto.UpdateHealthEntryRequest;
import com.livo.api.modules.health.entity.enums.HealthType;
import com.livo.api.modules.health.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints for tracking workouts, nutrition, sleep, vitals, and wellness metrics")
public class HealthController {

    private final HealthService healthService;

    @PostMapping("/entries")
    @Operation(summary = "Create a health or wellness entry")
    public ResponseEntity<ApiResponse<HealthEntryResponse>> createHealthEntry(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateHealthEntryRequest request
    ) {
        HealthEntryResponse response = healthService.createHealthEntry(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Health entry created successfully"));
    }

    @GetMapping("/entries")
    @Operation(summary = "List health entries with optional filtering")
    public ResponseEntity<ApiResponse<List<HealthEntryResponse>>> getHealthEntries(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) HealthType healthType,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<HealthEntryResponse> response = healthService.getHealthEntries(currentUser.getId(), healthType, goalId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entries/{id}")
    @Operation(summary = "Get a health entry by ID")
    public ResponseEntity<ApiResponse<HealthEntryResponse>> getHealthEntry(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        HealthEntryResponse response = healthService.getHealthEntry(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Update an existing health entry")
    public ResponseEntity<ApiResponse<HealthEntryResponse>> updateHealthEntry(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHealthEntryRequest request
    ) {
        HealthEntryResponse response = healthService.updateHealthEntry(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Health entry updated successfully"));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Soft-delete a health entry")
    public ResponseEntity<ApiResponse<Void>> deleteHealthEntry(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        healthService.deleteHealthEntry(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Health entry deleted successfully"));
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily health summary including workout minutes, sleep, and entries")
    public ResponseEntity<ApiResponse<DailyHealthSummaryResponse>> getDailySummary(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyHealthSummaryResponse response = healthService.getDailySummary(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregated health and workout analytics over a date range")
    public ResponseEntity<ApiResponse<HealthStatsResponse>> getHealthStats(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        HealthStatsResponse response = healthService.getHealthStats(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
