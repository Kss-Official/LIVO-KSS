package com.livo.api.modules.health.dto;

import com.livo.api.modules.health.entity.HealthEntryEntity;
import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEntryResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String title;
    private String description;
    private HealthType healthType;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private Integer durationMins;
    private HealthIntensity intensity;
    private Map<String, Object> metricsJson;
    private String notes;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static HealthEntryResponse fromEntity(HealthEntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return HealthEntryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .healthType(entity.getHealthType())
                .entryDate(entity.getEntryDate())
                .entryTime(entity.getEntryTime())
                .durationMins(entity.getDurationMins())
                .intensity(entity.getIntensity())
                .metricsJson(entity.getMetricsJson())
                .notes(entity.getNotes())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
