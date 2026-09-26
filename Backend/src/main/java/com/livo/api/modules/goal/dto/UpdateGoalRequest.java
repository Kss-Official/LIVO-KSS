package com.livo.api.modules.goal.dto;

import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateGoalRequest {

    @Size(max = 150, message = "Goal title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 20, message = "Related area cannot exceed 20 characters")
    private String relatedArea;

    @Size(max = 200, message = "Target description cannot exceed 200 characters")
    private String targetDescription;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private GoalPriority priority;

    private LocalDate targetDate;

    private GoalTrackingType progressTrackingType;

    @DecimalMin(value = "0.01", message = "Target value must be greater than zero")
    private BigDecimal targetValue;

    @DecimalMin(value = "0.0", message = "Current value cannot be negative")
    private BigDecimal currentValue;

    @Size(max = 30, message = "Unit cannot exceed 30 characters")
    private String unit;

    @Size(max = 20, message = "Reminder frequency cannot exceed 20 characters")
    private String reminderFrequency;

    private GoalStatus status;
}
