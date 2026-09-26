package com.livo.api.modules.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTimeDistributionDto {
    private String category; // "WORK", "PERSONAL", "LEARNING", "HEALTH", "TRAVEL", "OTHERS"
    private long totalMinutes;
    private BigDecimal totalHours;
    private double percentage;
    private String colorHex;
}
