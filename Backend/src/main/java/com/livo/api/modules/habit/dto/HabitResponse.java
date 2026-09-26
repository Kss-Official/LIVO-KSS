package com.livo.api.modules.habit.dto;

import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String title;
    private String description;
    private String motivationNote;
    private String iconKey;
    private String colorHex;
    private HabitFrequency frequencyType;

    @Builder.Default
    private List<Integer> customDays = new ArrayList<>();

    private int targetCount;
    private String targetUnit;
    private HabitPreferredTime preferredTime;
    private LocalTime preferredClockTime;
    private boolean reminderEnabled;
    private LocalTime reminderTime;
    private int currentStreak;
    private int longestStreak;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isArchived;

    private boolean completedToday;
    private int todayCountCompleted;
    private long totalCompletions;

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static HabitResponse fromEntity(HabitEntity entity, boolean completedToday, int todayCountCompleted, long totalCompletions) {
        if (entity == null) {
            return null;
        }
        return HabitResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .motivationNote(entity.getMotivationNote())
                .iconKey(entity.getIconKey())
                .colorHex(entity.getColorHex())
                .frequencyType(entity.getFrequencyType())
                .customDays(entity.getCustomDays() != null ? new ArrayList<>(entity.getCustomDays()) : new ArrayList<>())
                .targetCount(entity.getTargetCount())
                .targetUnit(entity.getTargetUnit())
                .preferredTime(entity.getPreferredTime())
                .preferredClockTime(entity.getPreferredClockTime())
                .reminderEnabled(entity.isReminderEnabled())
                .reminderTime(entity.getReminderTime())
                .currentStreak(entity.getCurrentStreak())
                .longestStreak(entity.getLongestStreak())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isArchived(entity.isArchived())
                .completedToday(completedToday)
                .todayCountCompleted(todayCountCompleted)
                .totalCompletions(totalCompletions)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
