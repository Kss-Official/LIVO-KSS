package com.livo.api.modules.health.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity tracking physical and mental wellness records, vitals, workouts, and medical checkups.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_entries")
public class HealthEntryEntity extends BaseSyncEntity {

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_type", nullable = false, length = 20)
    private HealthType healthType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "entry_time")
    private LocalTime entryTime;

    @Column(name = "duration_mins")
    private Integer durationMins;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensity", length = 10)
    private HealthIntensity intensity;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metricsJson = new HashMap<>();

    @Column(name = "notes", length = 500)
    private String notes;
}
