package com.livo.api.modules.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalProgressRequest {

    @NotNull(message = "New current value is required")
    @DecimalMin(value = "0.0", message = "Current value cannot be negative")
    private BigDecimal currentValue;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private LocalDate recordedDate;
}
