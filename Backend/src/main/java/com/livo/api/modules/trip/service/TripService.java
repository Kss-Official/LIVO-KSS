package com.livo.api.modules.trip.service;

import com.livo.api.modules.trip.dto.CreateItineraryItemRequest;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.dto.ItineraryItemResponse;
import com.livo.api.modules.trip.dto.TripResponse;
import com.livo.api.modules.trip.dto.TripSummaryResponse;
import com.livo.api.modules.trip.dto.UpdateItineraryItemRequest;
import com.livo.api.modules.trip.dto.UpdateTripRequest;
import com.livo.api.modules.trip.entity.enums.TripType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripService {

    TripResponse createTrip(UUID userId, CreateTripRequest request);

    List<TripResponse> getTrips(UUID userId, TripType tripType, UUID goalId, Boolean upcoming, LocalDate startDate, LocalDate endDate);

    TripResponse getTrip(UUID userId, UUID tripId);

    TripResponse updateTrip(UUID userId, UUID tripId, UpdateTripRequest request);

    void deleteTrip(UUID userId, UUID tripId);

    TripSummaryResponse getTripSummary(UUID userId, UUID tripId);

    ItineraryItemResponse addItineraryItem(UUID userId, UUID tripId, CreateItineraryItemRequest request);

    List<ItineraryItemResponse> getItineraryItems(UUID userId, UUID tripId);

    ItineraryItemResponse getItineraryItem(UUID userId, UUID tripId, UUID itemId);

    ItineraryItemResponse updateItineraryItem(UUID userId, UUID tripId, UUID itemId, UpdateItineraryItemRequest request);

    void deleteItineraryItem(UUID userId, UUID tripId, UUID itemId);
}
