package com.livo.api.modules.trip.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.trip.dto.CreateItineraryItemRequest;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.dto.ItineraryItemResponse;
import com.livo.api.modules.trip.dto.TripResponse;
import com.livo.api.modules.trip.dto.TripSummaryResponse;
import com.livo.api.modules.trip.dto.UpdateItineraryItemRequest;
import com.livo.api.modules.trip.dto.UpdateTripRequest;
import com.livo.api.modules.trip.entity.ItineraryItemEntity;
import com.livo.api.modules.trip.entity.TripEntity;
import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import com.livo.api.modules.trip.repository.ItineraryItemRepository;
import com.livo.api.modules.trip.repository.TripRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public TripResponse createTrip(UUID userId, CreateTripRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Trip end date cannot be before start date");
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }

        TripEntity entity = TripEntity.builder()
                .goalId(request.getGoalId())
                .title(request.getTitle().trim())
                .destination(request.getDestination().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .tripType(request.getTripType() != null ? request.getTripType() : TripType.LEISURE)
                .travelMode(request.getTravelMode() != null ? request.getTravelMode() : TravelMode.FLIGHT)
                .accommodationType(request.getAccommodationType() != null ? request.getAccommodationType() : AccommodationType.HOTEL)
                .accommodationNotes(request.getAccommodationNotes())
                .travelWith(request.getTravelWith() != null ? request.getTravelWith() : TravelWith.SOLO)
                .budgetAmount(request.getBudgetAmount())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .notes(request.getNotes())
                .build();

        entity.setUserId(userId);
        entity.setVersion(1L);

        TripEntity saved = tripRepository.save(entity);
        log.info("Created trip {} ({}) for user {}", saved.getId(), saved.getTitle(), userId);
        return mapToTripResponse(saved, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTrips(UUID userId, TripType tripType, UUID goalId, Boolean upcoming, LocalDate startDate, LocalDate endDate) {
        List<TripEntity> filteredTrips;

        if (tripType == null && goalId == null && upcoming == null && startDate == null && endDate == null) {
            filteredTrips = tripRepository.findAllByUserIdAndDeletedAtIsNullOrderByStartDateAsc(userId);
        } else {
            Specification<TripEntity> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("userId"), userId));
                predicates.add(cb.isNull(root.get("deletedAt")));

                if (tripType != null) {
                    predicates.add(cb.equal(root.get("tripType"), tripType));
                }

                if (goalId != null) {
                    predicates.add(cb.equal(root.get("goalId"), goalId));
                }

                if (Boolean.TRUE.equals(upcoming)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), LocalDate.now()));
                } else if (Boolean.FALSE.equals(upcoming)) {
                    predicates.add(cb.lessThan(root.get("endDate"), LocalDate.now()));
                }

                if (startDate != null && endDate != null) {
                    predicates.add(cb.between(root.get("startDate"), startDate, endDate));
                } else if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
                } else if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), endDate));
                }

                if (Boolean.FALSE.equals(upcoming)) {
                    query.orderBy(cb.desc(root.get("startDate")));
                } else {
                    query.orderBy(cb.asc(root.get("startDate")));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            filteredTrips = tripRepository.findAll(spec);
        }

        if (filteredTrips.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> tripIds = filteredTrips.stream().map(TripEntity::getId).collect(Collectors.toList());

        Map<UUID, Long> itineraryCounts = itineraryItemRepository
                .findAllByTripIdInAndUserIdAndDeletedAtIsNull(tripIds, userId)
                .stream()
                .collect(Collectors.groupingBy(ItineraryItemEntity::getTripId, Collectors.counting()));

        Map<UUID, BigDecimal> totalExpensesByTrip = transactionRepository
                .findAllByUserIdAndTripIdInAndDeletedAtIsNull(userId, tripIds)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        TransactionEntity::getTripId,
                        Collectors.reducing(BigDecimal.ZERO, TransactionEntity::getAmount, BigDecimal::add)
                ));

        Map<UUID, Long> taskCountByTrip = taskRepository
                .findAllByUserIdAndTripIdInAndDeletedAtIsNull(userId, tripIds)
                .stream()
                .collect(Collectors.groupingBy(TaskEntity::getTripId, Collectors.counting()));

        Map<UUID, Long> eventCountByTrip = eventRepository
                .findAllByUserIdAndTripIdInAndDeletedAtIsNull(userId, tripIds)
                .stream()
                .collect(Collectors.groupingBy(EventEntity::getTripId, Collectors.counting()));

        return filteredTrips.stream()
                .map(t -> buildTripResponse(
                        t,
                        itineraryCounts.getOrDefault(t.getId(), 0L),
                        totalExpensesByTrip.getOrDefault(t.getId(), BigDecimal.ZERO),
                        taskCountByTrip.getOrDefault(t.getId(), 0L),
                        eventCountByTrip.getOrDefault(t.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTrip(UUID userId, UUID tripId) {
        TripEntity entity = findTripOrThrow(tripId, userId);
        return mapToTripResponse(entity, userId);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(UUID userId, UUID tripId, UpdateTripRequest request) {
        TripEntity trip = findTripOrThrow(tripId, userId);

        LocalDate effectiveStart = request.getStartDate() != null ? request.getStartDate() : trip.getStartDate();
        LocalDate effectiveEnd = request.getEndDate() != null ? request.getEndDate() : trip.getEndDate();

        if (effectiveEnd.isBefore(effectiveStart)) {
            throw new BadRequestException("Trip end date cannot be before start date");
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
            trip.setGoalId(request.getGoalId());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            trip.setTitle(request.getTitle().trim());
        }
        if (request.getDestination() != null && !request.getDestination().isBlank()) {
            trip.setDestination(request.getDestination().trim());
        }
        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        }
        if (request.getTripType() != null) {
            trip.setTripType(request.getTripType());
        }
        if (request.getTravelMode() != null) {
            trip.setTravelMode(request.getTravelMode());
        }
        if (request.getAccommodationType() != null) {
            trip.setAccommodationType(request.getAccommodationType());
        }
        if (request.getAccommodationNotes() != null) {
            trip.setAccommodationNotes(request.getAccommodationNotes());
        }
        if (request.getTravelWith() != null) {
            trip.setTravelWith(request.getTravelWith());
        }
        if (request.getBudgetAmount() != null) {
            trip.setBudgetAmount(request.getBudgetAmount());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            trip.setCurrency(request.getCurrency().toUpperCase());
        }
        if (request.getNotes() != null) {
            trip.setNotes(request.getNotes());
        }

        trip.setVersion(trip.getVersion() != null ? trip.getVersion() + 1 : 1L);
        TripEntity updated = tripRepository.save(trip);
        log.info("Updated trip {} for user {}", tripId, userId);
        return mapToTripResponse(updated, userId);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID userId, UUID tripId) {
        TripEntity trip = findTripOrThrow(tripId, userId);
        Instant now = Instant.now();

        trip.setDeletedAt(now);
        trip.setVersion(trip.getVersion() != null ? trip.getVersion() + 1 : 1L);
        tripRepository.save(trip);

        // Cascade soft-delete itinerary items
        List<ItineraryItemEntity> items = itineraryItemRepository.findAllByTripIdAndUserIdAndDeletedAtIsNull(tripId, userId);
        items.forEach(it -> {
            it.setDeletedAt(now);
            it.setVersion(it.getVersion() != null ? it.getVersion() + 1 : 1L);
        });
        itineraryItemRepository.saveAll(items);

        log.info("Soft-deleted trip {} and {} itinerary items for user {}", tripId, items.size(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public TripSummaryResponse getTripSummary(UUID userId, UUID tripId) {
        TripEntity trip = findTripOrThrow(tripId, userId);
        TripResponse tripResponse = mapToTripResponse(trip, userId);

        List<ItineraryItemEntity> itineraryEntities = itineraryItemRepository
                .findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(tripId, userId);
        List<ItineraryItemResponse> itineraryResponses = itineraryEntities.stream()
                .map(this::mapToItineraryResponse)
                .collect(Collectors.toList());

        List<TransactionEntity> transactions = transactionRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, tripId);
        List<TransactionResponse> recentTransactions = transactions.stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(10)
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        long expenseCount = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).count();
        long taskCount = taskRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, tripId).size();
        long eventCount = eventRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, tripId).size();

        long tripDurationDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        long daysUntilTrip = ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());

        return TripSummaryResponse.builder()
                .trip(tripResponse)
                .itinerary(itineraryResponses)
                .totalExpenses(tripResponse.getTotalExpenses())
                .expenseCount(expenseCount)
                .recentTransactions(recentTransactions)
                .linkedTaskCount(taskCount)
                .linkedEventCount(eventCount)
                .tripDurationDays(tripDurationDays)
                .daysUntilTrip(daysUntilTrip)
                .build();
    }

    @Override
    @Transactional
    public ItineraryItemResponse addItineraryItem(UUID userId, UUID tripId, CreateItineraryItemRequest request) {
        TripEntity trip = findTripOrThrow(tripId, userId);

        if (request.getItemDate().isBefore(trip.getStartDate()) || request.getItemDate().isAfter(trip.getEndDate())) {
            throw new BadRequestException("Itinerary item date (" + request.getItemDate() + 
                    ") must be within trip dates (" + trip.getStartDate() + " to " + trip.getEndDate() + ")");
        }

        ItineraryItemEntity entity = ItineraryItemEntity.builder()
                .tripId(tripId)
                .itemDate(request.getItemDate())
                .itemTime(request.getItemTime())
                .title(request.getTitle().trim())
                .location(request.getLocation())
                .notes(request.getNotes())
                .build();

        entity.setUserId(userId);
        entity.setVersion(1L);

        ItineraryItemEntity saved = itineraryItemRepository.save(entity);
        log.info("Added itinerary item {} to trip {} for user {}", saved.getId(), tripId, userId);
        return mapToItineraryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryItemResponse> getItineraryItems(UUID userId, UUID tripId) {
        findTripOrThrow(tripId, userId);
        return itineraryItemRepository
                .findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(tripId, userId)
                .stream()
                .map(this::mapToItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItineraryItemResponse getItineraryItem(UUID userId, UUID tripId, UUID itemId) {
        findTripOrThrow(tripId, userId);
        ItineraryItemEntity entity = itineraryItemRepository
                .findByIdAndTripIdAndUserIdAndDeletedAtIsNull(itemId, tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item not found: " + itemId));
        return mapToItineraryResponse(entity);
    }

    @Override
    @Transactional
    public ItineraryItemResponse updateItineraryItem(UUID userId, UUID tripId, UUID itemId, UpdateItineraryItemRequest request) {
        TripEntity trip = findTripOrThrow(tripId, userId);
        ItineraryItemEntity item = itineraryItemRepository
                .findByIdAndTripIdAndUserIdAndDeletedAtIsNull(itemId, tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item not found: " + itemId));

        if (request.getItemDate() != null) {
            if (request.getItemDate().isBefore(trip.getStartDate()) || request.getItemDate().isAfter(trip.getEndDate())) {
                throw new BadRequestException("Itinerary item date (" + request.getItemDate() + 
                        ") must be within trip dates (" + trip.getStartDate() + " to " + trip.getEndDate() + ")");
            }
            item.setItemDate(request.getItemDate());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getItemTime() != null) {
            item.setItemTime(request.getItemTime());
        }
        if (request.getLocation() != null) {
            item.setLocation(request.getLocation());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }

        item.setVersion(item.getVersion() != null ? item.getVersion() + 1 : 1L);
        ItineraryItemEntity updated = itineraryItemRepository.save(item);
        log.info("Updated itinerary item {} in trip {} for user {}", itemId, tripId, userId);
        return mapToItineraryResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItineraryItem(UUID userId, UUID tripId, UUID itemId) {
        findTripOrThrow(tripId, userId);
        ItineraryItemEntity item = itineraryItemRepository
                .findByIdAndTripIdAndUserIdAndDeletedAtIsNull(itemId, tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item not found: " + itemId));

        item.setDeletedAt(Instant.now());
        item.setVersion(item.getVersion() != null ? item.getVersion() + 1 : 1L);
        itineraryItemRepository.save(item);
        log.info("Soft-deleted itinerary item {} from trip {} for user {}", itemId, tripId, userId);
    }

    private TripEntity findTripOrThrow(UUID tripId, UUID userId) {
        return tripRepository.findByIdAndUserIdAndDeletedAtIsNull(tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
    }

    private TripResponse mapToTripResponse(TripEntity entity, UUID userId) {
        long itineraryCount = itineraryItemRepository.countByTripIdAndUserIdAndDeletedAtIsNull(entity.getId(), userId);

        List<TransactionEntity> transactions = transactionRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, entity.getId());
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long taskCount = taskRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, entity.getId()).size();
        long eventCount = eventRepository.findAllByUserIdAndTripIdAndDeletedAtIsNull(userId, entity.getId()).size();

        return buildTripResponse(entity, itineraryCount, totalExpenses, taskCount, eventCount);
    }

    private TripResponse buildTripResponse(
            TripEntity entity,
            long itineraryCount,
            BigDecimal totalExpenses,
            long taskCount,
            long eventCount
    ) {
        BigDecimal budgetRemaining = entity.getBudgetAmount() != null
                ? entity.getBudgetAmount().subtract(totalExpenses)
                : null;

        LocalDate today = LocalDate.now();
        boolean completed = today.isAfter(entity.getEndDate());
        boolean ongoing = !today.isBefore(entity.getStartDate()) && !today.isAfter(entity.getEndDate());

        return TripResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .title(entity.getTitle())
                .destination(entity.getDestination())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .tripType(entity.getTripType())
                .travelMode(entity.getTravelMode())
                .accommodationType(entity.getAccommodationType())
                .accommodationNotes(entity.getAccommodationNotes())
                .travelWith(entity.getTravelWith())
                .budgetAmount(entity.getBudgetAmount())
                .currency(entity.getCurrency())
                .notes(entity.getNotes())
                .itineraryCount(itineraryCount)
                .totalExpenses(totalExpenses)
                .budgetRemaining(budgetRemaining)
                .taskCount(taskCount)
                .eventCount(eventCount)
                .completed(completed)
                .ongoing(ongoing)
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ItineraryItemResponse mapToItineraryResponse(ItineraryItemEntity entity) {
        return ItineraryItemResponse.builder()
                .id(entity.getId())
                .tripId(entity.getTripId())
                .userId(entity.getUserId())
                .itemDate(entity.getItemDate())
                .itemTime(entity.getItemTime())
                .title(entity.getTitle())
                .location(entity.getLocation())
                .notes(entity.getNotes())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
