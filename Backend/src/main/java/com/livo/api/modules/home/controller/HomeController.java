package com.livo.api.modules.home.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.home.dto.DailyQuoteDto;
import com.livo.api.modules.home.dto.HomeDashboardResponse;
import com.livo.api.modules.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Unified high-performance home dashboard and daily life overview endpoints")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "Get Aggregated Home Dashboard", description = "Returns greeting, quote, priority carousel, schedule, habits, goals, workload, and finance in a single fast call")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>> getHomeDashboard(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HomeDashboardResponse response = homeService.getHomeDashboard(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily-overview")
    @Operation(summary = "Get Daily Overview Feed", description = "Convenience alias for the aggregated daily life overview")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>> getDailyOverview(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HomeDashboardResponse response = homeService.getHomeDashboard(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Home Dashboard Feed (Alias)", description = "Convenience alias for /api/v1/home")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>> getHomeDashboardAlias(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HomeDashboardResponse response = homeService.getHomeDashboard(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/quote")
    @Operation(summary = "Get Quote of the Day", description = "Returns today's curated inspirational quote")
    public ResponseEntity<ApiResponse<DailyQuoteDto>> getDailyQuote(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyQuoteDto quote = homeService.getDailyQuote(date);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
}
