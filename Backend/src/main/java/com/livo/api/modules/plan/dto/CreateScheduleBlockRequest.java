package com.livo.api.modules.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateScheduleBlockRequest {

    private UUID taskId;

    private UUID eventId;

    private UUID habitId;

    @NotNull(message = "Block date is required")
    private LocalDate blockDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 150, message = "Title must be between 1 and 150 characters")
    private String title;

    @Builder.Default
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category = "PERSONAL";

    @Builder.Default
    private boolean isLocked = false;

    @Builder.Default
    private boolean allowConflict = false;
}
