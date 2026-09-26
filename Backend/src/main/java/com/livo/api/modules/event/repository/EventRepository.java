package com.livo.api.modules.event.repository;

import com.livo.api.modules.event.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    List<EventEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<EventEntity> findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(UUID userId, Instant start, Instant end);

    Optional<EventEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<EventEntity> findAllByUserIdAndTripIdAndDeletedAtIsNull(UUID userId, UUID tripId);

    List<EventEntity> findAllByUserIdAndTripIdInAndDeletedAtIsNull(UUID userId, Collection<UUID> tripIds);

    List<EventEntity> findAllByUserIdAndUpdatedAtAfter(UUID userId, java.time.Instant since);

    Optional<EventEntity> findByIdAndUserId(UUID id, UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM EventEntity e WHERE e.userId = :userId AND e.deletedAt IS NULL " +
            "AND (:start IS NULL OR e.startTime >= :start) " +
            "AND (:end IS NULL OR e.startTime <= :end) " +
            "ORDER BY e.startTime ASC")
    List<EventEntity> findFilteredEvents(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("start") Instant start,
            @org.springframework.data.repository.query.Param("end") Instant end
    );
 
    @org.springframework.data.jpa.repository.Query("SELECT e FROM EventEntity e WHERE e.userId = :userId AND e.deletedAt IS NULL AND (" +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(e.description IS NOT NULL AND LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(e.location IS NOT NULL AND LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<EventEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}

