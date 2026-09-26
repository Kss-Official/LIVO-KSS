package com.livo.api.modules.plan.repository;

import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlockEntity, UUID> {

    List<ScheduleBlockEntity> findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(UUID userId, LocalDate blockDate);

    List<ScheduleBlockEntity> findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(UUID userId, LocalDate start, LocalDate end);

    Optional<ScheduleBlockEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<ScheduleBlockEntity> findAllByTaskIdAndUserIdAndDeletedAtIsNull(UUID taskId, UUID userId);

    List<ScheduleBlockEntity> findAllByEventIdAndUserIdAndDeletedAtIsNull(UUID eventId, UUID userId);

    List<ScheduleBlockEntity> findAllByHabitIdAndUserIdAndDeletedAtIsNull(UUID habitId, UUID userId);
}
