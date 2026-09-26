package com.livo.api.modules.trip.dto;

import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateTripRequest {

    private UUID goalId;

    @Size(max = 150, message = "Trip title cannot exceed 150 characters")
    private String title;

    @Size(max = 150, message = "Destination cannot exceed 150 characters")
    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private TripType tripType;

    private TravelMode travelMode;

    private AccommodationType accommodationType;

    @Size(max = 500, message = "Accommodation notes cannot exceed 500 characters")
    private String accommodationNotes;

    private TravelWith travelWith;

    @DecimalMin(value = "0.00", message = "Budget amount must be non-negative")
    private BigDecimal budgetAmount;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
