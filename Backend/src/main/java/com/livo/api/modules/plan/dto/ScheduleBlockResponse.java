package com.livo.api.modules.plan.dto;

import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBlockResponse {

    private UUID id;
    private UUID userId;
    private UUID taskId;
    private UUID eventId;
    private UUID habitId;
    private LocalDate blockDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private String category;
    private boolean isLocked;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static ScheduleBlockResponse fromEntity(ScheduleBlockEntity entity) {
        if (entity == null) {
            return null;
        }
        return ScheduleBlockResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .taskId(entity.getTaskId())
                .eventId(entity.getEventId())
                .habitId(entity.getHabitId())
                .blockDate(entity.getBlockDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .isLocked(entity.isLocked())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
