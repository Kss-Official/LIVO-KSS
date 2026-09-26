package com.livo.api.modules.learning.dto;

import com.livo.api.modules.learning.entity.LearningItemEntity;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.entity.enums.StudyFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningItemResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String title;
    private String description;
    private String objective;
    private String notes;
    private LearningType learningType;
    private String category;
    private DifficultyLevel difficultyLevel;
    private int targetStudyTimeMinutes;
    private StudyFrequency studyFrequency;
    private short progressPercentage;
    private LearningStatus status;
    private LocalDate startDate;
    private LocalDate targetCompletionDate;

    private int totalStudyTimeMinutes;
    private long resourcesCount;
    private long completedResourcesCount;

    @Builder.Default
    private List<LearningResourceResponse> resources = new ArrayList<>();

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static LearningItemResponse fromEntity(
            LearningItemEntity entity,
            int totalStudyTimeMinutes,
            List<LearningResourceResponse> resources
    ) {
        if (entity == null) {
            return null;
        }
        List<LearningResourceResponse> resourceList = resources != null ? resources : new ArrayList<>();
        long totalRes = resourceList.size();
        long completedRes = resourceList.stream().filter(LearningResourceResponse::isCompleted).count();

        return LearningItemResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .objective(entity.getObjective())
                .notes(entity.getNotes())
                .learningType(entity.getLearningType())
                .category(entity.getCategory())
                .difficultyLevel(entity.getDifficultyLevel())
                .targetStudyTimeMinutes(entity.getTargetStudyTimeMinutes())
                .studyFrequency(entity.getStudyFrequency())
                .progressPercentage(entity.getProgressPercentage())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .targetCompletionDate(entity.getTargetCompletionDate())
                .totalStudyTimeMinutes(totalStudyTimeMinutes)
                .resourcesCount(totalRes)
                .completedResourcesCount(completedRes)
                .resources(resourceList)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
