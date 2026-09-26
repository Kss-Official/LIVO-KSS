package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLifeScoreDto {
    private int overallScore; // 0 - 100
    private double taskCompletionRate; // percentage 0 - 100
    private double habitCheckinRate; // percentage 0 - 100
    private double goalPaceRate; // percentage 0 - 100
    private String ratingLabel; // "Exceptional", "On Track", "Building Momentum", "Needs Attention"
}
