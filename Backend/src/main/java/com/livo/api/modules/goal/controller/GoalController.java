package com.livo.api.modules.goal.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.GoalProgressHistoryResponse;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.dto.UpdateGoalProgressRequest;
import com.livo.api.modules.goal.dto.UpdateGoalRequest;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Goals, milestones, progress tracking, and AI milestone breakdown")
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @CurrentUser UUID userId,
            @RequestParam(required = false) GoalStatus status,
            @RequestParam(required = false) String relatedArea
    ) {
        List<GoalResponse> response = goalService.getAllGoals(userId, status, relatedArea);
        return ResponseEntity.ok(ApiResponse.success(response, "Goals retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        GoalResponse response = goalService.getGoalById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateGoalRequest request
    ) {
        GoalResponse response = goalService.createGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Goal created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request
    ) {
        GoalResponse response = goalService.updateGoal(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        goalService.deleteGoal(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoalProgress(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalProgressRequest request
    ) {
        GoalResponse response = goalService.updateGoalProgress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal progress updated successfully"));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<GoalProgressHistoryResponse>>> getGoalProgressHistory(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        List<GoalProgressHistoryResponse> response = goalService.getGoalProgressHistory(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal progress history retrieved successfully"));
    }
}
