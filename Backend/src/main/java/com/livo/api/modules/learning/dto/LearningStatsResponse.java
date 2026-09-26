package com.livo.api.modules.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStatsResponse {

    private long totalItems;
    private long completedItems;
    private long inProgressItems;
    private long savedItems;
    private int totalStudyMinutes;
    private double totalStudyHours;
    private long totalSessions;

    @Builder.Default
    private Map<String, Integer> studyMinutesByCategory = new HashMap<>();
}
