package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextUpCard {
    private UUID id;
    private String type; // "EVENT", "TASK_BLOCK", "ROUTINE"
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationOrCategory;
    private boolean isLiveNow;
}
