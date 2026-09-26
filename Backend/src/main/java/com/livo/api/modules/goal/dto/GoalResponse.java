package com.livo.api.modules.goal.dto;

import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private String relatedArea;
    private String targetDescription;
    private String category;
    private GoalPriority priority;
    private LocalDate targetDate;
    private GoalTrackingType progressTrackingType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private String reminderFrequency;
    private GoalStatus status;

    private Double progressPercentage;
    private long milestonesCount;
    private long completedMilestonesCount;

    @Builder.Default
    private List<MilestoneResponse> milestones = new ArrayList<>();

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static GoalResponse fromEntity(GoalEntity entity, List<MilestoneResponse> milestones) {
        if (entity == null) {
            return null;
        }

        List<MilestoneResponse> milestoneList = milestones != null ? milestones : new ArrayList<>();
        long totalMilestones = milestoneList.size();
        long completedMilestones = milestoneList.stream().filter(MilestoneResponse::isCompleted).count();

        Double progressPercentage = calculateProgressPercentage(
                entity.getProgressTrackingType(),
                entity.getCurrentValue(),
                entity.getTargetValue(),
                totalMilestones,
                completedMilestones
        );

        return GoalResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .relatedArea(entity.getRelatedArea())
                .targetDescription(entity.getTargetDescription())
                .category(entity.getCategory())
                .priority(entity.getPriority())
                .targetDate(entity.getTargetDate())
                .progressTrackingType(entity.getProgressTrackingType())
                .targetValue(entity.getTargetValue())
                .currentValue(entity.getCurrentValue())
                .unit(entity.getUnit())
                .reminderFrequency(entity.getReminderFrequency())
                .status(entity.getStatus())
                .progressPercentage(progressPercentage)
                .milestonesCount(totalMilestones)
                .completedMilestonesCount(completedMilestones)
                .milestones(milestoneList)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Double calculateProgressPercentage(
            GoalTrackingType type,
            BigDecimal currentValue,
            BigDecimal targetValue,
            long totalMilestones,
            long completedMilestones
    ) {
        if (type == null) {
            return 0.0;
        }

        switch (type) {
            case MILESTONE_BASED:
                if (totalMilestones == 0) {
                    return 0.0;
                }
                return BigDecimal.valueOf((double) completedMilestones / totalMilestones * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

            case NUMERICAL:
                if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0 || currentValue == null) {
                    return 0.0;
                }
                BigDecimal ratio = currentValue.divide(targetValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                return Math.min(100.0, Math.max(0.0, ratio.doubleValue()));

            case PERCENTAGE:
            default:
                if (currentValue == null) {
                    return 0.0;
                }
                return Math.min(100.0, Math.max(0.0, currentValue.setScale(2, RoundingMode.HALF_UP).doubleValue()));
        }
    }
}
