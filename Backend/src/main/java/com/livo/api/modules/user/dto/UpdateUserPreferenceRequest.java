package com.livo.api.modules.user.dto;

import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPreferenceRequest {

    private LocalTime dailyWakeTime;

    private LocalTime dailySleepTime;

    @DecimalMin(value = "1.00", message = "Max planned hours must be at least 1.00")
    @DecimalMax(value = "24.00", message = "Max planned hours cannot exceed 24.00")
    private BigDecimal maxPlannedHoursPerDay;

    private LocalTime preferredDeepWorkStart;

    private LocalTime preferredDeepWorkEnd;

    private Boolean aiProactiveSuggestions;

    private AiPreferenceLevel aiPreferenceLevel;

    private Boolean aiAllowFinance;

    private Boolean aiAllowHealth;

    private Boolean notificationsEnabled;

    private LocalTime notificationQuietStart;

    private LocalTime notificationQuietEnd;

    private List<LocalDate> dismissedOverloadDates;
}
