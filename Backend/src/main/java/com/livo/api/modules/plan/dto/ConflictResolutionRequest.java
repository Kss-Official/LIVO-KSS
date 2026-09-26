package com.livo.api.modules.plan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResolutionRequest {

    public enum ConflictAction {
        ACCEPT_SUGGESTION,
        CHOOSE_TIME,
        KEEP_BOTH
    }

    @NotNull(message = "Conflict action is required")
    private ConflictAction action;

    private UUID scheduleBlockId;

    private CreateScheduleBlockRequest newBlock;

    private LocalDate targetDate;

    private LocalTime chosenStartTime;

    private LocalTime chosenEndTime;
}
