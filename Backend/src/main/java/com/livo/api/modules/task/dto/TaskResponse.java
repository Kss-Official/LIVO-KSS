package com.livo.api.modules.task.dto;

import com.livo.api.modules.tag.dto.TagResponse;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private UUID milestoneId;
    private UUID tripId;
    private String title;
    private String description;
    private String projectLabel;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer durationMins;
    private Integer actualDurationMins;
    private Instant startedAt;
    private TaskPriority priority;
    private String category;
    private TaskStatus status;
    private String repeatRule;
    private Integer reminderMinutesBefore;
    private Instant completedAt;
    private List<LocalDate> completedDates;
    private List<LocalDate> skippedDates;

    @Builder.Default
    private List<SubtaskDto> subtasks = new ArrayList<>();

    @Builder.Default
    private List<TagResponse> tags = new ArrayList<>();

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse fromEntity(TaskEntity entity, List<SubtaskDto> subtasks, List<TagResponse> tags) {
        if (entity == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .milestoneId(entity.getMilestoneId())
                .tripId(entity.getTripId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .projectLabel(entity.getProjectLabel())
                .dueDate(entity.getDueDate())
                .dueTime(entity.getDueTime())
                .durationMins(entity.getDurationMins())
                .actualDurationMins(entity.getActualDurationMins())
                .startedAt(entity.getStartedAt())
                .priority(entity.getPriority())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .repeatRule(entity.getRepeatRule())
                .reminderMinutesBefore(entity.getReminderMinutesBefore())
                .completedAt(entity.getCompletedAt())
                .completedDates(entity.getCompletedDates() != null ? new ArrayList<>(entity.getCompletedDates()) : new ArrayList<>())
                .skippedDates(entity.getSkippedDates() != null ? new ArrayList<>(entity.getSkippedDates()) : new ArrayList<>())
                .subtasks(subtasks != null ? subtasks : new ArrayList<>())
                .tags(tags != null ? tags : new ArrayList<>())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
