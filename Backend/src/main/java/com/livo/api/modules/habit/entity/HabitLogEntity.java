package com.livo.api.modules.habit.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Daily check-in log for a habit, recording completed count and timestamp.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "habit_logs")
public class HabitLogEntity extends BaseSyncEntity {

    @Column(name = "habit_id", nullable = false)
    private UUID habitId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Builder.Default
    @Column(name = "count_completed", nullable = false)
    private int countCompleted = 1;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.loggedAt == null) {
            this.loggedAt = Instant.now();
        }
    }
}
