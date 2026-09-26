package com.livo.api.modules.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitHistoryResponse {

    private UUID habitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int currentStreak;
    private int longestStreak;
    private long totalCompletions;
    private double completionRate;

    @Builder.Default
    private List<HabitLogResponse> logs = new ArrayList<>();
}
