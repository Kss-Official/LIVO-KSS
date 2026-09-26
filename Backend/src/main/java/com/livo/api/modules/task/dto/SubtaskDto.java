package com.livo.api.modules.task.dto;

import com.livo.api.modules.task.entity.SubtaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskDto {

    private UUID id;
    private UUID taskId;
    private String title;
    private boolean isCompleted;
    private int sortOrder;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubtaskDto fromEntity(SubtaskEntity entity) {
        if (entity == null) {
            return null;
        }
        return SubtaskDto.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .title(entity.getTitle())
                .isCompleted(entity.isCompleted())
                .sortOrder(entity.getSortOrder())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
