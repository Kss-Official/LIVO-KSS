package com.livo.api.modules.ai.service;

import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.ai.dto.AiFeedbackRequest;
import com.livo.api.modules.ai.dto.AiRecommendationResponse;
import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.entity.enums.AiPermissionLevel;
import com.livo.api.modules.ai.entity.enums.AiRecommendationPriority;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import com.livo.api.modules.ai.entity.enums.AiRelatedEntityType;
import com.livo.api.modules.ai.repository.AiRecommendationRepository;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final AiRecommendationRepository aiRecommendationRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;

    @Transactional
    public List<AiRecommendationResponse> refreshRecommendations(UUID userId) {
        LocalDate today = LocalDate.now();
        List<AiRecommendationEntity> generated = new ArrayList<>();

        // 1. Check for Overdue Tasks
        List<TaskEntity> overdue = taskRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .toList();

        if (!overdue.isEmpty()) {
            TaskEntity topOverdue = overdue.get(0);
            String dedupeKey = "OVERDUE_" + topOverdue.getId();
            Optional<AiRecommendationEntity> existing = aiRecommendationRepository.findByUserIdAndDedupeKeyAndIsActiveTrue(userId, dedupeKey);

            if (existing.isEmpty()) {
                AiRecommendationEntity rec = AiRecommendationEntity.builder()
                        .userId(userId)
                        .type(AiRecommendationType.OVERDUE_TASK)
                        .title("Overdue Task: " + topOverdue.getTitle())
                        .reason("This task was scheduled for " + topOverdue.getDueDate() + " and is awaiting completion.")
                        .recommendationPriority(AiRecommendationPriority.CRITICAL)
                        .permissionLevel(AiPermissionLevel.SUGGEST)
                        .relatedEntityType(AiRelatedEntityType.TASK)
                        .relatedEntityId(topOverdue.getId())
                        .actionType("RESCHEDULE_TASK")
                        .dedupeKey(dedupeKey)
                        .isActive(true)
                        .build();
                generated.add(aiRecommendationRepository.save(rec));
            }
        }

        // 2. Check for High Priority Tasks Due Today
        List<TaskEntity> highPriorityToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(userId, today).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                .filter(t -> t.getPriority() == TaskPriority.HIGH || t.getPriority() == TaskPriority.URGENT)
                .toList();

        if (!highPriorityToday.isEmpty()) {
            TaskEntity priorityTask = highPriorityToday.get(0);
            String dedupeKey = "PRIORITY_" + priorityTask.getId();
            Optional<AiRecommendationEntity> existing = aiRecommendationRepository.findByUserIdAndDedupeKeyAndIsActiveTrue(userId, dedupeKey);

            if (existing.isEmpty()) {
                AiRecommendationEntity rec = AiRecommendationEntity.builder()
                        .userId(userId)
                        .type(AiRecommendationType.HIGH_PRIORITY_TASK)
                        .title("Priority Focus: " + priorityTask.getTitle())
                        .reason("High impact task scheduled for today. Recommend blocking 90 minutes of uninterrupted focus.")
                        .recommendationPriority(AiRecommendationPriority.HIGH)
                        .permissionLevel(AiPermissionLevel.SUGGEST)
                        .relatedEntityType(AiRelatedEntityType.TASK)
                        .relatedEntityId(priorityTask.getId())
                        .actionType("FOCUS_BLOCK")
                        .dedupeKey(dedupeKey)
                        .isActive(true)
                        .build();
                generated.add(aiRecommendationRepository.save(rec));
            }
        }

        // 3. Check for Active Habit Streaks
        List<HabitEntity> habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        if (!habits.isEmpty()) {
            HabitEntity habit = habits.get(0);
            String dedupeKey = "HABIT_STREAK_" + habit.getId();
            Optional<AiRecommendationEntity> existing = aiRecommendationRepository.findByUserIdAndDedupeKeyAndIsActiveTrue(userId, dedupeKey);

            if (existing.isEmpty()) {
                AiRecommendationEntity rec = AiRecommendationEntity.builder()
                        .userId(userId)
                        .type(AiRecommendationType.HABIT_STREAK)
                        .title("Maintain Habit: " + habit.getTitle())
                        .reason("Consistent daily repetition builds compounding long-term performance.")
                        .recommendationPriority(AiRecommendationPriority.MEDIUM)
                        .permissionLevel(AiPermissionLevel.INFORM)
                        .relatedEntityType(AiRelatedEntityType.HABIT)
                        .relatedEntityId(habit.getId())
                        .dedupeKey(dedupeKey)
                        .isActive(true)
                        .build();
                generated.add(aiRecommendationRepository.save(rec));
            }
        }

        log.info("Refreshed proactive AI recommendations for user {}: created {}", userId, generated.size());
        return getActiveRecommendations(userId);
    }

    @Transactional(readOnly = true)
    public List<AiRecommendationResponse> getActiveRecommendations(UUID userId) {
        return aiRecommendationRepository.findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId).stream()
                .map(AiRecommendationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AiRecommendationResponse recordFeedback(UUID userId, UUID recId, AiFeedbackRequest request) {
        AiRecommendationEntity entity = aiRecommendationRepository.findByIdAndUserId(recId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found: " + recId));

        entity.setFeedbackType(request.getFeedbackType());
        entity.setUserNote(request.getUserNote());
        entity.setRespondedAt(Instant.now());
        entity.setActive(false);

        AiRecommendationEntity updated = aiRecommendationRepository.save(entity);
        log.info("Recorded feedback {} for recommendation {} by user {}", request.getFeedbackType(), recId, userId);
        return AiRecommendationResponse.fromEntity(updated);
    }
}
