package com.livo.api.modules.finance;

import com.livo.api.modules.finance.entity.BudgetEntity;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.BudgetRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FinanceDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part7_fb_" + UUID.randomUUID())
                .email("test.part7." + UUID.randomUUID() + "@example.com")
                .fullName("Part 7 Finance User")
                .timezone("Asia/Kolkata")
                .language("en")
                .currency("INR")
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.findById(testUser.getId()).ifPresent(u -> {
                transactionRepository.deleteAll(transactionRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                budgetRepository.deleteAll(budgetRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: TransactionEntity persistence, type filtering, and date range query")
    void testTransactionPersistence() {
        LocalDate today = LocalDate.now();
        TransactionEntity expense = TransactionEntity.builder()
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1450.50"))
                .currency("INR")
                .title("Grocery shopping at Whole Foods")
                .description("Weekly veggies and pantry staples")
                .category("Food & Dining")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(today)
                .transactionTime(LocalTime.of(14, 30))
                .build();
        expense.setUserId(testUser.getId());
        expense = transactionRepository.saveAndFlush(expense);

        Optional<TransactionEntity> found = transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(expense.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Grocery shopping at Whole Foods");
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("1450.50"));
        assertThat(found.get().getCurrency()).isEqualTo("INR");
        assertThat(found.get().getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(found.get().getType()).isEqualTo(TransactionType.EXPENSE);

        // Filter by type
        List<TransactionEntity> expenses = transactionRepository.findAllByUserIdAndTypeAndDeletedAtIsNull(testUser.getId(), TransactionType.EXPENSE);
        assertThat(expenses).hasSize(1);

        // Filter by category
        List<TransactionEntity> foodExpenses = transactionRepository.findAllByUserIdAndCategoryAndDeletedAtIsNull(testUser.getId(), "Food & Dining");
        assertThat(foodExpenses).hasSize(1);

        // Date range query
        List<TransactionEntity> dateRange = transactionRepository.findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(
                testUser.getId(), today.minusDays(1), today.plusDays(1)
        );
        assertThat(dateRange).hasSize(1);

        // Soft delete
        expense.markDeleted();
        transactionRepository.saveAndFlush(expense);
        assertThat(transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(expense.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: BudgetEntity persistence, month-start boundary, and unique check")
    void testBudgetPersistence() {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        BudgetEntity budget = BudgetEntity.builder()
                .category("Food & Dining")
                .monthlyLimit(new BigDecimal("15000.00"))
                .currency("INR")
                .alertThresholdPercent((short) 85)
                .monthStart(firstOfMonth)
                .build();
        budget.setUserId(testUser.getId());
        budget = budgetRepository.saveAndFlush(budget);

        Optional<BudgetEntity> found = budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(budget.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isEqualTo("Food & Dining");
        assertThat(found.get().getMonthlyLimit()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(found.get().getAlertThresholdPercent()).isEqualTo((short) 85);
        assertThat(found.get().getMonthStart()).isEqualTo(firstOfMonth);

        // Existence check
        assertThat(budgetRepository.existsByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(testUser.getId(), "Food & Dining", firstOfMonth)).isTrue();
        assertThat(budgetRepository.existsByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(testUser.getId(), "Utilities", firstOfMonth)).isFalse();

        // Query by monthStart
        List<BudgetEntity> monthlyBudgets = budgetRepository.findAllByUserIdAndMonthStartAndDeletedAtIsNull(testUser.getId(), firstOfMonth);
        assertThat(monthlyBudgets).hasSize(1);

        // Soft delete
        budget.markDeleted();
        budgetRepository.saveAndFlush(budget);
        assertThat(budgetRepository.findByIdAndUserIdAndDeletedAtIsNull(budget.getId(), testUser.getId())).isEmpty();
    }
}
