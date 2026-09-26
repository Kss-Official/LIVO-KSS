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
public class HabitCorrelationResponse {
    private String headlineInsight;
    private int evaluatedDays;
    @Builder.Default
    private List<HabitCorrelationCardDto> correlations = new ArrayList<>();
}
