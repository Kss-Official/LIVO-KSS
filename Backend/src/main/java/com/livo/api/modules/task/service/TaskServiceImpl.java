package com.livo.api.modules.task.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
import com.livo.api.modules.tag.dto.TagResponse;
import com.livo.api.modules.tag.entity.TagEntity;
import com.livo.api.modules.tag.repository.TagRepository;
import com.livo.api.modules.task.dto.CreateSubtaskInlineRequest;
import com.livo.api.modules.task.dto.CreateSubtaskRequest;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.RescheduleTaskRequest;
import com.livo.api.modules.task.dto.SubtaskDto;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.dto.UpdateTaskRequest;
import com.livo.api.modules.task.entity.SubtaskEntity;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.TaskTagEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.SubtaskRepository;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.task.repository.TaskTagRepository;
import com.livo.api.modules.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final TaskTagRepository taskTagRepository;
    private final TagRepository tagRepository;
    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final TripRepository tripRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(
            UUID userId,
            TaskStatus status,
            LocalDate dueDate,
            LocalDate startDate,
            LocalDate endDate,
            UUID goalId,
            String category,
            TaskPriority priority
    ) {
        Specification<TaskEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (dueDate != null) {
                predicates.add(cb.equal(root.get("dueDate"), dueDate));
            } else if (startDate != null && endDate != null) {
                predicates.add(cb.and(
                        cb.isNotNull(root.get("dueDate")),
                        cb.greaterThanOrEqualTo(root.get("dueDate"), startDate),
                        cb.lessThanOrEqualTo(root.get("dueDate"), endDate)
                ));
            }
            if (goalId != null) {
                predicates.add(cb.equal(root.get("goalId"), goalId));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                Sort.Order.asc("dueDate").nullsLast(),
                Sort.Order.asc("dueTime").nullsLast(),
                Sort.Order.desc("createdAt")
        );

        List<TaskEntity> filteredTasks = taskRepository.findAll(spec, sort);

        if (filteredTasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> taskIds = filteredTasks.stream().map(TaskEntity::getId).collect(Collectors.toList());

        List<SubtaskEntity> allSubtasks = subtaskRepository
                .findAllByTaskIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(taskIds, userId);
        Map<UUID, List<SubtaskDto>> subtasksByTaskId = allSubtasks.stream()
                .collect(Collectors.groupingBy(
                        SubtaskEntity::getTaskId,
                        Collectors.mapping(SubtaskDto::fromEntity, Collectors.toList())
                ));

        List<TaskTagEntity> allTaskTags = taskTagRepository
                .findAllByTaskIdInAndUserIdAndDeletedAtIsNull(taskIds, userId);

        Set<UUID> tagIds = allTaskTags.stream().map(TaskTagEntity::getTagId).collect(Collectors.toSet());
        Map<UUID, TagResponse> tagMap = tagIds.isEmpty() ? Collections.emptyMap() :
                tagRepository.findAllByIdInAndUserIdAndDeletedAtIsNull(tagIds, userId).stream()
                        .collect(Collectors.toMap(TagEntity::getId, TagResponse::fromEntity));

        Map<UUID, List<TagResponse>> tagsByTaskId = new HashMap<>();
        for (TaskTagEntity tt : allTaskTags) {
            TagResponse tr = tagMap.get(tt.getTagId());
            if (tr != null) {
                tagsByTaskId.computeIfAbsent(tt.getTaskId(), k -> new ArrayList<>()).add(tr);
            }
        }

        return filteredTasks.stream()
                .map(t -> TaskResponse.fromEntity(
                        t,
                        subtasksByTaskId.getOrDefault(t.getId(), Collections.emptyList()),
                        tagsByTaskId.getOrDefault(t.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID userId, UUID taskId) {
        TaskEntity task = findTaskOrThrow(userId, taskId);
        return enrichTaskResponse(task, userId);
    }

    @Override
    @Transactional
    public TaskResponse createTask(UUID userId, CreateTaskRequest request) {
        UUID resolvedGoalId = validateRelatedEntities(userId, request.getGoalId(), request.getMilestoneId(), request.getTripId());

        TaskEntity task = TaskEntity.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .projectLabel(request.getProjectLabel())
                .dueDate(request.getDueDate())
                .dueTime(request.getDueTime() != null ? request.getDueTime().withNano(0) : null)
                .durationMins(request.getDurationMins() != null ? request.getDurationMins() : 30)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .category(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory().trim() : "WORK")
                .status(TaskStatus.TODO)
                .goalId(resolvedGoalId)
                .milestoneId(request.getMilestoneId())
                .tripId(request.getTripId())
                .repeatRule(request.getRepeatRule())
                .reminderMinutesBefore(request.getReminderMinutesBefore())
                .build();
        task.setUserId(userId);
        task.setVersion(1L);

        TaskEntity savedTask = taskRepository.save(task);

        // Inline Subtasks
        if (request.getSubtasks() != null && !request.getSubtasks().isEmpty()) {
            for (CreateSubtaskInlineRequest s : request.getSubtasks()) {
                SubtaskEntity subtask = SubtaskEntity.builder()
                        .taskId(savedTask.getId())
                        .title(s.getTitle().trim())
                        .sortOrder(s.getSortOrder())
                        .isCompleted(false)
                        .build();
                subtask.setUserId(userId);
                subtask.setVersion(1L);
                subtaskRepository.save(subtask);
            }
        }

        // Tags association
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (UUID tagId : request.getTagIds()) {
                if (tagRepository.findByIdAndUserIdAndDeletedAtIsNull(tagId, userId).isPresent()) {
                    TaskTagEntity taskTag = TaskTagEntity.builder()
                            .taskId(savedTask.getId())
                            .tagId(tagId)
                            .build();
                    taskTag.setUserId(userId);
                    taskTag.setVersion(1L);
                    taskTagRepository.save(taskTag);
                }
            }
        }

        return enrichTaskResponse(savedTask, userId);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request) {
        TaskEntity task = findTaskOrThrow(userId, taskId);
        TaskStatus oldStatus = task.getStatus();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getProjectLabel() != null) {
            task.setProjectLabel(request.getProjectLabel());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getDueTime() != null) {
            task.setDueTime(request.getDueTime().withNano(0));
        }
        if (request.getDurationMins() != null) {
            task.setDurationMins(request.getDurationMins());
        }
        if (request.getActualDurationMins() != null) {
            task.setActualDurationMins(request.getActualDurationMins());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            task.setCategory(request.getCategory().trim());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
                LocalDate today = LocalDate.now();
                if (!task.getCompletedDates().contains(today)) {
                    task.getCompletedDates().add(today);
                }
            } else if (request.getStatus() != TaskStatus.COMPLETED && oldStatus == TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
        }
        UUID targetGoalId = request.getGoalId() != null ? request.getGoalId() : task.getGoalId();
        UUID targetMilestoneId = request.getMilestoneId() != null ? request.getMilestoneId() : task.getMilestoneId();
        UUID targetTripId = request.getTripId() != null ? request.getTripId() : task.getTripId();

        if (request.getGoalId() != null || request.getMilestoneId() != null || request.getTripId() != null) {
            targetGoalId = validateRelatedEntities(userId, targetGoalId, targetMilestoneId, targetTripId);
        }

        if (request.getGoalId() != null) {
            task.setGoalId(targetGoalId);
        }
        if (request.getMilestoneId() != null) {
            task.setMilestoneId(targetMilestoneId);
            if (task.getGoalId() == null && targetGoalId != null) {
                task.setGoalId(targetGoalId);
            }
        }
        if (request.getTripId() != null) {
            task.setTripId(targetTripId);
        }
        if (request.getRepeatRule() != null) {
            task.setRepeatRule(request.getRepeatRule());
        }
        if (request.getReminderMinutesBefore() != null) {
            task.setReminderMinutesBefore(request.getReminderMinutesBefore());
        }

        // Update tags if provided
        if (request.getTagIds() != null) {
            List<TaskTagEntity> existingTags = taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(taskId, userId);
            for (TaskTagEntity tt : existingTags) {
                tt.markDeleted();
            }
            taskTagRepository.saveAll(existingTags);

            for (UUID tagId : request.getTagIds()) {
                if (tagRepository.findByIdAndUserIdAndDeletedAtIsNull(tagId, userId).isPresent()) {
                    TaskTagEntity taskTag = TaskTagEntity.builder()
                            .taskId(taskId)
                            .tagId(tagId)
                            .build();
                    taskTag.setUserId(userId);
                    taskTag.setVersion(1L);
                    taskTagRepository.save(taskTag);
                }
            }
        }

        if (oldStatus != TaskStatus.COMPLETED && task.getStatus() == TaskStatus.COMPLETED) {
            eventPublisher.publishEvent(new com.livo.api.common.event.TaskCompletedEvent(task.getId(), userId, task.getGoalId(), task.getTitle()));
        }

        return enrichTaskResponse(taskRepository.save(task), userId);
    }

    @Override
    @Transactional
    public TaskResponse toggleTaskCompletion(UUID userId, UUID taskId) {
        TaskEntity task = findTaskOrThrow(userId, taskId);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
        } else {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(Instant.now());
            LocalDate today = LocalDate.now();
            if (!task.getCompletedDates().contains(today)) {
                task.getCompletedDates().add(today);
            }
            eventPublisher.publishEvent(new com.livo.api.common.event.TaskCompletedEvent(task.getId(), userId, task.getGoalId(), task.getTitle()));
        }

        return enrichTaskResponse(taskRepository.save(task), userId);
    }

    @Override
    @Transactional
    public TaskResponse startTask(UUID userId, UUID taskId) {
        TaskEntity task = findTaskOrThrow(userId, taskId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        if (task.getStartedAt() == null) {
            task.setStartedAt(Instant.now());
        }
        return enrichTaskResponse(taskRepository.save(task), userId);
    }

    @Override
    @Transactional
    public TaskResponse rescheduleTask(UUID userId, UUID taskId, RescheduleTaskRequest request) {
        TaskEntity task = findTaskOrThrow(userId, taskId);
        String action = request.getAction() != null ? request.getAction().toUpperCase() : "CUSTOM";

        LocalDate newDate;
        switch (action) {
            case "TODAY" -> newDate = LocalDate.now();
            case "TOMORROW" -> newDate = LocalDate.now().plusDays(1);
            case "NEXT_WEEK" -> newDate = LocalDate.now().plusWeeks(1);
            case "DROP" -> {
                task.setStatus(TaskStatus.CANCELLED);
                task.getSkippedDates().add(LocalDate.now());
                return enrichTaskResponse(taskRepository.save(task), userId);
            }
            default -> newDate = request.getNewDueDate();
        }

        if (newDate != null) {
            task.setDueDate(newDate);
        }
        if (request.getNewDueTime() != null) {
            task.setDueTime(request.getNewDueTime());
        }

        return enrichTaskResponse(taskRepository.save(task), userId);
    }

    @Override
    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        TaskEntity task = findTaskOrThrow(userId, taskId);
        task.markDeleted();
        taskRepository.save(task);

        // Also soft delete associated subtasks
        List<SubtaskEntity> subtasks = subtaskRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(taskId, userId);
        for (SubtaskEntity subtask : subtasks) {
            subtask.markDeleted();
        }
        subtaskRepository.saveAll(subtasks);

        // Also soft delete associated task tags
        List<TaskTagEntity> taskTags = taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(taskId, userId);
        for (TaskTagEntity taskTag : taskTags) {
            taskTag.markDeleted();
        }
        taskTagRepository.saveAll(taskTags);
    }

    @Override
    @Transactional
    public SubtaskDto addSubtask(UUID userId, UUID taskId, CreateSubtaskRequest request) {
        findTaskOrThrow(userId, taskId);

        SubtaskEntity subtask = SubtaskEntity.builder()
                .taskId(taskId)
                .title(request.getTitle().trim())
                .sortOrder(request.getSortOrder())
                .isCompleted(false)
                .build();
        subtask.setUserId(userId);
        subtask.setVersion(1L);

        return SubtaskDto.fromEntity(subtaskRepository.save(subtask));
    }

    @Override
    @Transactional
    public SubtaskDto toggleSubtask(UUID userId, UUID taskId, UUID subtaskId) {
        findTaskOrThrow(userId, taskId);

        SubtaskEntity subtask = subtaskRepository.findByIdAndUserIdAndDeletedAtIsNull(subtaskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask", "id", subtaskId));

        subtask.setCompleted(!subtask.isCompleted());
        return SubtaskDto.fromEntity(subtaskRepository.save(subtask));
    }

    @Override
    @Transactional
    public void deleteSubtask(UUID userId, UUID taskId, UUID subtaskId) {
        findTaskOrThrow(userId, taskId);

        SubtaskEntity subtask = subtaskRepository.findByIdAndUserIdAndDeletedAtIsNull(subtaskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask", "id", subtaskId));

        subtask.markDeleted();
        subtaskRepository.save(subtask);
    }

    private TaskEntity findTaskOrThrow(UUID userId, UUID taskId) {
        return taskRepository.findByIdAndUserIdAndDeletedAtIsNull(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }

    private TaskResponse enrichTaskResponse(TaskEntity task, UUID userId) {
        List<SubtaskDto> subtasks = subtaskRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(task.getId(), userId)
                .stream()
                .map(SubtaskDto::fromEntity)
                .collect(Collectors.toList());

        List<TaskTagEntity> taskTags = taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(task.getId(), userId);
        List<TagResponse> tags = new ArrayList<>();
        for (TaskTagEntity tt : taskTags) {
            tagRepository.findByIdAndUserIdAndDeletedAtIsNull(tt.getTagId(), userId)
                    .ifPresent(tag -> tags.add(TagResponse.fromEntity(tag)));
        }

        return TaskResponse.fromEntity(task, subtasks, tags);
    }

    private UUID validateRelatedEntities(UUID userId, UUID goalId, UUID milestoneId, UUID tripId) {
        UUID effectiveGoalId = goalId;

        if (milestoneId != null) {
            MilestoneEntity milestone = milestoneRepository.findByIdAndUserIdAndDeletedAtIsNull(milestoneId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));

            if (goalId != null && !milestone.getGoalId().equals(goalId)) {
                throw new BadRequestException(String.format(
                        "Milestone %s belongs to goal %s, but task specified goal %s",
                        milestoneId, milestone.getGoalId(), goalId
                ));
            }
            if (effectiveGoalId == null) {
                effectiveGoalId = milestone.getGoalId();
            }
        }

        if (effectiveGoalId != null) {
            final UUID checkGoalId = effectiveGoalId;
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(checkGoalId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", checkGoalId));
        }

        if (tripId != null) {
            tripRepository.findByIdAndUserIdAndDeletedAtIsNull(tripId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
        }

        return effectiveGoalId;
    }
}
