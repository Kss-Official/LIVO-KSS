package com.livo.api.modules.learning.dto;

import com.livo.api.modules.learning.entity.LearningSessionEntity;
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
public class LearningSessionResponse {

    private UUID id;
    private UUID learningItemId;
    private UUID userId;
    private LocalDate sessionDate;
    private int durationMinutes;
    private String notes;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static LearningSessionResponse fromEntity(LearningSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return LearningSessionResponse.builder()
                .id(entity.getId())
                .learningItemId(entity.getLearningItemId())
                .userId(entity.getUserId())
                .sessionDate(entity.getSessionDate())
                .durationMinutes(entity.getDurationMinutes())
                .notes(entity.getNotes())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
