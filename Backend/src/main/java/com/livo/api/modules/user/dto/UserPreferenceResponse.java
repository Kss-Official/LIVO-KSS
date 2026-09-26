package com.livo.api.modules.user.dto;

import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {

    private UUID id;
    private UUID userId;
    private LocalTime dailyWakeTime;
    private LocalTime dailySleepTime;
    private BigDecimal maxPlannedHoursPerDay;
    private LocalTime preferredDeepWorkStart;
    private LocalTime preferredDeepWorkEnd;
    private boolean aiProactiveSuggestions;
    private AiPreferenceLevel aiPreferenceLevel;
    private boolean aiAllowFinance;
    private boolean aiAllowHealth;
    private boolean notificationsEnabled;
    private LocalTime notificationQuietStart;
    private LocalTime notificationQuietEnd;
    private List<LocalDate> dismissedOverloadDates;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserPreferenceResponse fromEntity(UserPreferenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserPreferenceResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .dailyWakeTime(entity.getDailyWakeTime())
                .dailySleepTime(entity.getDailySleepTime())
                .maxPlannedHoursPerDay(entity.getMaxPlannedHoursPerDay())
                .preferredDeepWorkStart(entity.getPreferredDeepWorkStart())
                .preferredDeepWorkEnd(entity.getPreferredDeepWorkEnd())
                .aiProactiveSuggestions(entity.isAiProactiveSuggestions())
                .aiPreferenceLevel(entity.getAiPreferenceLevel())
                .aiAllowFinance(entity.isAiAllowFinance())
                .aiAllowHealth(entity.isAiAllowHealth())
                .notificationsEnabled(entity.isNotificationsEnabled())
                .notificationQuietStart(entity.getNotificationQuietStart())
                .notificationQuietEnd(entity.getNotificationQuietEnd())
                .dismissedOverloadDates(entity.getDismissedOverloadDates())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
