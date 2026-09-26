package com.livo.api.modules.trip.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.trip.dto.CreateItineraryItemRequest;
import com.livo.api.modules.trip.dto.ItineraryItemResponse;
import com.livo.api.modules.trip.dto.UpdateItineraryItemRequest;
import com.livo.api.modules.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/itinerary")
@RequiredArgsConstructor
@Tag(name = "Travel", description = "Endpoints for managing day-wise activities and schedule items within trips")
public class ItineraryItemController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Add an itinerary item to a trip")
    public ResponseEntity<ApiResponse<ItineraryItemResponse>> addItineraryItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID tripId,
            @Valid @RequestBody CreateItineraryItemRequest request
    ) {
        ItineraryItemResponse response = tripService.addItineraryItem(currentUser.getId(), tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Itinerary item added successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all itinerary items for a trip in chronological order")
    public ResponseEntity<ApiResponse<List<ItineraryItemResponse>>> getItineraryItems(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID tripId
    ) {
        List<ItineraryItemResponse> response = tripService.getItineraryItems(currentUser.getId(), tripId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get a single itinerary item by ID")
    public ResponseEntity<ApiResponse<ItineraryItemResponse>> getItineraryItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID tripId,
            @PathVariable UUID itemId
    ) {
        ItineraryItemResponse response = tripService.getItineraryItem(currentUser.getId(), tripId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update an itinerary item")
    public ResponseEntity<ApiResponse<ItineraryItemResponse>> updateItineraryItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID tripId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateItineraryItemRequest request
    ) {
        ItineraryItemResponse response = tripService.updateItineraryItem(currentUser.getId(), tripId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Itinerary item updated successfully"));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Soft-delete an itinerary item")
    public ResponseEntity<ApiResponse<Void>> deleteItineraryItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID tripId,
            @PathVariable UUID itemId
    ) {
        tripService.deleteItineraryItem(currentUser.getId(), tripId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Itinerary item deleted successfully"));
    }
}
