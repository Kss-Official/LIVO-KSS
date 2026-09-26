package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDashboardResponse {
    private LocalDate date;
    private String greeting;
    private DailyQuoteDto dailyQuote;
    private DailyLifeScoreDto dailyLifeScore;
    private ActiveTaskCard activeTask;
    @Builder.Default
    private List<PriorityTaskCard> priorityCarousel = new ArrayList<>();
    private NextUpCard nextUpSchedule;
    private HabitsProgressSummary habitsProgress;
    private GoalsProgressSummary goalsProgress;
    private WorkloadSummaryDto workloadSummary;
    private FinanceSnapshotDto financeSnapshot;
    private HomeRecommendationDto proactiveRecommendation;
    private long unreadNotificationCount;
}
