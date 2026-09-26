package com.livo.api.modules.plan.service;

import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.DailyPlanResponse;
import com.livo.api.modules.plan.dto.ScheduleBlockResponse;
import com.livo.api.modules.plan.dto.UpdateScheduleBlockRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlanService {

    List<ScheduleBlockResponse> getScheduleBlocks(UUID userId, LocalDate date, LocalDate startDate, LocalDate endDate);

    ScheduleBlockResponse getScheduleBlockById(UUID userId, UUID blockId);

    ScheduleBlockResponse createScheduleBlock(UUID userId, CreateScheduleBlockRequest request);

    ScheduleBlockResponse updateScheduleBlock(UUID userId, UUID blockId, UpdateScheduleBlockRequest request);

    void deleteScheduleBlock(UUID userId, UUID blockId);

    DailyPlanResponse getDailyPlan(UUID userId, LocalDate date);

    com.livo.api.modules.plan.dto.WeekSummaryResponse getWeekSummary(UUID userId, LocalDate startDate);

    DailyPlanResponse autoPlanMyDay(UUID userId, LocalDate date);

    com.livo.api.engines.overload.DayRebalanceEngine.RebalanceProposal rebalanceDay(UUID userId, LocalDate date);

    com.livo.api.modules.plan.dto.ConflictCheckResponse checkConflicts(UUID userId, com.livo.api.modules.plan.dto.ConflictCheckRequest request);

    ScheduleBlockResponse resolveConflict(UUID userId, com.livo.api.modules.plan.dto.ConflictResolutionRequest request);

    ScheduleBlockResponse acceptConflictSuggestion(UUID userId, UUID blockId);

    ScheduleBlockResponse chooseConflictTime(UUID userId, UUID blockId, java.time.LocalTime startTime, java.time.LocalTime endTime);

    ScheduleBlockResponse keepBothConflict(UUID userId, CreateScheduleBlockRequest request);
}
