package com.livo.api.modules.habit.dto;

import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHabitRequest {

    private UUID goalId;

    @NotBlank(message = "Habit title cannot be blank")
    @Size(max = 150, message = "Habit title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 300, message = "Motivation note cannot exceed 300 characters")
    private String motivationNote;

    @Builder.Default
    @Size(max = 50, message = "Icon key cannot exceed 50 characters")
    private String iconKey = "WATER";

    @Builder.Default
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color hex must be a valid 6-character hex code (e.g. #10B981)")
    private String colorHex = "#10B981";

    @Builder.Default
    @NotNull(message = "Frequency type is required")
    private HabitFrequency frequencyType = HabitFrequency.DAILY;

    @Builder.Default
    private List<Integer> customDays = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));

    @Builder.Default
    @Min(value = 1, message = "Target count must be at least 1")
    private int targetCount = 1;

    @Builder.Default
    @Size(max = 30, message = "Target unit cannot exceed 30 characters")
    private String targetUnit = "times";

    private HabitPreferredTime preferredTime;
    private LocalTime preferredClockTime;

    @Builder.Default
    private boolean reminderEnabled = false;
    private LocalTime reminderTime;

    private LocalDate startDate;
    private LocalDate endDate;
}
