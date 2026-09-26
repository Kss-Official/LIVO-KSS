package com.livo.api.modules.learning.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.learning.dto.LearningSessionResponse;
import com.livo.api.modules.learning.dto.LogLearningSessionRequest;
import com.livo.api.modules.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/sessions")
@RequiredArgsConstructor
public class LearningSessionController {

    private final LearningService learningService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningSessionResponse>> logSession(
            @CurrentUser UUID userId,
            @Valid @RequestBody LogLearningSessionRequest request
    ) {
        LearningSessionResponse response = learningService.logSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Learning session logged successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningSessionResponse>>> getSessions(
            @CurrentUser UUID userId,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<LearningSessionResponse> response = learningService.getSessions(userId, itemId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning sessions retrieved successfully"));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @CurrentUser UUID userId,
            @PathVariable UUID sessionId
    ) {
        learningService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Learning session deleted successfully"));
    }
}
