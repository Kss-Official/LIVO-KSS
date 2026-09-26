package com.livo.api.modules.plan.dto;

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
public class UpdateScheduleBlockRequest {

    private UUID taskId;

    private UUID eventId;

    private UUID habitId;

    private LocalDate blockDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(min = 1, max = 150, message = "Title must be between 1 and 150 characters")
    private String title;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private Boolean isLocked;

    @Builder.Default
    private boolean allowConflict = false;
}
