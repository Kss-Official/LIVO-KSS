package com.livo.api.common.listener;

import com.livo.api.common.event.ExpenseCreatedEvent;
import com.livo.api.modules.finance.entity.BudgetEntity;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.BudgetRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceEventsListener {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Async("boundedTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        log.info("Handling ExpenseCreatedEvent for transaction {} user {} category {}", event.transactionId(), event.userId(), event.category());

        LocalDate date = event.transactionDate() != null ? event.transactionDate() : LocalDate.now();
        LocalDate monthStart = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        Instant monthStartInstant = monthStart.atStartOfDay(ZoneId.of("UTC")).toInstant();

        String category = event.category() != null ? event.category().toUpperCase() : "";
        Optional<BudgetEntity> budgetOpt = budgetRepository.findByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(
                event.userId(), category, monthStart
        );
        if (budgetOpt.isEmpty() && event.category() != null) {
            budgetOpt = budgetRepository.findByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(
                    event.userId(), event.category(), monthStart
            );
        }

        if (budgetOpt.isPresent()) {
            BudgetEntity budget = budgetOpt.get();
            BigDecimal limit = budget.getMonthlyLimit();

            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                List<TransactionEntity> monthTransactions = transactionRepository.findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(
                        event.userId(), monthStart, monthEnd
                ).stream()
                        .filter(t -> t.getCategory() != null && t.getCategory().equalsIgnoreCase(event.category()))
                        .toList();

                BigDecimal totalSpent = monthTransactions.stream()
                        .filter(t -> t.getType() == TransactionType.EXPENSE)
                        .map(TransactionEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                double ratio = totalSpent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue();
                short thresholdPercent = budget.getAlertThresholdPercent() > 0 ? budget.getAlertThresholdPercent() : (short) 80;
                double thresholdRatio = thresholdPercent / 100.0;

                if (ratio >= 1.0) {
                    boolean alreadyExceeded = notificationRepository.existsBudgetAlertSince(
                            event.userId(), "Budget Exceeded!", event.category(), monthStartInstant
                    );
                    if (!alreadyExceeded) {
                        notificationService.sendSystemNotification(
                                event.userId(),
                                NotificationType.BUDGET,
                                "Budget Exceeded!",
                                String.format("You have exceeded your %s budget! Spent: %s / Limit: %s", event.category(), totalSpent, limit),
                                "TRANSACTION",
                                event.transactionId()
                        );
                    }
                } else if (ratio >= thresholdRatio) {
                    boolean alreadyAlerted = notificationRepository.existsBudgetAlertSince(
                            event.userId(), "Budget Alert", event.category(), monthStartInstant
                    );
                    boolean alreadyExceeded = notificationRepository.existsBudgetAlertSince(
                            event.userId(), "Budget Exceeded!", event.category(), monthStartInstant
                    );
                    if (!alreadyAlerted && !alreadyExceeded) {
                        notificationService.sendSystemNotification(
                                event.userId(),
                                NotificationType.BUDGET,
                                String.format("Budget Alert (%d%%)", thresholdPercent),
                                String.format("You have used %.0f%% of your %s budget! Spent: %s / Limit: %s", ratio * 100, event.category(), totalSpent, limit),
                                "TRANSACTION",
                                event.transactionId()
                        );
                    }
                }
            }
        }
    }
}
