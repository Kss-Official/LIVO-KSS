package com.livo.api.modules.goal.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.MilestoneResponse;
import com.livo.api.modules.goal.dto.ReorderMilestonesRequest;
import com.livo.api.modules.goal.dto.UpdateMilestoneRequest;
import com.livo.api.modules.goal.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/milestones")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Milestone management and progression")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public ResponseEntity<ApiResponse<MilestoneResponse>> addMilestone(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId,
            @Valid @RequestBody CreateMilestoneRequest request
    ) {
        MilestoneResponse response = milestoneService.addMilestone(userId, goalId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Milestone added successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getMilestones(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId
    ) {
        List<MilestoneResponse> response = milestoneService.getMilestonesByGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Milestones retrieved successfully"));
    }

    @PutMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody UpdateMilestoneRequest request
    ) {
        MilestoneResponse response = milestoneService.updateMilestone(userId, goalId, milestoneId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Milestone updated successfully"));
    }

    @PatchMapping("/{milestoneId}/toggle")
    public ResponseEntity<ApiResponse<MilestoneResponse>> toggleMilestone(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID milestoneId
    ) {
        MilestoneResponse response = milestoneService.toggleMilestoneCompletion(userId, goalId, milestoneId);
        return ResponseEntity.ok(ApiResponse.success(response, "Milestone completion toggled successfully"));
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> reorderMilestones(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId,
            @Valid @RequestBody ReorderMilestonesRequest request
    ) {
        List<MilestoneResponse> response = milestoneService.reorderMilestones(userId, goalId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Milestones reordered successfully"));
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(
            @CurrentUser UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID milestoneId
    ) {
        milestoneService.deleteMilestone(userId, goalId, milestoneId);
        return ResponseEntity.ok(ApiResponse.success(null, "Milestone deleted successfully"));
    }
}
