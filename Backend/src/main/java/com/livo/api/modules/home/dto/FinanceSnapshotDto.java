package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSnapshotDto {
    private String currentMonth; // "YYYY-MM"
    private BigDecimal monthTotalExpense;
    private BigDecimal monthTotalIncome;
    private BigDecimal netSavings;
    private BigDecimal monthlyBudgetLimit;
    private double budgetUsedPercentage;
    private boolean isNearBudgetAlert;
}
