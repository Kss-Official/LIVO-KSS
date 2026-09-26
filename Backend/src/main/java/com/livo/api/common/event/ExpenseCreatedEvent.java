package com.livo.api.common.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event published when an expense transaction is recorded.
 */
public record ExpenseCreatedEvent(
        UUID transactionId,
        UUID userId,
        String category,
        BigDecimal amount,
        LocalDate transactionDate
) {
}
