package com.livo.api.modules.finance.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.finance.dto.BudgetResponse;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.UpdateBudgetRequest;
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
@RequestMapping("/api/v1/finance/budgets")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Monthly budgets and category allocations")
public class BudgetController {

    private final FinanceService financeService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse response = financeService.createBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Budget created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month
    ) {
        List<BudgetResponse> response = financeService.getBudgets(userId, month);
        return ResponseEntity.ok(ApiResponse.success(response, "Budgets retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        BudgetResponse response = financeService.getBudgetById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        BudgetResponse response = financeService.updateBudget(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        financeService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }
}
