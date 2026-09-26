package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitDotDto {
    private UUID habitId;
    private String title;
    private String iconKey;
    private String colorHex;
    private int currentStreak;
    private boolean isCheckedInToday;
}
