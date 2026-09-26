package com.livo.api.modules.user.entity;

import com.livo.api.common.entity.BaseEntity;
import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User personal preferences and operational parameters for AI,
 * daily scheduling, and notification boundaries.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreferenceEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Builder.Default
    @Column(name = "daily_wake_time", nullable = false)
    private LocalTime dailyWakeTime = LocalTime.of(7, 0);

    @Builder.Default
    @Column(name = "daily_sleep_time", nullable = false)
    private LocalTime dailySleepTime = LocalTime.of(23, 0);

    @Builder.Default
    @Column(name = "max_planned_hours_per_day", nullable = false, precision = 4, scale = 2)
    private BigDecimal maxPlannedHoursPerDay = new BigDecimal("8.00");

    @Column(name = "preferred_deep_work_start")
    private LocalTime preferredDeepWorkStart;

    @Column(name = "preferred_deep_work_end")
    private LocalTime preferredDeepWorkEnd;

    @Builder.Default
    @Column(name = "ai_proactive_suggestions", nullable = false)
    private boolean aiProactiveSuggestions = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_preference_level", nullable = false, length = 20)
    private AiPreferenceLevel aiPreferenceLevel = AiPreferenceLevel.BALANCED;

    @Builder.Default
    @Column(name = "ai_allow_finance", nullable = false)
    private boolean aiAllowFinance = false;

    @Builder.Default
    @Column(name = "ai_allow_health", nullable = false)
    private boolean aiAllowHealth = false;

    @Builder.Default
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Builder.Default
    @Column(name = "notification_quiet_start", nullable = false)
    private LocalTime notificationQuietStart = LocalTime.of(22, 0);

    @Builder.Default
    @Column(name = "notification_quiet_end", nullable = false)
    private LocalTime notificationQuietEnd = LocalTime.of(7, 0);

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "dismissed_overload_dates", columnDefinition = "date[]")
    private List<LocalDate> dismissedOverloadDates = new ArrayList<>();

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 1L;
}
