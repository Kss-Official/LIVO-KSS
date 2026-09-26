package com.livo.api.modules.event.dto;

import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import jakarta.validation.constraints.Size;
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
public class UpdateEventRequest {

    @Size(min = 1, max = 150, message = "Event title must be between 1 and 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    private EventFormat format;

    private EventPriority priority;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private Instant startTime;

    private Instant endTime;

    private Integer reminderMinutes;

    @Size(max = 100, message = "Repeat rule cannot exceed 100 characters")
    private String repeatRule;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private UUID tripId;
}
