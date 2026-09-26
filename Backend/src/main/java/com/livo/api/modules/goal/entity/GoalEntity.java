package com.livo.api.modules.goal.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Long-term Goal entity supporting percentage, numerical, and milestone-based progress tracking.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goals")
public class GoalEntity extends BaseSyncEntity {

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "related_area", length = 20)
    private String relatedArea;

    @Column(name = "target_description", length = 200)
    private String targetDescription;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "PERSONAL";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private GoalPriority priority = GoalPriority.MEDIUM;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_tracking_type", nullable = false, length = 20)
    private GoalTrackingType progressTrackingType = GoalTrackingType.PERCENTAGE;

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Builder.Default
    @Column(name = "current_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "reminder_frequency", length = 20)
    private String reminderFrequency;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.IN_PROGRESS;
}
