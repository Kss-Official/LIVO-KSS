package com.livo.api.modules.learning.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.learning.dto.CreateLearningResourceRequest;
import com.livo.api.modules.learning.dto.LearningResourceResponse;
import com.livo.api.modules.learning.dto.UpdateLearningResourceRequest;
import com.livo.api.modules.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/items/{itemId}/resources")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningService learningService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningResourceResponse>> addResource(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody CreateLearningResourceRequest request
    ) {
        LearningResourceResponse response = learningService.addResource(userId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Learning resource added successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningResourceResponse>>> getResources(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId
    ) {
        List<LearningResourceResponse> response = learningService.getResources(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning resources retrieved successfully"));
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<ApiResponse<LearningResourceResponse>> updateResource(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId,
            @PathVariable UUID resourceId,
            @Valid @RequestBody UpdateLearningResourceRequest request
    ) {
        LearningResourceResponse response = learningService.updateResource(userId, itemId, resourceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning resource updated successfully"));
    }

    @PatchMapping("/{resourceId}/toggle")
    public ResponseEntity<ApiResponse<LearningResourceResponse>> toggleResource(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId,
            @PathVariable UUID resourceId
    ) {
        LearningResourceResponse response = learningService.toggleResourceCompletion(userId, itemId, resourceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning resource completion toggled successfully"));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId,
            @PathVariable UUID resourceId
    ) {
        learningService.deleteResource(userId, itemId, resourceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Learning resource deleted successfully"));
    }
}
