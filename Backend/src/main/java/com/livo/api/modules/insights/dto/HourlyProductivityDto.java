package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyProductivityDto {
    private int hour; // 0 to 23
    private String label; // "12 AM", "9 AM", "2 PM", etc.
    private int completedTasksCount;
    private long focusMinutes;
    private boolean isPeakHour;
}
