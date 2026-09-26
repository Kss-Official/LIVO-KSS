package com.livo.api.modules.event.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Calendar Event entity with location, format, priority, and reminder configuration.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class EventEntity extends BaseSyncEntity {

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "location", length = 200)
    private String location;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private EventFormat format = EventFormat.IN_PERSON;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private EventPriority priority = EventPriority.MEDIUM;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "GENERAL";

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;

    @Column(name = "repeat_rule", length = 100)
    private String repeatRule;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skipped_dates", columnDefinition = "date[]")
    private List<LocalDate> skippedDates = new ArrayList<>();

    @Column(name = "notes", length = 1000)
    private String notes;
}
