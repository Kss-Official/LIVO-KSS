package com.livo.api.modules.habit.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Habit entity supporting streak tracking, custom frequencies, and milestone goals.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "habits")
public class HabitEntity extends BaseSyncEntity {

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "motivation_note", length = 300)
    private String motivationNote;

    @Builder.Default
    @Column(name = "icon_key", nullable = false, length = 50)
    private String iconKey = "WATER";

    @Builder.Default
    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex = "#10B981";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 20)
    private HabitFrequency frequencyType = HabitFrequency.DAILY;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "custom_days", columnDefinition = "int[]")
    private List<Integer> customDays = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));

    @Builder.Default
    @Column(name = "target_count", nullable = false)
    private int targetCount = 1;

    @Builder.Default
    @Column(name = "target_unit", nullable = false, length = 30)
    private String targetUnit = "times";

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_time", length = 20)
    private HabitPreferredTime preferredTime;

    @Column(name = "preferred_clock_time")
    private LocalTime preferredClockTime;

    @Builder.Default
    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled = false;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Builder.Default
    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Builder.Default
    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;
}
