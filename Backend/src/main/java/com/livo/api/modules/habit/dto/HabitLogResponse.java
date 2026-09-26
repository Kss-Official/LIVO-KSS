package com.livo.api.modules.habit.dto;

import com.livo.api.modules.habit.entity.HabitLogEntity;
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
public class HabitLogResponse {

    private UUID id;
    private UUID habitId;
    private UUID userId;
    private LocalDate logDate;
    private int countCompleted;
    private Instant loggedAt;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static HabitLogResponse fromEntity(HabitLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return HabitLogResponse.builder()
                .id(entity.getId())
                .habitId(entity.getHabitId())
                .userId(entity.getUserId())
                .logDate(entity.getLogDate())
                .countCompleted(entity.getCountCompleted())
                .loggedAt(entity.getLoggedAt())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
