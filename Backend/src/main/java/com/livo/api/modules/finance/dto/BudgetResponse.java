package com.livo.api.modules.finance.dto;

import com.livo.api.modules.finance.entity.BudgetEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private UUID id;
    private UUID userId;
    private String category;
    private BigDecimal monthlyLimit;
    private String currency;
    private short alertThresholdPercent;
    private LocalDate monthStart;

    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageUsed;
    private boolean isOverBudget;
    private boolean isAlertTriggered;

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static BudgetResponse fromEntity(BudgetEntity entity, BigDecimal spentAmount) {
        if (entity == null) {
            return null;
        }
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        BigDecimal limit = entity.getMonthlyLimit() != null ? entity.getMonthlyLimit() : BigDecimal.ZERO;
        BigDecimal remaining = limit.subtract(spent);

        double percentUsed = 0.0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            percentUsed = spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        boolean overBudget = spent.compareTo(limit) > 0;
        boolean alertTriggered = percentUsed >= entity.getAlertThresholdPercent();

        return BudgetResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .category(entity.getCategory())
                .monthlyLimit(limit)
                .currency(entity.getCurrency())
                .alertThresholdPercent(entity.getAlertThresholdPercent())
                .monthStart(entity.getMonthStart())
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageUsed(percentUsed)
                .isOverBudget(overBudget)
                .isAlertTriggered(alertTriggered)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
