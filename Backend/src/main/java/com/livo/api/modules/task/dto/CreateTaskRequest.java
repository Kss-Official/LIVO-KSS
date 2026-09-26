package com.livo.api.modules.task.dto;

import com.livo.api.modules.task.entity.enums.TaskPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 150, message = "Task title must be between 1 and 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Project label cannot exceed 100 characters")
    private String projectLabel;

    private LocalDate dueDate;

    private LocalTime dueTime;

    @Builder.Default
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    private Integer durationMins = 30;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Builder.Default
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category = "WORK";

    private UUID goalId;

    private UUID milestoneId;

    private UUID tripId;

    @Size(max = 100, message = "Repeat rule cannot exceed 100 characters")
    private String repeatRule;

    private Integer reminderMinutesBefore;

    @Valid
    @Builder.Default
    private List<CreateSubtaskInlineRequest> subtasks = new ArrayList<>();

    @Builder.Default
    private List<UUID> tagIds = new ArrayList<>();
}
