package com.livo.api.modules.finance.service;

import com.livo.api.modules.finance.dto.BudgetResponse;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.dto.FinancialSummaryResponse;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.dto.UpdateBudgetRequest;
import com.livo.api.modules.finance.dto.UpdateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinanceService {

    TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request);

    TransactionResponse getTransactionById(UUID userId, UUID transactionId);

    List<TransactionResponse> getTransactions(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            String category,
            UUID goalId,
            UUID tripId
    );

    TransactionResponse updateTransaction(UUID userId, UUID transactionId, UpdateTransactionRequest request);

    void deleteTransaction(UUID userId, UUID transactionId);

    BudgetResponse createBudget(UUID userId, CreateBudgetRequest request);

    List<BudgetResponse> getBudgets(UUID userId, LocalDate month);

    BudgetResponse getBudgetById(UUID userId, UUID budgetId);

    BudgetResponse updateBudget(UUID userId, UUID budgetId, UpdateBudgetRequest request);

    void deleteBudget(UUID userId, UUID budgetId);

    FinancialSummaryResponse getFinancialSummary(UUID userId, LocalDate startDate, LocalDate endDate);
}
