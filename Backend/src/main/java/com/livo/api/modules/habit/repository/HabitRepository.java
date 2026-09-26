package com.livo.api.modules.habit.repository;

import com.livo.api.modules.habit.entity.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {

    List<HabitEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<HabitEntity> findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(UUID userId);

    Optional<HabitEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<HabitEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    List<HabitEntity> findAllByUserIdAndIsArchivedTrueAndDeletedAtIsNull(UUID userId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    List<HabitEntity> findAllByUserIdAndUpdatedAtAfter(UUID userId, java.time.Instant since);

    Optional<HabitEntity> findByIdAndUserId(UUID id, UUID userId);
 
    @org.springframework.data.jpa.repository.Query("SELECT h FROM HabitEntity h WHERE h.userId = :userId AND h.deletedAt IS NULL AND (" +
           "LOWER(h.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(h.description IS NOT NULL AND LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(h.motivationNote IS NOT NULL AND LOWER(h.motivationNote) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<HabitEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}

