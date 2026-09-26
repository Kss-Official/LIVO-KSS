package com.livo.api.modules.plan.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.ScheduleBlockResponse;
import com.livo.api.modules.plan.dto.UpdateScheduleBlockRequest;
import com.livo.api.modules.plan.service.PlanService;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedule-blocks")
@RequiredArgsConstructor
@Tag(name = "Plan", description = "Schedule blocks management")
public class ScheduleBlockController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleBlockResponse>>> getScheduleBlocks(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ScheduleBlockResponse> blocks = planService.getScheduleBlocks(userId, date, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(blocks, "Schedule blocks retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> getScheduleBlockById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        ScheduleBlockResponse block = planService.getScheduleBlockById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(block, "Schedule block retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> createScheduleBlock(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateScheduleBlockRequest request
    ) {
        ScheduleBlockResponse response = planService.createScheduleBlock(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Schedule block created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> updateScheduleBlock(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateScheduleBlockRequest request
    ) {
        ScheduleBlockResponse response = planService.updateScheduleBlock(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule block updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScheduleBlock(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        planService.deleteScheduleBlock(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule block deleted successfully"));
    }
}
