package com.livo.api.modules.plan.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.engines.overload.DayRebalanceEngine;
import com.livo.api.modules.plan.dto.ConflictCheckRequest;
import com.livo.api.modules.plan.dto.ConflictCheckResponse;
import com.livo.api.modules.plan.dto.ConflictResolutionRequest;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.DailyPlanResponse;
import com.livo.api.modules.plan.dto.ScheduleBlockResponse;
import com.livo.api.modules.plan.dto.WeekSummaryResponse;
import com.livo.api.modules.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plan")
@RequiredArgsConstructor
@Tag(name = "Plan", description = "Daily timeline, schedule blocks, sweep-line conflict detection, and workload rebalance")
public class PlanController {

    private final PlanService planService;

    @GetMapping({"/daily", "/today"})
    @Operation(summary = "Get Daily Plan", description = "Daily timeline, events, task blocks, routines, and workload capacity %")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> getDailyPlan(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyPlanResponse response = planService.getDailyPlan(userId, date);
        return ResponseEntity.ok(ApiResponse.success(response, "Daily plan retrieved successfully"));
    }

    @GetMapping("/week-summary")
    @Operation(summary = "Get 7-Day Week Summary Strip", description = "Workload capacity percentage per day across the 7-day strip")
    public ResponseEntity<ApiResponse<WeekSummaryResponse>> getWeekSummary(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        WeekSummaryResponse response = planService.getWeekSummary(userId, startDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Week summary retrieved successfully"));
    }

    @PostMapping("/my-day")
    @Operation(summary = "Auto-Plan My Day", description = "Optimally fits unscheduled tasks into available daytime free slots")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> autoPlanMyDay(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyPlanResponse response = planService.autoPlanMyDay(userId, date);
        return ResponseEntity.ok(ApiResponse.success(response, "Daily plan optimized successfully"));
    }

    @PostMapping("/rebalance")
    @Operation(summary = "Auto-Rebalance Overloaded Day", description = "Generates and applies shifts for flexible tasks on overloaded days")
    public ResponseEntity<ApiResponse<DayRebalanceEngine.RebalanceProposal>> rebalanceDay(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DayRebalanceEngine.RebalanceProposal response = planService.rebalanceDay(userId, date);
        return ResponseEntity.ok(ApiResponse.success(response, "Day rebalance proposal generated and applied"));
    }

    @PostMapping("/conflicts/check")
    @Operation(summary = "Check Schedule Conflicts", description = "Sweep-line interval overlap detection with free slot suggestions")
    public ResponseEntity<ApiResponse<ConflictCheckResponse>> checkConflicts(
            @CurrentUser UUID userId,
            @Valid @RequestBody ConflictCheckRequest request
    ) {
        ConflictCheckResponse response = planService.checkConflicts(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Conflict check completed"));
    }

    @PostMapping("/conflicts/resolve")
    @Operation(summary = "Resolve Schedule Conflict", description = "Resolve conflict via ACCEPT_SUGGESTION, CHOOSE_TIME, or KEEP_BOTH")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> resolveConflict(
            @CurrentUser UUID userId,
            @Valid @RequestBody ConflictResolutionRequest request
    ) {
        ScheduleBlockResponse response = planService.resolveConflict(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Conflict resolved successfully"));
    }

    @PostMapping("/conflicts/accept")
    @Operation(summary = "Accept Suggested Free Slot", description = "Auto-shifts conflicting block to recommended free slot")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> acceptSuggestion(
            @CurrentUser UUID userId,
            @RequestParam UUID blockId
    ) {
        ScheduleBlockResponse response = planService.acceptConflictSuggestion(userId, blockId);
        return ResponseEntity.ok(ApiResponse.success(response, "Suggested free slot accepted"));
    }

    @PostMapping("/conflicts/choose-time")
    @Operation(summary = "Choose Custom Time for Conflict Block", description = "Moves conflicting block to specified custom time")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> chooseTime(
            @CurrentUser UUID userId,
            @RequestParam UUID blockId,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime
    ) {
        ScheduleBlockResponse response = planService.chooseConflictTime(userId, blockId, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(response, "Custom time applied"));
    }

    @PostMapping("/conflicts/keep-both")
    @Operation(summary = "Keep Both Overlapping Blocks", description = "Allows intentional block overlap without raising conflict errors")
    public ResponseEntity<ApiResponse<ScheduleBlockResponse>> keepBoth(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateScheduleBlockRequest request
    ) {
        ScheduleBlockResponse response = planService.keepBothConflict(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule block created with overlap allowed"));
    }
}
