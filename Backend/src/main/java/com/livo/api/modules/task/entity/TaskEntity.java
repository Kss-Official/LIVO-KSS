package com.livo.api.modules.task.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core Task entity supporting priority scoring, repeating rules,
 * subtask breakdowns, and multi-tenant isolation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class TaskEntity extends BaseSyncEntity {

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "project_label", length = 100)
    private String projectLabel;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "due_time")
    private LocalTime dueTime;

    @Builder.Default
    @Column(name = "duration_mins", nullable = false)
    private Integer durationMins = 30;

    @Column(name = "actual_duration_mins")
    private Integer actualDurationMins;

    @Column(name = "started_at")
    private Instant startedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "WORK";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "repeat_rule", length = 100)
    private String repeatRule;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "completed_dates", columnDefinition = "date[]")
    private List<LocalDate> completedDates = new ArrayList<>();

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skipped_dates", columnDefinition = "date[]")
    private List<LocalDate> skippedDates = new ArrayList<>();

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    public void normalizeDueTime() {
        if (this.dueTime != null) {
            this.dueTime = this.dueTime.withNano(0);
        }
    }
}
