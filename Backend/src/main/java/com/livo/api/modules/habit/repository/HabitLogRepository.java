package com.livo.api.modules.habit.repository;

import com.livo.api.modules.habit.entity.HabitLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLogEntity, UUID> {

    List<HabitLogEntity> findAllByHabitIdAndUserIdAndDeletedAtIsNull(UUID habitId, UUID userId);

    Optional<HabitLogEntity> findByHabitIdAndLogDateAndDeletedAtIsNull(UUID habitId, LocalDate logDate);

    Optional<HabitLogEntity> findByHabitIdAndUserIdAndLogDateAndDeletedAtIsNull(UUID habitId, UUID userId, LocalDate logDate);

    List<HabitLogEntity> findAllByUserIdAndLogDateAndDeletedAtIsNull(UUID userId, LocalDate logDate);

    List<HabitLogEntity> findAllByHabitIdAndUserIdAndDeletedAtIsNullOrderByLogDateDesc(UUID habitId, UUID userId);

    List<HabitLogEntity> findAllByHabitIdAndLogDateBetweenAndDeletedAtIsNullOrderByLogDateAsc(UUID habitId, LocalDate start, LocalDate end);

    List<HabitLogEntity> findAllByHabitIdAndUserIdAndLogDateBetweenAndDeletedAtIsNullOrderByLogDateAsc(UUID habitId, UUID userId, LocalDate start, LocalDate end);

    boolean existsByHabitIdAndLogDateAndDeletedAtIsNull(UUID habitId, LocalDate logDate);

    long countByHabitIdAndUserIdAndDeletedAtIsNull(UUID habitId, UUID userId);

    long countByHabitIdInAndUserIdAndLogDateBetweenAndDeletedAtIsNull(Collection<UUID> habitIds, UUID userId, LocalDate start, LocalDate end);
}
