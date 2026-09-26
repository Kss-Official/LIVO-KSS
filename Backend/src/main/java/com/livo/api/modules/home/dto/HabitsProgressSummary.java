package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitsProgressSummary {
    private int totalScheduledToday;
    private int completedToday;
    private double completionRate;
    @Builder.Default
    private List<HabitDotDto> habits = new ArrayList<>();
}
