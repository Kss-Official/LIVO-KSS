package com.livo.api.modules.learning.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.learning.dto.LearningStatsResponse;
import com.livo.api.modules.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/stats")
@RequiredArgsConstructor
public class LearningStatsController {

    private final LearningService learningService;

    @GetMapping
    public ResponseEntity<ApiResponse<LearningStatsResponse>> getLearningStats(
            @CurrentUser UUID userId
    ) {
        LearningStatsResponse response = learningService.getLearningStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning statistics retrieved successfully"));
    }
}
