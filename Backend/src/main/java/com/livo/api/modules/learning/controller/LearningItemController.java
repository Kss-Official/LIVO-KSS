package com.livo.api.modules.learning.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.LearningItemResponse;
import com.livo.api.modules.learning.dto.UpdateLearningItemRequest;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/learning/items", "/api/v1/learning"})
@RequiredArgsConstructor
@Tag(name = "Learning", description = "Courses, books, skills, and study session logs")
public class LearningItemController {

    private final LearningService learningService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningItemResponse>> createLearningItem(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateLearningItemRequest request
    ) {
        LearningItemResponse response = learningService.createLearningItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Learning item created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningItemResponse>>> getLearningItems(
            @CurrentUser UUID userId,
            @RequestParam(required = false) LearningStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID goalId
    ) {
        List<LearningItemResponse> response = learningService.getLearningItems(userId, status, category, goalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning items retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningItemResponse>> getLearningItemById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        LearningItemResponse response = learningService.getLearningItemById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning item retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningItemResponse>> updateLearningItem(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLearningItemRequest request
    ) {
        LearningItemResponse response = learningService.updateLearningItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Learning item updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLearningItem(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        learningService.deleteLearningItem(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Learning item deleted successfully"));
    }
}
