package com.livo.api.modules.task.repository;

import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    List<TaskEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<TaskEntity> findAllByUserIdAndStatusAndDeletedAtIsNull(UUID userId, TaskStatus status);

    List<TaskEntity> findAllByUserIdAndDueDateAndDeletedAtIsNull(UUID userId, LocalDate dueDate);

    List<TaskEntity> findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(UUID userId, LocalDate start, LocalDate end);

    Optional<TaskEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<TaskEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    List<TaskEntity> findAllByUserIdAndTripIdAndDeletedAtIsNull(UUID userId, UUID tripId);

    List<TaskEntity> findAllByUserIdAndTripIdInAndDeletedAtIsNull(UUID userId, Collection<UUID> tripIds);

    List<TaskEntity> findAllByUserIdAndUpdatedAtAfter(UUID userId, java.time.Instant since);

    Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:dueDate IS NULL OR t.dueDate = :dueDate) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (t.dueDate IS NOT NULL AND t.dueDate >= :startDate AND t.dueDate <= :endDate)) " +
            "AND (:goalId IS NULL OR t.goalId = :goalId) " +
            "AND (:category IS NULL OR LOWER(t.category) = :category) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "ORDER BY t.dueDate ASC NULLS LAST, t.dueTime ASC NULLS LAST, t.createdAt DESC")
    List<TaskEntity> findFilteredTasks(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("status") TaskStatus status,
            @org.springframework.data.repository.query.Param("dueDate") LocalDate dueDate,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate,
            @org.springframework.data.repository.query.Param("goalId") UUID goalId,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("priority") TaskPriority priority
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL " +
            "AND t.status NOT IN (com.livo.api.modules.task.entity.enums.TaskStatus.COMPLETED, com.livo.api.modules.task.entity.enums.TaskStatus.CANCELLED) " +
            "AND (t.dueDate = :targetDate " +
            "     OR (t.dueDate IS NOT NULL AND t.dueDate < :targetDate AND t.priority IN (com.livo.api.modules.task.entity.enums.TaskPriority.URGENT, com.livo.api.modules.task.entity.enums.TaskPriority.HIGH)) " +
            "     OR t.status = com.livo.api.modules.task.entity.enums.TaskStatus.IN_PROGRESS) " +
            "ORDER BY " +
            "  CASE t.priority WHEN com.livo.api.modules.task.entity.enums.TaskPriority.URGENT THEN 4 " +
            "                  WHEN com.livo.api.modules.task.entity.enums.TaskPriority.HIGH THEN 3 " +
            "                  WHEN com.livo.api.modules.task.entity.enums.TaskPriority.MEDIUM THEN 2 " +
            "                  ELSE 1 END DESC, " +
            "  t.dueDate ASC NULLS LAST, " +
            "  t.dueTime ASC NULLS LAST")
    List<TaskEntity> findPriorityCandidates(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("targetDate") LocalDate targetDate,
            org.springframework.data.domain.Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<TaskEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.deletedAt IS NULL " +
            "AND t.status NOT IN (com.livo.api.modules.task.entity.enums.TaskStatus.COMPLETED, com.livo.api.modules.task.entity.enums.TaskStatus.CANCELLED) " +
            "AND t.dueDate = :dueDate AND t.dueTime IS NOT NULL AND t.dueTime >= :startTime AND t.dueTime <= :endTime")
    List<TaskEntity> findUpcomingTasksForReminder(
            @org.springframework.data.repository.query.Param("dueDate") LocalDate dueDate,
            @org.springframework.data.repository.query.Param("startTime") java.time.LocalTime startTime,
            @org.springframework.data.repository.query.Param("endTime") java.time.LocalTime endTime
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.deletedAt IS NULL " +
            "AND t.dueDate IS NOT NULL AND t.dueDate < :cutoff " +
            "AND t.status IN (com.livo.api.modules.task.entity.enums.TaskStatus.TODO, com.livo.api.modules.task.entity.enums.TaskStatus.IN_PROGRESS)")
    List<TaskEntity> findOverdueTasksForRollover(
            @org.springframework.data.repository.query.Param("cutoff") LocalDate cutoff
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t, UserEntity u " +
            "WHERE t.userId = u.id AND u.deletedAt IS NULL " +
            "AND (u.timezone = :timezone OR (:isDefault = true AND (u.timezone IS NULL OR u.timezone = ''))) " +
            "AND t.deletedAt IS NULL " +
            "AND t.status NOT IN (com.livo.api.modules.task.entity.enums.TaskStatus.COMPLETED, com.livo.api.modules.task.entity.enums.TaskStatus.CANCELLED) " +
            "AND t.dueDate = :dueDate AND t.dueTime IS NOT NULL AND t.dueTime >= :startTime AND t.dueTime <= :endTime")
    List<TaskEntity> findUpcomingTasksForReminderInTimezone(
            @org.springframework.data.repository.query.Param("timezone") String timezone,
            @org.springframework.data.repository.query.Param("isDefault") boolean isDefault,
            @org.springframework.data.repository.query.Param("dueDate") LocalDate dueDate,
            @org.springframework.data.repository.query.Param("startTime") java.time.LocalTime startTime,
            @org.springframework.data.repository.query.Param("endTime") java.time.LocalTime endTime
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t, UserEntity u " +
            "WHERE t.userId = u.id AND u.deletedAt IS NULL " +
            "AND (u.timezone = :timezone OR (:isDefault = true AND (u.timezone IS NULL OR u.timezone = ''))) " +
            "AND t.deletedAt IS NULL " +
            "AND t.status NOT IN (com.livo.api.modules.task.entity.enums.TaskStatus.COMPLETED, com.livo.api.modules.task.entity.enums.TaskStatus.CANCELLED) " +
            "AND t.dueTime IS NOT NULL " +
            "AND ((t.dueDate = :today AND t.dueTime >= :startTime) OR (t.dueDate = :tomorrow AND t.dueTime <= :endTime))")
    List<TaskEntity> findUpcomingTasksForReminderInTimezoneSpanningMidnight(
            @org.springframework.data.repository.query.Param("timezone") String timezone,
            @org.springframework.data.repository.query.Param("isDefault") boolean isDefault,
            @org.springframework.data.repository.query.Param("today") LocalDate today,
            @org.springframework.data.repository.query.Param("tomorrow") LocalDate tomorrow,
            @org.springframework.data.repository.query.Param("startTime") java.time.LocalTime startTime,
            @org.springframework.data.repository.query.Param("endTime") java.time.LocalTime endTime
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t, UserEntity u " +
            "WHERE t.userId = u.id AND u.deletedAt IS NULL " +
            "AND (u.timezone = :timezone OR (:isDefault = true AND (u.timezone IS NULL OR u.timezone = ''))) " +
            "AND t.deletedAt IS NULL " +
            "AND t.dueDate IS NOT NULL AND t.dueDate < :cutoff " +
            "AND t.status IN (com.livo.api.modules.task.entity.enums.TaskStatus.TODO, com.livo.api.modules.task.entity.enums.TaskStatus.IN_PROGRESS)")
    List<TaskEntity> findOverdueTasksForRolloverInTimezone(
            @org.springframework.data.repository.query.Param("timezone") String timezone,
            @org.springframework.data.repository.query.Param("isDefault") boolean isDefault,
            @org.springframework.data.repository.query.Param("cutoff") LocalDate cutoff
    );
}

