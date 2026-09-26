package com.livo.api.modules.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateItineraryItemRequest {

    @NotNull(message = "Item date is required")
    private LocalDate itemDate;

    private LocalTime itemTime;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
