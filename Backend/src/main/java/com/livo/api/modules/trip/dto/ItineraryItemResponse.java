package com.livo.api.modules.trip.dto;

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
public class ItineraryItemResponse {

    private UUID id;
    private UUID tripId;
    private UUID userId;
    private LocalDate itemDate;
    private LocalTime itemTime;
    private String title;
    private String location;
    private String notes;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
