package com.livo.api.modules.routine.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.routine.dto.CreateRoutineRequest;
import com.livo.api.modules.routine.dto.RoutineResponse;
import com.livo.api.modules.routine.dto.UpdateRoutineRequest;
import com.livo.api.modules.routine.service.RoutineService;
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
@RequestMapping("/api/v1/routines")
@RequiredArgsConstructor
@Tag(name = "Routines", description = "Committed daily routines (morning, lunch, bedtime)")
public class RoutineController {

    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> getRoutines(
            @CurrentUser UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<RoutineResponse> routines = routineService.getRoutines(userId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(routines, "Routines retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutineResponse>> getRoutineById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        RoutineResponse response = routineService.getRoutineById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Routine retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoutineResponse>> createRoutine(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateRoutineRequest request
    ) {
        RoutineResponse response = routineService.createRoutine(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Routine created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutineResponse>> updateRoutine(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoutineRequest request
    ) {
        RoutineResponse response = routineService.updateRoutine(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Routine updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoutine(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        routineService.deleteRoutine(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Routine deleted successfully"));
    }
}
