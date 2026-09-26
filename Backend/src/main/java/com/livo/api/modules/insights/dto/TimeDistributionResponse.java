package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeDistributionResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalTrackedMinutes;
    private BigDecimal totalTrackedHours;
    @Builder.Default
    private List<CategoryTimeDistributionDto> categories = new ArrayList<>();
}
