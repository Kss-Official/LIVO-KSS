package com.livo.api.modules.routine.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Committed daily recurring routines (e.g. Morning Routine, Lunch Break, Bedtime).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "routines")
public class RoutineEntity extends BaseSyncEntity {

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "days_of_week", columnDefinition = "int[]")
    private List<Integer> daysOfWeek = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
