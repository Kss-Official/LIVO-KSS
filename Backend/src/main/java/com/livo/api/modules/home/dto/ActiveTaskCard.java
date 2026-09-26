package com.livo.api.modules.home.dto;

import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
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
public class ActiveTaskCard {
    private UUID taskId;
    private String title;
    private TaskPriority priority;
    private TaskStatus status;
    private Instant startedAt;
    private long elapsedMinutes;
    private String projectLabel;
}
