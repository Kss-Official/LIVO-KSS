package com.livo.api.modules.goal.dto;

import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreateGoalRequest {

    @NotBlank(message = "Goal title cannot be blank")
    @Size(max = 150, message = "Goal title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 20, message = "Related area cannot exceed 20 characters")
    private String relatedArea;

    @Size(max = 200, message = "Target description cannot exceed 200 characters")
    private String targetDescription;

    @Builder.Default
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category = "PERSONAL";

    @Builder.Default
    private GoalPriority priority = GoalPriority.MEDIUM;

    private LocalDate targetDate;

    @Builder.Default
    private GoalTrackingType progressTrackingType = GoalTrackingType.PERCENTAGE;

    @DecimalMin(value = "0.01", message = "Target value must be greater than zero")
    private BigDecimal targetValue;

    @Builder.Default
    @DecimalMin(value = "0.0", message = "Current value cannot be negative")
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Size(max = 30, message = "Unit cannot exceed 30 characters")
    private String unit;

    @Size(max = 20, message = "Reminder frequency cannot exceed 20 characters")
    private String reminderFrequency;

    @Builder.Default
    private GoalStatus status = GoalStatus.IN_PROGRESS;
}
