package com.livo.api.modules.plan.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Daily timeline schedule block representing dedicated time slots for tasks, events, or habits.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedule_blocks")
public class ScheduleBlockEntity extends BaseSyncEntity {

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "habit_id")
    private UUID habitId;

    @Column(name = "block_date", nullable = false)
    private LocalDate blockDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "PERSONAL";

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;
}
