package com.livo.api.modules.learning.entity;

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
 * Entity tracking time spent in study sessions for a learning item.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_sessions")
public class LearningSessionEntity extends BaseSyncEntity {

    @Column(name = "learning_item_id", nullable = false)
    private UUID learningItemId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "notes", length = 500)
    private String notes;
}
