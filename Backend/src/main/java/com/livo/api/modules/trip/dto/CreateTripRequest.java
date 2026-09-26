package com.livo.api.modules.trip.dto;

import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    private UUID goalId;

    @NotBlank(message = "Trip title is required")
    @Size(max = 150, message = "Trip title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Destination is required")
    @Size(max = 150, message = "Destination cannot exceed 150 characters")
    private String destination;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Builder.Default
    private TripType tripType = TripType.LEISURE;

    @Builder.Default
    private TravelMode travelMode = TravelMode.FLIGHT;

    @Builder.Default
    private AccommodationType accommodationType = AccommodationType.HOTEL;

    @Size(max = 500, message = "Accommodation notes cannot exceed 500 characters")
    private String accommodationNotes;

    @Builder.Default
    private TravelWith travelWith = TravelWith.SOLO;

    @DecimalMin(value = "0.00", message = "Budget amount must be non-negative")
    private BigDecimal budgetAmount;

    @Builder.Default
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "INR";

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
