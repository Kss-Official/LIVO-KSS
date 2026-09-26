package com.livo.api.modules.routine.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoutineRequest {

    @Size(min = 1, max = 100, message = "Routine title must be between 1 and 100 characters")
    private String title;

    private LocalTime startTime;

    private LocalTime endTime;

    private List<Integer> daysOfWeek;

    private Boolean isActive;
}
