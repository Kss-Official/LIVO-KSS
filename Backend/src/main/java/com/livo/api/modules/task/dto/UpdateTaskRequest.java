package com.livo.api.modules.task.dto;

import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 1, max = 150, message = "Task title must be between 1 and 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Project label cannot exceed 100 characters")
    private String projectLabel;

    private LocalDate dueDate;

    private LocalTime dueTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes")
    private Integer durationMins;

    @Min(value = 0, message = "Actual duration must be non-negative")
    private Integer actualDurationMins;

    private TaskPriority priority;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private TaskStatus status;

    private UUID goalId;

    private UUID milestoneId;

    private UUID tripId;

    @Size(max = 100, message = "Repeat rule cannot exceed 100 characters")
    private String repeatRule;

    private Integer reminderMinutesBefore;

    private List<UUID> tagIds;
}
