package com.livo.api.modules.finance.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget entity defining monthly spending thresholds per expense category.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "budgets")
public class BudgetEntity extends BaseSyncEntity {

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "monthly_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Builder.Default
    @Column(name = "alert_threshold_percent", nullable = false)
    private short alertThresholdPercent = 80;

    @Column(name = "month_start", nullable = false)
    private LocalDate monthStart;
}
