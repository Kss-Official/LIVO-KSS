package com.livo.api.modules.trip.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.dto.TripResponse;
import com.livo.api.modules.trip.dto.TripSummaryResponse;
import com.livo.api.modules.trip.dto.UpdateTripRequest;
import com.livo.api.modules.trip.entity.enums.TripType;
import com.livo.api.modules.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Travel", description = "Endpoints for managing trips, vacations, itineraries, and travel budgets")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Create a new trip")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateTripRequest request
    ) {
        TripResponse response = tripService.createTrip(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Trip created successfully"));
    }

    @GetMapping
    @Operation(summary = "List trips with optional filtering")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getTrips(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) TripType tripType,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) Boolean upcoming,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<TripResponse> response = tripService.getTrips(currentUser.getId(), tripType, goalId, upcoming, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID with aggregated travel and budget metrics")
    public ResponseEntity<ApiResponse<TripResponse>> getTrip(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        TripResponse response = tripService.getTrip(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing trip")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        TripResponse response = tripService.updateTrip(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Trip updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete trip and cascade to itinerary items")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        tripService.deleteTrip(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Trip deleted successfully"));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get detailed trip summary with itinerary and travel expenses")
    public ResponseEntity<ApiResponse<TripSummaryResponse>> getTripSummary(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        TripSummaryResponse response = tripService.getTripSummary(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
