package com.livo.api.modules.trip.repository;

import com.livo.api.modules.trip.entity.TripEntity;
import com.livo.api.modules.trip.entity.enums.TripType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, UUID>, JpaSpecificationExecutor<TripEntity> {

    List<TripEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<TripEntity> findAllByUserIdAndDeletedAtIsNullOrderByStartDateAsc(UUID userId);

    Optional<TripEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<TripEntity> findAllByUserIdAndStartDateBetweenAndDeletedAtIsNullOrderByStartDateAsc(
            UUID userId, LocalDate startDate, LocalDate endDate
    );

    List<TripEntity> findAllByUserIdAndEndDateGreaterThanEqualAndDeletedAtIsNullOrderByStartDateAsc(
            UUID userId, LocalDate date
    );

    List<TripEntity> findAllByUserIdAndEndDateLessThanAndDeletedAtIsNullOrderByStartDateDesc(
            UUID userId, LocalDate date
    );

    List<TripEntity> findAllByUserIdAndTripTypeAndDeletedAtIsNull(UUID userId, TripType tripType);

    List<TripEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TripEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL " +
            "AND (:tripType IS NULL OR t.tripType = :tripType) " +
            "AND (:goalId IS NULL OR t.goalId = :goalId) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (t.startDate >= :startDate AND t.startDate <= :endDate)) " +
            "AND (:today IS NULL OR :upcoming IS NULL OR " +
            "     (:upcoming = true AND t.endDate >= :today) OR " +
            "     (:upcoming = false AND t.endDate < :today)) " +
            "ORDER BY t.startDate ASC")
    List<TripEntity> findFilteredTrips(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("tripType") TripType tripType,
            @org.springframework.data.repository.query.Param("goalId") UUID goalId,
            @org.springframework.data.repository.query.Param("upcoming") Boolean upcoming,
            @org.springframework.data.repository.query.Param("today") LocalDate today,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
    );
 
    @org.springframework.data.jpa.repository.Query("SELECT tr FROM TripEntity tr WHERE tr.userId = :userId AND tr.deletedAt IS NULL AND (" +
           "LOWER(tr.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(tr.destination IS NOT NULL AND LOWER(tr.destination) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<TripEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}
