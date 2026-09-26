package com.livo.api.modules.trip.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing an individual schedule item or activity within a trip.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "itinerary_items")
public class ItineraryItemEntity extends BaseSyncEntity {

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @Column(name = "item_time")
    private LocalTime itemTime;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "notes", length = 500)
    private String notes;
}
