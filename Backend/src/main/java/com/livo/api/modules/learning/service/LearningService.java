package com.livo.api.modules.learning.service;

import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.CreateLearningResourceRequest;
import com.livo.api.modules.learning.dto.LearningItemResponse;
import com.livo.api.modules.learning.dto.LearningResourceResponse;
import com.livo.api.modules.learning.dto.LearningSessionResponse;
import com.livo.api.modules.learning.dto.LearningStatsResponse;
import com.livo.api.modules.learning.dto.LogLearningSessionRequest;
import com.livo.api.modules.learning.dto.UpdateLearningItemRequest;
import com.livo.api.modules.learning.dto.UpdateLearningResourceRequest;
import com.livo.api.modules.learning.entity.enums.LearningStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LearningService {

    LearningItemResponse createLearningItem(UUID userId, CreateLearningItemRequest request);

    LearningItemResponse getLearningItemById(UUID userId, UUID itemId);

    List<LearningItemResponse> getLearningItems(UUID userId, LearningStatus status, String category, UUID goalId);

    LearningItemResponse updateLearningItem(UUID userId, UUID itemId, UpdateLearningItemRequest request);

    void deleteLearningItem(UUID userId, UUID itemId);

    LearningResourceResponse addResource(UUID userId, UUID itemId, CreateLearningResourceRequest request);

    List<LearningResourceResponse> getResources(UUID userId, UUID itemId);

    LearningResourceResponse updateResource(UUID userId, UUID itemId, UUID resourceId, UpdateLearningResourceRequest request);

    LearningResourceResponse toggleResourceCompletion(UUID userId, UUID itemId, UUID resourceId);

    void deleteResource(UUID userId, UUID itemId, UUID resourceId);

    LearningSessionResponse logSession(UUID userId, LogLearningSessionRequest request);

    List<LearningSessionResponse> getSessions(UUID userId, UUID itemId, LocalDate startDate, LocalDate endDate);

    void deleteSession(UUID userId, UUID sessionId);

    LearningStatsResponse getLearningStats(UUID userId);
}
