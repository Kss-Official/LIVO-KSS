package com.livo.api.modules.habit.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogHabitRequest {

    private LocalDate logDate;

    @Builder.Default
    @Min(value = 1, message = "Count completed must be at least 1")
    private int countCompleted = 1;
}
