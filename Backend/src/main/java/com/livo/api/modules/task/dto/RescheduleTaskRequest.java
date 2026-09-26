package com.livo.api.modules.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleTaskRequest {

    private LocalDate newDueDate;

    private LocalTime newDueTime;

    @Builder.Default
    private String action = "CUSTOM";
}
