package com.livo.api.modules.home.dto;

import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityTaskCard {
    private UUID taskId;
    private String title;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer durationMins;
    private String projectLabel;
    private int rank;
    private String rankLabel;
    private String whyLivoPicked;
}
