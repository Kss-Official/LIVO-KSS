package com.livo.api.modules.goal.dto;

import com.livo.api.modules.goal.entity.GoalProgressHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressHistoryResponse {

    private UUID id;
    private UUID goalId;
    private UUID userId;
    private LocalDate recordedDate;
    private BigDecimal previousValue;
    private BigDecimal newValue;
    private String notes;
    private Instant createdAt;

    public static GoalProgressHistoryResponse fromEntity(GoalProgressHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return GoalProgressHistoryResponse.builder()
                .id(entity.getId())
                .goalId(entity.getGoalId())
                .userId(entity.getUserId())
                .recordedDate(entity.getRecordedDate())
                .previousValue(entity.getPreviousValue())
                .newValue(entity.getNewValue())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
