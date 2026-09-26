package com.livo.api.modules.learning.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.entity.enums.StudyFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing a structured learning item, course, book, or skill project.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_items")
public class LearningItemEntity extends BaseSyncEntity {

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "objective", length = 300)
    private String objective;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "learning_type", nullable = false, length = 20)
    private LearningType learningType = LearningType.COURSE;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "TECH";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Builder.Default
    @Column(name = "target_study_time_minutes", nullable = false)
    private int targetStudyTimeMinutes = 30;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "study_frequency", nullable = false, length = 20)
    private StudyFrequency studyFrequency = StudyFrequency.DAILY;

    @Builder.Default
    @Column(name = "progress_percentage", nullable = false)
    private short progressPercentage = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LearningStatus status = LearningStatus.IN_PROGRESS;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;
}
