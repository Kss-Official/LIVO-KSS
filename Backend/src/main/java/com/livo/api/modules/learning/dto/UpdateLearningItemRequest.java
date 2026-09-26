package com.livo.api.modules.learning.dto;

import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.entity.enums.StudyFrequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLearningItemRequest {

    private UUID goalId;

    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 300, message = "Objective cannot exceed 300 characters")
    private String objective;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private LearningType learningType;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private DifficultyLevel difficultyLevel;

    @Min(value = 1, message = "Target study time must be at least 1 minute")
    private Integer targetStudyTimeMinutes;

    private StudyFrequency studyFrequency;

    @Min(value = 0, message = "Progress must be between 0 and 100")
    @Max(value = 100, message = "Progress must be between 0 and 100")
    private Short progressPercentage;

    private LearningStatus status;

    private LocalDate startDate;
    private LocalDate targetCompletionDate;
}
