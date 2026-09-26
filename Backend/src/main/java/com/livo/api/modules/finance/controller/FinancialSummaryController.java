package com.livo.api.modules.finance.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.finance.dto.FinancialSummaryResponse;
import com.livo.api.modules.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/summary")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Financial summaries, spending analytics, and category rollups")
public class FinancialSummaryController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        FinancialSummaryResponse response = financeService.getFinancialSummary(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Financial summary retrieved successfully"));
    }
}
