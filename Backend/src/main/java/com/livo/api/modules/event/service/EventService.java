package com.livo.api.modules.event.service;

import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.event.dto.UpdateEventRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventService {

    List<EventResponse> getEvents(UUID userId, Instant start, Instant end);

    EventResponse getEventById(UUID userId, UUID eventId);

    EventResponse createEvent(UUID userId, CreateEventRequest request);

    EventResponse updateEvent(UUID userId, UUID eventId, UpdateEventRequest request);

    void deleteEvent(UUID userId, UUID eventId);
}
