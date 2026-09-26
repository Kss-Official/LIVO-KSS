package com.livo.api.modules.health.repository;

import com.livo.api.modules.health.entity.HealthEntryEntity;
import com.livo.api.modules.health.entity.enums.HealthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthEntryRepository extends JpaRepository<HealthEntryEntity, UUID> {

    List<HealthEntryEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<HealthEntryEntity> findAllByUserIdAndDeletedAtIsNullOrderByEntryDateDescEntryTimeDesc(UUID userId);

    Optional<HealthEntryEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<HealthEntryEntity> findAllByUserIdAndHealthTypeAndDeletedAtIsNull(UUID userId, HealthType healthType);

    List<HealthEntryEntity> findAllByUserIdAndEntryDateBetweenAndDeletedAtIsNullOrderByEntryDateDesc(
            UUID userId, LocalDate startDate, LocalDate endDate
    );

    List<HealthEntryEntity> findAllByUserIdAndEntryDateBetweenAndDeletedAtIsNullOrderByEntryDateDescEntryTimeDesc(
            UUID userId, LocalDate startDate, LocalDate endDate
    );

    List<HealthEntryEntity> findAllByUserIdAndEntryDateAndDeletedAtIsNullOrderByEntryTimeAsc(
            UUID userId, LocalDate entryDate
    );

    List<HealthEntryEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM HealthEntryEntity h WHERE h.userId = :userId AND h.deletedAt IS NULL " +
            "AND (:healthType IS NULL OR h.healthType = :healthType) " +
            "AND (:goalId IS NULL OR h.goalId = :goalId) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (h.entryDate >= :startDate AND h.entryDate <= :endDate)) " +
            "ORDER BY h.entryDate DESC, h.entryTime DESC NULLS LAST")
    List<HealthEntryEntity> findFilteredHealthEntries(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("healthType") HealthType healthType,
            @org.springframework.data.repository.query.Param("goalId") UUID goalId,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
    );
}
