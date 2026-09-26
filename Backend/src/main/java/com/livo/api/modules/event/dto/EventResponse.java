package com.livo.api.modules.event.dto;

import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private UUID id;
    private UUID userId;
    private UUID tripId;
    private String title;
    private String description;
    private String location;
    private EventFormat format;
    private EventPriority priority;
    private String category;
    private Instant startTime;
    private Instant endTime;
    private Integer reminderMinutes;
    private String repeatRule;

    @Builder.Default
    private List<LocalDate> skippedDates = new ArrayList<>();

    private String notes;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static EventResponse fromEntity(EventEntity entity) {
        if (entity == null) {
            return null;
        }
        return EventResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tripId(entity.getTripId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .format(entity.getFormat())
                .priority(entity.getPriority())
                .category(entity.getCategory())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .reminderMinutes(entity.getReminderMinutes())
                .repeatRule(entity.getRepeatRule())
                .skippedDates(entity.getSkippedDates() != null ? new ArrayList<>(entity.getSkippedDates()) : new ArrayList<>())
                .notes(entity.getNotes())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
