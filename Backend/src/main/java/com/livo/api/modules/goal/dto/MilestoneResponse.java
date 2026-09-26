package com.livo.api.modules.goal.dto;

import com.livo.api.modules.goal.entity.MilestoneEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {

    private UUID id;
    private UUID goalId;
    private UUID userId;
    private String title;
    private LocalDate targetDate;
    private boolean isCompleted;
    private int sortOrder;
    private boolean isAiGenerated;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static MilestoneResponse fromEntity(MilestoneEntity entity) {
        if (entity == null) {
            return null;
        }
        return MilestoneResponse.builder()
                .id(entity.getId())
                .goalId(entity.getGoalId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .targetDate(entity.getTargetDate())
                .isCompleted(entity.isCompleted())
                .sortOrder(entity.getSortOrder())
                .isAiGenerated(entity.isAiGenerated())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
