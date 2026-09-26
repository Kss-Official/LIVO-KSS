package com.livo.api.modules.habit.dto;

import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHabitRequest {

    private UUID goalId;

    @Size(max = 150, message = "Habit title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 300, message = "Motivation note cannot exceed 300 characters")
    private String motivationNote;

    @Size(max = 50, message = "Icon key cannot exceed 50 characters")
    private String iconKey;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color hex must be a valid 6-character hex code (e.g. #10B981)")
    private String colorHex;

    private HabitFrequency frequencyType;

    private List<Integer> customDays;

    @Min(value = 1, message = "Target count must be at least 1")
    private Integer targetCount;

    @Size(max = 30, message = "Target unit cannot exceed 30 characters")
    private String targetUnit;

    private HabitPreferredTime preferredTime;
    private LocalTime preferredClockTime;

    private Boolean reminderEnabled;
    private LocalTime reminderTime;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isArchived;
}
