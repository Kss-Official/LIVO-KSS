package com.livo.api.modules.routine.dto;

import com.livo.api.modules.routine.entity.RoutineEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;

    @Builder.Default
    private List<Integer> daysOfWeek = new ArrayList<>();

    private boolean isActive;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static RoutineResponse fromEntity(RoutineEntity entity) {
        if (entity == null) {
            return null;
        }
        return RoutineResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .daysOfWeek(entity.getDaysOfWeek() != null ? new ArrayList<>(entity.getDaysOfWeek()) : new ArrayList<>())
                .isActive(entity.isActive())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
