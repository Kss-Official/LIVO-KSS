package com.livo.api.modules.trip.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing travel plans, vacations, and business itineraries.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trips")
public class TripEntity extends BaseSyncEntity {

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "destination", nullable = false, length = 150)
    private String destination;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", nullable = false, length = 20)
    private TripType tripType = TripType.LEISURE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_mode", nullable = false, length = 20)
    private TravelMode travelMode = TravelMode.FLIGHT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "accommodation_type", nullable = false, length = 20)
    private AccommodationType accommodationType = AccommodationType.HOTEL;

    @Column(name = "accommodation_notes", length = 500)
    private String accommodationNotes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_with", nullable = false, length = 20)
    private TravelWith travelWith = TravelWith.SOLO;

    @Column(name = "budget_amount", precision = 12, scale = 2)
    private BigDecimal budgetAmount;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "notes", length = 1000)
    private String notes;
}
