package com.livo.api.modules.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpendingResponse {

    private String category;
    private BigDecimal totalAmount;
    private long transactionCount;
    private double percentageOfExpense;
}
