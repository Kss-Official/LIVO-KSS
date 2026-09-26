package com.livo.api.modules.goal.entity;

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
import java.util.UUID;

/**
 * Milestone checkpoint linked to a parent Goal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "milestones")
public class MilestoneEntity extends BaseSyncEntity {

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 1;

    @Builder.Default
    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;
}
