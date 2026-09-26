package com.livo.api.modules.finance.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.finance.dto.BudgetResponse;
import com.livo.api.modules.finance.dto.CategorySpendingResponse;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.dto.FinancialSummaryResponse;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.dto.UpdateBudgetRequest;
import com.livo.api.modules.finance.dto.UpdateTransactionRequest;
import com.livo.api.modules.finance.entity.BudgetEntity;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.BudgetRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.trip.repository.TripRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;
    private final TripRepository tripRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request) {
        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }

        if (request.getTripId() != null) {
            tripRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getTripId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + request.getTripId()));
        }

        LocalDate transactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : LocalDate.now();

        String currency = request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR";

        TransactionEntity transaction = TransactionEntity.builder()
                .goalId(request.getGoalId())
                .tripId(request.getTripId())
                .type(request.getType() != null ? request.getType() : TransactionType.EXPENSE)
                .amount(request.getAmount())
                .currency(currency)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory().toUpperCase())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI)
                .transactionDate(transactionDate)
                .transactionTime(request.getTransactionTime())
                .build();
        transaction.setUserId(userId);
        transaction.setVersion(1L);

        TransactionEntity saved = transactionRepository.save(transaction);
        log.info("Created transaction {} ({}) amount {} for user {}", saved.getId(), saved.getTitle(), saved.getAmount(), userId);

        if (saved.getType() == TransactionType.EXPENSE) {
            eventPublisher.publishEvent(new com.livo.api.common.event.ExpenseCreatedEvent(
                    saved.getId(), userId, saved.getCategory(), saved.getAmount(), saved.getTransactionDate()
            ));
        }

        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID userId, UUID transactionId) {
        TransactionEntity transaction = findTransactionOrThrow(userId, transactionId);
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            String category,
            UUID goalId,
            UUID tripId
    ) {
        if (startDate == null && endDate == null && type == null && (category == null || category.isBlank()) && goalId == null && tripId == null) {
            return transactionRepository.findAllByUserIdAndDeletedAtIsNullOrderByTransactionDateDesc(userId)
                    .stream()
                    .map(TransactionResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        Specification<TransactionEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("transactionDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (goalId != null) {
                predicates.add(cb.equal(root.get("goalId"), goalId));
            }

            if (tripId != null) {
                predicates.add(cb.equal(root.get("tripId"), tripId));
            }

            query.orderBy(cb.desc(root.get("transactionDate")), cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        TransactionEntity transaction = findTransactionOrThrow(userId, transactionId);

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
            transaction.setGoalId(request.getGoalId());
        }
        if (request.getTripId() != null) {
            tripRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getTripId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + request.getTripId()));
            transaction.setTripId(request.getTripId());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            transaction.setCurrency(request.getCurrency().toUpperCase());
        }
        if (request.getTitle() != null) {
            transaction.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            transaction.setCategory(request.getCategory().toUpperCase());
        }
        if (request.getPaymentMethod() != null) {
            transaction.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getTransactionTime() != null) {
            transaction.setTransactionTime(request.getTransactionTime());
        }

        TransactionEntity updated = transactionRepository.save(transaction);
        log.info("Updated transaction {} for user {}", transactionId, userId);
        return TransactionResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID userId, UUID transactionId) {
        TransactionEntity transaction = findTransactionOrThrow(userId, transactionId);
        transaction.setDeletedAt(Instant.now());
        transactionRepository.save(transaction);
        log.info("Soft-deleted transaction {} for user {}", transactionId, userId);
    }

    @Override
    @Transactional
    public BudgetResponse createBudget(UUID userId, CreateBudgetRequest request) {
        LocalDate monthStart = request.getMonthStart() != null
                ? request.getMonthStart().withDayOfMonth(1)
                : LocalDate.now().withDayOfMonth(1);

        String category = request.getCategory().toUpperCase();

        if (budgetRepository.existsByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(userId, category, monthStart)) {
            throw new BadRequestException("Budget already exists for category '" + category + "' in month " + monthStart);
        }

        String currency = request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR";

        BudgetEntity budget = BudgetEntity.builder()
                .category(category)
                .monthlyLimit(request.getMonthlyLimit())
                .currency(currency)
                .alertThresholdPercent(request.getAlertThresholdPercent())
                .monthStart(monthStart)
                .build();
        budget.setUserId(userId);
        budget.setVersion(1L);

        BudgetEntity saved = budgetRepository.save(budget);
        BigDecimal spent = calculateCategorySpentInMonth(userId, category, monthStart);
        log.info("Created budget {} for category {} in month {} by user {}", saved.getId(), category, monthStart, userId);
        return BudgetResponse.fromEntity(saved, spent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(UUID userId, LocalDate month) {
        LocalDate monthStart = month != null ? month.withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);
        List<BudgetEntity> budgets = budgetRepository.findAllByUserIdAndMonthStartAndDeletedAtIsNull(userId, monthStart);

        return budgets.stream()
                .map(b -> {
                    BigDecimal spent = calculateCategorySpentInMonth(userId, b.getCategory(), b.getMonthStart());
                    return BudgetResponse.fromEntity(b, spent);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID userId, UUID budgetId) {
        BudgetEntity budget = findBudgetOrThrow(userId, budgetId);
        BigDecimal spent = calculateCategorySpentInMonth(userId, budget.getCategory(), budget.getMonthStart());
        return BudgetResponse.fromEntity(budget, spent);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(UUID userId, UUID budgetId, UpdateBudgetRequest request) {
        BudgetEntity budget = findBudgetOrThrow(userId, budgetId);

        if (request.getMonthlyLimit() != null) {
            budget.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getAlertThresholdPercent() != null) {
            budget.setAlertThresholdPercent(request.getAlertThresholdPercent());
        }

        BudgetEntity updated = budgetRepository.save(budget);
        BigDecimal spent = calculateCategorySpentInMonth(userId, updated.getCategory(), updated.getMonthStart());
        log.info("Updated budget {} for user {}", budgetId, userId);
        return BudgetResponse.fromEntity(updated, spent);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID userId, UUID budgetId) {
        BudgetEntity budget = findBudgetOrThrow(userId, budgetId);
        budget.setDeletedAt(Instant.now());
        budgetRepository.save(budget);
        log.info("Soft-deleted budget {} for user {}", budgetId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<TransactionEntity> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(userId, start, end);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (TransactionEntity t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Category breakdown for expenses
        final BigDecimal finalTotalExpense = totalExpense;
        Map<String, List<TransactionEntity>> expenseByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(TransactionEntity::getCategory));

        List<CategorySpendingResponse> categoryBreakdown = new ArrayList<>();
        for (Map.Entry<String, List<TransactionEntity>> entry : expenseByCategory.entrySet()) {
            String cat = entry.getKey();
            List<TransactionEntity> catTxns = entry.getValue();
            BigDecimal catTotal = catTxns.stream()
                    .map(TransactionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = 0.0;
            if (finalTotalExpense.compareTo(BigDecimal.ZERO) > 0) {
                percentage = catTotal.divide(finalTotalExpense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            categoryBreakdown.add(CategorySpendingResponse.builder()
                    .category(cat)
                    .totalAmount(catTotal)
                    .transactionCount(catTxns.size())
                    .percentageOfExpense(percentage)
                    .build());
        }

        // Sort categories by totalAmount desc
        categoryBreakdown.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        // Active budgets for the month of start date
        LocalDate budgetMonth = start.withDayOfMonth(1);
        List<BudgetEntity> budgetEntities = budgetRepository.findAllByUserIdAndMonthStartAndDeletedAtIsNull(userId, budgetMonth);
        List<BudgetResponse> budgetResponses = budgetEntities.stream()
                .map(b -> {
                    BigDecimal spent = calculateCategorySpentInMonth(userId, b.getCategory(), b.getMonthStart());
                    return BudgetResponse.fromEntity(b, spent);
                })
                .collect(Collectors.toList());

        return FinancialSummaryResponse.builder()
                .startDate(start)
                .endDate(end)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .totalTransactions(transactions.size())
                .categoryBreakdown(categoryBreakdown)
                .budgets(budgetResponses)
                .build();
    }

    private TransactionEntity findTransactionOrThrow(UUID userId, UUID transactionId) {
        return transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    private BudgetEntity findBudgetOrThrow(UUID userId, UUID budgetId) {
        return budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + budgetId));
    }

    private BigDecimal calculateCategorySpentInMonth(UUID userId, String category, LocalDate monthStart) {
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<TransactionEntity> transactions = transactionRepository
                .findAllByUserIdAndCategoryAndTransactionDateBetweenAndDeletedAtIsNull(userId, category.toUpperCase(), monthStart, monthEnd);

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
