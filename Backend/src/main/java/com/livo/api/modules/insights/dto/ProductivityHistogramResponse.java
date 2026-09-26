package com.livo.api.modules.insights.dto;

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
public class ProductivityHistogramResponse {
    private String peakHourWindow; // e.g. "10:00 AM - 11:00 AM"
    private int peakHour;
    private int totalCompletedTasks;
    private long totalFocusMinutes;
    @Builder.Default
    private List<HourlyProductivityDto> hourlyDistribution = new ArrayList<>();
}
