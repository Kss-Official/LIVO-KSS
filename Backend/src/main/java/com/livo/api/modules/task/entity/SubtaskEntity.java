package com.livo.api.modules.task.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Breakdown step belonging to a parent task.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subtasks")
public class SubtaskEntity extends BaseSyncEntity {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 1;
}
