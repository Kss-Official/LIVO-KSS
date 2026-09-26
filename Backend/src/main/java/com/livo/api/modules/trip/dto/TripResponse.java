package com.livo.api.modules.trip.dto;

import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private TripType tripType;
    private TravelMode travelMode;
    private AccommodationType accommodationType;
    private String accommodationNotes;
    private TravelWith travelWith;
    private BigDecimal budgetAmount;
    private String currency;
    private String notes;

    // Aggregated metrics
    private long itineraryCount;
    private BigDecimal totalExpenses;
    private BigDecimal budgetRemaining;
    private long taskCount;
    private long eventCount;
    private boolean completed;
    private boolean ongoing;

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
