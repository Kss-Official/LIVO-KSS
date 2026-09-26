package com.livo.api.modules.event.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.event.dto.UpdateEventRequest;
import com.livo.api.modules.event.service.EventService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @CurrentUser UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        List<EventResponse> events = eventService.getEvents(userId, start, end);
        return ResponseEntity.ok(ApiResponse.success(events, "Events retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        EventResponse response = eventService.getEventById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Event retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventResponse response = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Event created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        EventResponse response = eventService.updateEvent(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        eventService.deleteEvent(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }
}
