package com.livo.api.modules.finance.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.dto.UpdateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/transactions")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Income, expenses, and transaction records")
public class TransactionController {

    private final FinanceService financeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = financeService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) UUID tripId
    ) {
        List<TransactionResponse> response = financeService.getTransactions(userId, startDate, endDate, type, category, goalId, tripId);
        return ResponseEntity.ok(ApiResponse.success(response, "Transactions retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        TransactionResponse response = financeService.getTransactionById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        TransactionResponse response = financeService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        financeService.deleteTransaction(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }
}
