package com.livo.api.modules.ai.dto;

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
public class AiAnalysisResponse {

    private int workloadPercentage;
    private long pendingTaskCount;
    private long overdueTaskCount;
    private long habitsCompletedToday;
    private long habitsTotalToday;
    private long activeGoalsCount;
    private long todayEventsCount;
    private String summaryHeadline;
    private String executiveSummary;
    @Builder.Default
    private List<AiRecommendationResponse> recommendations = new ArrayList<>();
}
