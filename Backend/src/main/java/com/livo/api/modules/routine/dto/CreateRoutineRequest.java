package com.livo.api.modules.routine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoutineRequest {

    @NotBlank(message = "Routine title is required")
    @Size(min = 1, max = 100, message = "Routine title must be between 1 and 100 characters")
    private String title;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Builder.Default
    private List<Integer> daysOfWeek = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));

    @Builder.Default
    private boolean isActive = true;
}
