package com.livo.api.modules.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBudgetRequest {

    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than zero")
    private BigDecimal monthlyLimit;

    @Min(value = 1, message = "Alert threshold percent must be between 1 and 100")
    @Max(value = 100, message = "Alert threshold percent must be between 1 and 100")
    private Short alertThresholdPercent;
}
