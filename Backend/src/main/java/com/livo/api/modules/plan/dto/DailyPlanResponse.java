package com.livo.api.modules.plan.dto;

import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.routine.dto.RoutineResponse;
import com.livo.api.modules.task.dto.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanResponse {

    private LocalDate date;

    @Builder.Default
    private List<ScheduleBlockResponse> scheduleBlocks = new ArrayList<>();

    @Builder.Default
    private List<EventResponse> events = new ArrayList<>();

    @Builder.Default
    private List<RoutineResponse> routines = new ArrayList<>();

    @Builder.Default
    private List<TaskResponse> tasks = new ArrayList<>();

    private BigDecimal plannedWorkloadHours;
    private BigDecimal maxPlannedHours;
    private boolean isOverloaded;
}
