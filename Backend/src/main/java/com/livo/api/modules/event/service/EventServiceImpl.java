package com.livo.api.modules.event.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.event.dto.UpdateEventRequest;
import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import com.livo.api.modules.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(UUID userId, Instant start, Instant end) {
        List<EventEntity> list = eventRepository.findFilteredEvents(userId, start, end);
        return list.stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID userId, UUID eventId) {
        EventEntity event = findEventOrThrow(userId, eventId);
        return EventResponse.fromEntity(event);
    }

    @Override
    @Transactional
    public EventResponse createEvent(UUID userId, CreateEventRequest request) {
        Instant startTime = request.getStartTime();
        Instant endTime = request.getEndTime() != null ? request.getEndTime() : startTime.plus(Duration.ofMinutes(60));
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("Event end time must be after start time");
        }

        EventEntity event = EventEntity.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .location(request.getLocation())
                .format(request.getFormat() != null ? request.getFormat() : EventFormat.IN_PERSON)
                .priority(request.getPriority() != null ? request.getPriority() : EventPriority.MEDIUM)
                .category(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory().trim() : "GENERAL")
                .startTime(startTime)
                .endTime(endTime)
                .reminderMinutes(request.getReminderMinutes())
                .repeatRule(request.getRepeatRule())
                .notes(request.getNotes())
                .tripId(request.getTripId())
                .build();
        event.setUserId(userId);
        event.setVersion(1L);

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID userId, UUID eventId, UpdateEventRequest request) {
        EventEntity event = findEventOrThrow(userId, eventId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getFormat() != null) {
            event.setFormat(request.getFormat());
        }
        if (request.getPriority() != null) {
            event.setPriority(request.getPriority());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            event.setCategory(request.getCategory().trim());
        }

        Instant newStart = request.getStartTime() != null ? request.getStartTime() : event.getStartTime();
        Instant newEnd = request.getEndTime() != null ? request.getEndTime() : event.getEndTime();
        if (!newEnd.isAfter(newStart)) {
            throw new BadRequestException("Event end time must be after start time");
        }
        event.setStartTime(newStart);
        event.setEndTime(newEnd);

        if (request.getReminderMinutes() != null) {
            event.setReminderMinutes(request.getReminderMinutes());
        }
        if (request.getRepeatRule() != null) {
            event.setRepeatRule(request.getRepeatRule());
        }
        if (request.getNotes() != null) {
            event.setNotes(request.getNotes());
        }
        if (request.getTripId() != null) {
            event.setTripId(request.getTripId());
        }

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void deleteEvent(UUID userId, UUID eventId) {
        EventEntity event = findEventOrThrow(userId, eventId);
        event.markDeleted();
        eventRepository.save(event);
    }

    private EventEntity findEventOrThrow(UUID userId, UUID eventId) {
        return eventRepository.findByIdAndUserIdAndDeletedAtIsNull(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
    }
}
