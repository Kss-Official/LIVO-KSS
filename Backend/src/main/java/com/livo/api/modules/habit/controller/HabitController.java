package com.livo.api.modules.habit.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitHistoryResponse;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.dto.UpdateHabitRequest;
import com.livo.api.modules.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
@Tag(name = "Habits", description = "Habits, check-ins, streak recalculations, and consistency heatmap")
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HabitResponse>>> getHabits(
            @CurrentUser UUID userId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) UUID goalId
    ) {
        List<HabitResponse> response = habitService.getHabits(userId, archived, goalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Habits retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitResponse>> getHabitById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        HabitResponse response = habitService.getHabitById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HabitResponse>> createHabit(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateHabitRequest request
    ) {
        HabitResponse response = habitService.createHabit(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Habit created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitResponse>> updateHabit(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHabitRequest request
    ) {
        HabitResponse response = habitService.updateHabit(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHabit(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        habitService.deleteHabit(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Habit deleted successfully"));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<HabitResponse>> toggleArchive(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        HabitResponse response = habitService.toggleArchive(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit archive status toggled successfully"));
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<ApiResponse<HabitResponse>> logHabit(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody LogHabitRequest request
    ) {
        HabitResponse response = habitService.logHabit(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit logged successfully"));
    }

    @DeleteMapping("/{id}/log")
    public ResponseEntity<ApiResponse<HabitResponse>> unlogHabit(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate
    ) {
        HabitResponse response = habitService.unlogHabit(userId, id, logDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit log removed successfully"));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<HabitHistoryResponse>> getHabitHistory(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        HabitHistoryResponse response = habitService.getHabitHistory(userId, id, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Habit history retrieved successfully"));
    }
}
