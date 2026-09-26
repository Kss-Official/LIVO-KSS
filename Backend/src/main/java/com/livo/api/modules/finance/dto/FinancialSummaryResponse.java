package com.livo.api.modules.finance.dto;

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
public class FinancialSummaryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private double savingsRate;
    private long totalTransactions;

    @Builder.Default
    private List<CategorySpendingResponse> categoryBreakdown = new ArrayList<>();

    @Builder.Default
    private List<BudgetResponse> budgets = new ArrayList<>();
}
