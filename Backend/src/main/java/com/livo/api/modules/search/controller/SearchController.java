package com.livo.api.modules.search.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.search.dto.SearchDomain;
import com.livo.api.modules.search.dto.SearchResponse;
import com.livo.api.modules.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "High-speed cross-module search API across tasks, goals, events, habits, learning, expenses, and trips")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Execute Global Search", description = "Performs unified cross-module search with relevance ranking, snippet extraction, and domain filtering")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "ALL") SearchDomain domain,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        SearchResponse response = searchService.search(currentUser.getId(), q, domain, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
