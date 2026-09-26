package com.livo.api.modules.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.dto.UpdateBudgetRequest;
import com.livo.api.modules.finance.dto.UpdateTransactionRequest;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.BudgetRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.trip.entity.TripEntity;
import com.livo.api.modules.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TripRepository tripRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "finance_user_" + UUID.randomUUID();
        String email = "finance_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Finance Manager")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should perform CRUD on transactions and query with filters")
    void testCreateAndQueryTransactions() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Create Income Transaction
        CreateTransactionRequest income = CreateTransactionRequest.builder()
                .title("Tech Consulting Fee")
                .type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(80000.00))
                .currency("INR")
                .category("INCOME")
                .paymentMethod(PaymentMethod.NET_BANKING)
                .transactionDate(today)
                .transactionTime(LocalTime.of(10, 0))
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(income)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Tech Consulting Fee"))
                .andExpect(jsonPath("$.data.type").value("INCOME"))
                .andExpect(jsonPath("$.data.amount").value(80000.00));

        // 2. Create Expense Transaction
        CreateTransactionRequest expense = CreateTransactionRequest.builder()
                .title("Organic Groceries")
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(2500.00))
                .currency("INR")
                .category("GROCERIES")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(today)
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("GROCERIES"));

        // 3. Query all transactions
        mockMvc.perform(get("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 4. Query with filter: type=EXPENSE
        mockMvc.perform(get("/api/v1/finance/transactions")
                        .param("type", "EXPENSE")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Organic Groceries"));
    }

    @Test
    @DisplayName("Should manage monthly budgets, compute spent amounts, and trigger alert thresholds")
    void testBudgetLifecycleAndThresholdAlerts() throws Exception {
        LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);

        // 1. Create Budget for DINING (Limit: 5000, Threshold: 70%)
        CreateBudgetRequest budgetReq = CreateBudgetRequest.builder()
                .category("DINING")
                .monthlyLimit(BigDecimal.valueOf(5000.00))
                .currency("INR")
                .alertThresholdPercent((short) 70)
                .monthStart(currentMonthStart)
                .build();

        MvcResult budgetResult = mockMvc.perform(post("/api/v1/finance/budgets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID budgetId = UUID.fromString(objectMapper.readTree(budgetResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Initial budget checks (0 spent)
        mockMvc.perform(get("/api/v1/finance/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spentAmount").value(0.0))
                .andExpect(jsonPath("$.data.remainingAmount").value(5000.00))
                .andExpect(jsonPath("$.data.percentageUsed").value(0.0))
                .andExpect(jsonPath("$.data.overBudget").value(false))
                .andExpect(jsonPath("$.data.alertTriggered").value(false));

        // 2. Add expense of 4000.00 (80% spent -> triggers alert at >= 70%)
        CreateTransactionRequest expense1 = CreateTransactionRequest.builder()
                .title("Fine Dining Restaurant")
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(4000.00))
                .category("DINING")
                .transactionDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense1)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/finance/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spentAmount").value(4000.00))
                .andExpect(jsonPath("$.data.remainingAmount").value(1000.00))
                .andExpect(jsonPath("$.data.percentageUsed").value(80.00))
                .andExpect(jsonPath("$.data.overBudget").value(false))
                .andExpect(jsonPath("$.data.alertTriggered").value(true));

        // 3. Add another expense of 1500.00 (Total: 5500.00 -> 110% spent, overBudget = true)
        CreateTransactionRequest expense2 = CreateTransactionRequest.builder()
                .title("Coffee & Snacks")
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(1500.00))
                .category("DINING")
                .transactionDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/finance/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spentAmount").value(5500.00))
                .andExpect(jsonPath("$.data.remainingAmount").value(-500.00))
                .andExpect(jsonPath("$.data.percentageUsed").value(110.00))
                .andExpect(jsonPath("$.data.overBudget").value(true))
                .andExpect(jsonPath("$.data.alertTriggered").value(true));

        // 4. Update Budget Limit to 8000.00 (5500 / 8000 = 68.75% -> no longer over budget or alert)
        UpdateBudgetRequest updateReq = UpdateBudgetRequest.builder()
                .monthlyLimit(BigDecimal.valueOf(8000.00))
                .alertThresholdPercent((short) 75)
                .build();

        mockMvc.perform(put("/api/v1/finance/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.monthlyLimit").value(8000.00))
                .andExpect(jsonPath("$.data.percentageUsed").value(68.75))
                .andExpect(jsonPath("$.data.overBudget").value(false))
                .andExpect(jsonPath("$.data.alertTriggered").value(false));
    }

    @Test
    @DisplayName("Should aggregate financial summary with income, expense, net savings, and category breakdown")
    void testFinancialSummaryAggregation() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Income: 100,000
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .title("Salary")
                                .type(TransactionType.INCOME)
                                .amount(BigDecimal.valueOf(100000.00))
                                .category("SALARY")
                                .transactionDate(today)
                                .build())))
                .andExpect(status().isCreated());

        // 2. Expense 1 (RENT): 30,000
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .title("Apartment Rent")
                                .type(TransactionType.EXPENSE)
                                .amount(BigDecimal.valueOf(30000.00))
                                .category("HOUSING")
                                .transactionDate(today)
                                .build())))
                .andExpect(status().isCreated());

        // 3. Expense 2 (UTILITIES): 10,000
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .title("Electricity & Internet")
                                .type(TransactionType.EXPENSE)
                                .amount(BigDecimal.valueOf(10000.00))
                                .category("UTILITIES")
                                .transactionDate(today)
                                .build())))
                .andExpect(status().isCreated());

        // 4. Query Financial Summary
        mockMvc.perform(get("/api/v1/finance/summary")
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(100000.00))
                .andExpect(jsonPath("$.data.totalExpense").value(40000.00))
                .andExpect(jsonPath("$.data.netSavings").value(60000.00))
                .andExpect(jsonPath("$.data.savingsRate").value(60.00))
                .andExpect(jsonPath("$.data.totalTransactions").value(3))
                .andExpect(jsonPath("$.data.categoryBreakdown.length()").value(2))
                .andExpect(jsonPath("$.data.categoryBreakdown[0].category").value("HOUSING"))
                .andExpect(jsonPath("$.data.categoryBreakdown[0].percentageOfExpense").value(75.00)) // 30,000 / 40,000 = 75%
                .andExpect(jsonPath("$.data.categoryBreakdown[1].category").value("UTILITIES"))
                .andExpect(jsonPath("$.data.categoryBreakdown[1].percentageOfExpense").value(25.00)); // 10,000 / 40,000 = 25%
    }

    @Test
    @DisplayName("Should update and soft-delete transactions and budgets")
    void testUpdateAndSoftDelete() throws Exception {
        CreateTransactionRequest txnReq = CreateTransactionRequest.builder()
                .title("Books")
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(500.00))
                .category("EDUCATION")
                .transactionDate(LocalDate.now())
                .build();

        MvcResult txnResult = mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txnReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID txnId = UUID.fromString(objectMapper.readTree(txnResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Update
        mockMvc.perform(put("/api/v1/finance/transactions/" + txnId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateTransactionRequest.builder()
                                .title("Technical Books")
                                .amount(BigDecimal.valueOf(750.00))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Technical Books"))
                .andExpect(jsonPath("$.data.amount").value(750.00));

        // Delete
        mockMvc.perform(delete("/api/v1/finance/transactions/" + txnId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Fetch after delete -> 404
        mockMvc.perform(get("/api/v1/finance/transactions/" + txnId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should validate financial constraints and prevent cross-user access")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: Zero or negative amount
        CreateTransactionRequest invalidAmount = CreateTransactionRequest.builder()
                .title("Zero Amount")
                .amount(BigDecimal.ZERO)
                .category("GENERAL")
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAmount)))
                .andExpect(status().isBadRequest());

        // 2. Validation: Duplicate budget for same category and month
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        CreateBudgetRequest budget1 = CreateBudgetRequest.builder()
                .category("SHOPPING")
                .monthlyLimit(BigDecimal.valueOf(2000.00))
                .monthStart(monthStart)
                .build();

        mockMvc.perform(post("/api/v1/finance/budgets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget1)))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/v1/finance/budgets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget1)))
                .andExpect(status().isBadRequest());

        // 3. User isolation
        TransactionEntity user1Txn = TransactionEntity.builder()
                .title("User 1 Secret Transaction")
                .amount(BigDecimal.valueOf(1000.00))
                .category("CONFIDENTIAL")
                .transactionDate(LocalDate.now())
                .currency("INR")
                .build();
        user1Txn.setUserId(userId);
        user1Txn.setVersion(1L);
        user1Txn = transactionRepository.save(user1Txn);

        String otherUid = "other_finance_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_fin_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Finance User")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/finance/transactions/" + user1Txn.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject transaction creation with non-existent tripId (404)")
    void testCreateTransactionWithNonExistentTripFailsNotFound() throws Exception {
        UUID nonExistentTripId = UUID.randomUUID();
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .title("Trip Flight")
                .amount(BigDecimal.valueOf(5000.00))
                .category("TRAVEL")
                .tripId(nonExistentTripId)
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));
    }

    @Test
    @DisplayName("Should reject transaction creation with soft-deleted tripId (404)")
    void testCreateTransactionWithSoftDeletedTripFailsNotFound() throws Exception {
        TripEntity trip = TripEntity.builder()
                .title("Paris Vacation")
                .destination("Paris")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .build();
        trip.setUserId(userId);
        trip.setVersion(1L);
        trip = tripRepository.saveAndFlush(trip);
        trip.markDeleted();
        trip = tripRepository.saveAndFlush(trip);

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .title("Deleted Trip Hotel")
                .amount(BigDecimal.valueOf(8000.00))
                .category("TRAVEL")
                .tripId(trip.getId())
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));
    }

    @Test
    @DisplayName("Should allow transaction creation with valid active tripId (201)")
    void testCreateTransactionWithValidTripSuccess() throws Exception {
        TripEntity trip = TripEntity.builder()
                .title("Tokyo Conference")
                .destination("Tokyo")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(12))
                .build();
        trip.setUserId(userId);
        trip.setVersion(1L);
        trip = tripRepository.saveAndFlush(trip);

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .title("Conference Pass")
                .amount(BigDecimal.valueOf(15000.00))
                .category("TRAVEL")
                .tripId(trip.getId())
                .build();

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(trip.getId().toString()));
    }

    @Test
    @DisplayName("Should reject transaction update with non-existent or soft-deleted tripId (404)")
    void testUpdateTransactionWithInvalidTripFailsNotFound() throws Exception {
        // Create valid transaction
        TransactionEntity txn = TransactionEntity.builder()
                .title("General Expense")
                .amount(BigDecimal.valueOf(500.00))
                .category("FOOD")
                .transactionDate(LocalDate.now())
                .currency("INR")
                .build();
        txn.setUserId(userId);
        txn.setVersion(1L);
        txn = transactionRepository.saveAndFlush(txn);

        // Update with non-existent trip
        mockMvc.perform(put("/api/v1/finance/transactions/" + txn.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateTransactionRequest.builder()
                                .tripId(UUID.randomUUID())
                                .build())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));

        // Create soft-deleted trip
        TripEntity trip = TripEntity.builder()
                .title("Old Trip")
                .destination("Rome")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();
        trip.setUserId(userId);
        trip.setVersion(1L);
        trip = tripRepository.saveAndFlush(trip);
        trip.markDeleted();
        trip = tripRepository.saveAndFlush(trip);

        // Update with soft-deleted trip
        mockMvc.perform(put("/api/v1/finance/transactions/" + txn.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateTransactionRequest.builder()
                                .tripId(trip.getId())
                                .build())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));
    }
}
