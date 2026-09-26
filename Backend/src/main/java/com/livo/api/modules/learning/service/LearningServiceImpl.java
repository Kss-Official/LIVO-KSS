package com.livo.api.modules.learning.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.CreateLearningResourceRequest;
import com.livo.api.modules.learning.dto.LearningItemResponse;
import com.livo.api.modules.learning.dto.LearningResourceResponse;
import com.livo.api.modules.learning.dto.LearningSessionResponse;
import com.livo.api.modules.learning.dto.LearningStatsResponse;
import com.livo.api.modules.learning.dto.LogLearningSessionRequest;
import com.livo.api.modules.learning.dto.UpdateLearningItemRequest;
import com.livo.api.modules.learning.dto.UpdateLearningResourceRequest;
import com.livo.api.modules.learning.entity.LearningItemEntity;
import com.livo.api.modules.learning.entity.LearningResourceEntity;
import com.livo.api.modules.learning.entity.LearningSessionEntity;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningResourceType;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.entity.enums.StudyFrequency;
import com.livo.api.modules.learning.repository.LearningItemRepository;
import com.livo.api.modules.learning.repository.LearningResourceRepository;
import com.livo.api.modules.learning.repository.LearningSessionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final LearningItemRepository learningItemRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final GoalRepository goalRepository;

    @Override
    @Transactional
    public LearningItemResponse createLearningItem(UUID userId, CreateLearningItemRequest request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        if (request.getTargetCompletionDate() != null && request.getTargetCompletionDate().isBefore(startDate)) {
            throw new BadRequestException("Target completion date cannot be before start date");
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }

        LearningItemEntity item = LearningItemEntity.builder()
                .goalId(request.getGoalId())
                .title(request.getTitle())
                .description(request.getDescription())
                .objective(request.getObjective())
                .notes(request.getNotes())
                .learningType(request.getLearningType() != null ? request.getLearningType() : LearningType.COURSE)
                .category(request.getCategory() != null ? request.getCategory().toUpperCase() : "TECH")
                .difficultyLevel(request.getDifficultyLevel() != null ? request.getDifficultyLevel() : DifficultyLevel.BEGINNER)
                .targetStudyTimeMinutes(request.getTargetStudyTimeMinutes() > 0 ? request.getTargetStudyTimeMinutes() : 30)
                .studyFrequency(request.getStudyFrequency() != null ? request.getStudyFrequency() : StudyFrequency.DAILY)
                .progressPercentage(request.getProgressPercentage())
                .status(request.getStatus() != null ? request.getStatus() : LearningStatus.IN_PROGRESS)
                .startDate(startDate)
                .targetCompletionDate(request.getTargetCompletionDate())
                .build();
        item.setUserId(userId);
        item.setVersion(1L);

        LearningItemEntity saved = learningItemRepository.save(item);
        log.info("Created learning item {} ({}) for user {}", saved.getId(), saved.getTitle(), userId);
        return LearningItemResponse.fromEntity(saved, 0, new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public LearningItemResponse getLearningItemById(UUID userId, UUID itemId) {
        LearningItemEntity item = findItemOrThrow(userId, itemId);
        return mapToResponse(item, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningItemResponse> getLearningItems(UUID userId, LearningStatus status, String category, UUID goalId) {
        List<LearningItemEntity> filtered;

        if (status == null && (category == null || category.isBlank()) && goalId == null) {
            filtered = learningItemRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        } else {
            Specification<LearningItemEntity> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("userId"), userId));
                predicates.add(cb.isNull(root.get("deletedAt")));

                if (status != null) {
                    predicates.add(cb.equal(root.get("status"), status));
                }

                if (category != null && !category.isBlank()) {
                    predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
                }

                if (goalId != null) {
                    predicates.add(cb.equal(root.get("goalId"), goalId));
                }

                query.orderBy(cb.desc(root.get("createdAt")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };

            filtered = learningItemRepository.findAll(spec);
        }

        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> itemIds = filtered.stream().map(LearningItemEntity::getId).collect(Collectors.toList());

        List<LearningResourceEntity> allResources = learningResourceRepository
                .findAllByLearningItemIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(itemIds, userId);

        Map<UUID, List<LearningResourceResponse>> resourcesByItemId = allResources.stream()
                .collect(Collectors.groupingBy(
                        LearningResourceEntity::getLearningItemId,
                        Collectors.mapping(LearningResourceResponse::fromEntity, Collectors.toList())
                ));

        List<LearningSessionEntity> allSessions = learningSessionRepository
                .findAllByLearningItemIdInAndUserIdAndDeletedAtIsNull(itemIds, userId);

        Map<UUID, Integer> studyMinutesByItemId = allSessions.stream()
                .collect(Collectors.groupingBy(
                        LearningSessionEntity::getLearningItemId,
                        Collectors.summingInt(LearningSessionEntity::getDurationMinutes)
                ));

        return filtered.stream()
                .map(i -> LearningItemResponse.fromEntity(
                        i,
                        studyMinutesByItemId.getOrDefault(i.getId(), 0),
                        resourcesByItemId.getOrDefault(i.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LearningItemResponse updateLearningItem(UUID userId, UUID itemId, UpdateLearningItemRequest request) {
        LearningItemEntity item = findItemOrThrow(userId, itemId);

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
            item.setGoalId(request.getGoalId());
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : item.getStartDate();
        LocalDate targetDate = request.getTargetCompletionDate() != null ? request.getTargetCompletionDate() : item.getTargetCompletionDate();
        if (targetDate != null && targetDate.isBefore(startDate)) {
            throw new BadRequestException("Target completion date cannot be before start date");
        }

        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getObjective() != null) {
            item.setObjective(request.getObjective());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        if (request.getLearningType() != null) {
            item.setLearningType(request.getLearningType());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory().toUpperCase());
        }
        if (request.getDifficultyLevel() != null) {
            item.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTargetStudyTimeMinutes() != null) {
            item.setTargetStudyTimeMinutes(request.getTargetStudyTimeMinutes());
        }
        if (request.getStudyFrequency() != null) {
            item.setStudyFrequency(request.getStudyFrequency());
        }
        if (request.getProgressPercentage() != null) {
            item.setProgressPercentage(request.getProgressPercentage());
            if (request.getProgressPercentage() == 100 && item.getStatus() == LearningStatus.IN_PROGRESS) {
                item.setStatus(LearningStatus.COMPLETED);
            }
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            item.setStartDate(request.getStartDate());
        }
        if (request.getTargetCompletionDate() != null) {
            item.setTargetCompletionDate(request.getTargetCompletionDate());
        }

        LearningItemEntity updated = learningItemRepository.save(item);
        log.info("Updated learning item {} for user {}", itemId, userId);
        return mapToResponse(updated, userId);
    }

    @Override
    @Transactional
    public void deleteLearningItem(UUID userId, UUID itemId) {
        LearningItemEntity item = findItemOrThrow(userId, itemId);
        Instant now = Instant.now();
        item.setDeletedAt(now);
        learningItemRepository.save(item);

        List<LearningResourceEntity> resources = learningResourceRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(itemId, userId);
        for (LearningResourceEntity res : resources) {
            res.setDeletedAt(now);
            learningResourceRepository.save(res);
        }

        List<LearningSessionEntity> sessions = learningSessionRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySessionDateDesc(itemId, userId);
        for (LearningSessionEntity session : sessions) {
            session.setDeletedAt(now);
            learningSessionRepository.save(session);
        }

        log.info("Soft-deleted learning item {} and cascade elements for user {}", itemId, userId);
    }

    @Override
    @Transactional
    public LearningResourceResponse addResource(UUID userId, UUID itemId, CreateLearningResourceRequest request) {
        findItemOrThrow(userId, itemId);

        int sortOrder = request.getSortOrder() != null
                ? request.getSortOrder()
                : (int) (learningResourceRepository.countByLearningItemIdAndUserIdAndDeletedAtIsNull(itemId, userId) + 1);

        LearningResourceEntity resource = LearningResourceEntity.builder()
                .learningItemId(itemId)
                .resourceType(request.getResourceType() != null ? request.getResourceType() : LearningResourceType.WEBSITE)
                .title(request.getTitle())
                .url(request.getUrl())
                .notes(request.getNotes())
                .isCompleted(false)
                .sortOrder(sortOrder)
                .build();
        resource.setUserId(userId);
        resource.setVersion(1L);

        LearningResourceEntity saved = learningResourceRepository.save(resource);
        log.info("Added resource {} to learning item {} by user {}", saved.getId(), itemId, userId);
        return LearningResourceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningResourceResponse> getResources(UUID userId, UUID itemId) {
        findItemOrThrow(userId, itemId);
        return learningResourceRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(itemId, userId)
                .stream()
                .map(LearningResourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LearningResourceResponse updateResource(UUID userId, UUID itemId, UUID resourceId, UpdateLearningResourceRequest request) {
        findItemOrThrow(userId, itemId);
        LearningResourceEntity resource = findResourceOrThrow(userId, itemId, resourceId);

        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }
        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            resource.setUrl(request.getUrl());
        }
        if (request.getNotes() != null) {
            resource.setNotes(request.getNotes());
        }
        if (request.getIsCompleted() != null) {
            resource.setCompleted(request.getIsCompleted());
        }
        if (request.getSortOrder() != null) {
            resource.setSortOrder(request.getSortOrder());
        }

        LearningResourceEntity saved = learningResourceRepository.save(resource);
        return LearningResourceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public LearningResourceResponse toggleResourceCompletion(UUID userId, UUID itemId, UUID resourceId) {
        findItemOrThrow(userId, itemId);
        LearningResourceEntity resource = findResourceOrThrow(userId, itemId, resourceId);

        resource.setCompleted(!resource.isCompleted());
        LearningResourceEntity saved = learningResourceRepository.save(resource);
        return LearningResourceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteResource(UUID userId, UUID itemId, UUID resourceId) {
        findItemOrThrow(userId, itemId);
        LearningResourceEntity resource = findResourceOrThrow(userId, itemId, resourceId);

        resource.setDeletedAt(Instant.now());
        learningResourceRepository.save(resource);
        log.info("Soft-deleted resource {} from item {} by user {}", resourceId, itemId, userId);
    }

    @Override
    @Transactional
    public LearningSessionResponse logSession(UUID userId, LogLearningSessionRequest request) {
        findItemOrThrow(userId, request.getLearningItemId());

        LocalDate sessionDate = request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now();

        LearningSessionEntity session = LearningSessionEntity.builder()
                .learningItemId(request.getLearningItemId())
                .sessionDate(sessionDate)
                .durationMinutes(request.getDurationMinutes())
                .notes(request.getNotes())
                .build();
        session.setUserId(userId);
        session.setVersion(1L);

        LearningSessionEntity saved = learningSessionRepository.save(session);
        log.info("Logged study session {} ({} mins) for item {} by user {}", saved.getId(), saved.getDurationMinutes(), request.getLearningItemId(), userId);
        return LearningSessionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningSessionResponse> getSessions(UUID userId, UUID itemId, LocalDate startDate, LocalDate endDate) {
        List<LearningSessionEntity> sessions;
        if (itemId != null) {
            sessions = learningSessionRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySessionDateDesc(itemId, userId);
        } else if (startDate != null && endDate != null) {
            sessions = learningSessionRepository.findAllByUserIdAndSessionDateBetweenAndDeletedAtIsNullOrderBySessionDateDesc(userId, startDate, endDate);
        } else {
            sessions = learningSessionRepository.findAllByUserIdAndDeletedAtIsNullOrderBySessionDateDesc(userId);
        }

        return sessions.stream()
                .filter(s -> startDate == null || !s.getSessionDate().isBefore(startDate))
                .filter(s -> endDate == null || !s.getSessionDate().isAfter(endDate))
                .map(LearningSessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        LearningSessionEntity session = learningSessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session not found: " + sessionId));

        session.setDeletedAt(Instant.now());
        learningSessionRepository.save(session);
        log.info("Soft-deleted learning session {} for user {}", sessionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningStatsResponse getLearningStats(UUID userId) {
        List<LearningItemEntity> items = learningItemRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        long totalItems = items.size();
        long completedItems = items.stream().filter(i -> i.getStatus() == LearningStatus.COMPLETED).count();
        long inProgressItems = items.stream().filter(i -> i.getStatus() == LearningStatus.IN_PROGRESS).count();
        long savedItems = items.stream().filter(i -> i.getStatus() == LearningStatus.SAVED).count();

        List<LearningSessionEntity> sessions = learningSessionRepository.findAllByUserIdAndDeletedAtIsNullOrderBySessionDateDesc(userId);
        int totalMinutes = sessions.stream().mapToInt(LearningSessionEntity::getDurationMinutes).sum();
        double totalHours = BigDecimal.valueOf(totalMinutes / 60.0).setScale(2, RoundingMode.HALF_UP).doubleValue();

        Map<UUID, String> itemCategoryMap = items.stream()
                .collect(Collectors.toMap(LearningItemEntity::getId, LearningItemEntity::getCategory, (k1, k2) -> k1));

        Map<String, Integer> studyMinutesByCategory = new HashMap<>();
        for (LearningSessionEntity s : sessions) {
            String cat = itemCategoryMap.getOrDefault(s.getLearningItemId(), "OTHER");
            studyMinutesByCategory.put(cat, studyMinutesByCategory.getOrDefault(cat, 0) + s.getDurationMinutes());
        }

        return LearningStatsResponse.builder()
                .totalItems(totalItems)
                .completedItems(completedItems)
                .inProgressItems(inProgressItems)
                .savedItems(savedItems)
                .totalStudyMinutes(totalMinutes)
                .totalStudyHours(totalHours)
                .totalSessions(sessions.size())
                .studyMinutesByCategory(studyMinutesByCategory)
                .build();
    }

    private LearningItemEntity findItemOrThrow(UUID userId, UUID itemId) {
        return learningItemRepository.findByIdAndUserIdAndDeletedAtIsNull(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning item not found: " + itemId));
    }

    private LearningResourceEntity findResourceOrThrow(UUID userId, UUID itemId, UUID resourceId) {
        return learningResourceRepository.findByIdAndLearningItemIdAndUserIdAndDeletedAtIsNull(resourceId, itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning resource not found: " + resourceId));
    }

    private LearningItemResponse mapToResponse(LearningItemEntity item, UUID userId) {
        List<LearningResourceEntity> resources = learningResourceRepository
                .findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(item.getId(), userId);
        List<LearningResourceResponse> resourceResponses = resources.stream()
                .map(LearningResourceResponse::fromEntity)
                .collect(Collectors.toList());

        List<LearningSessionEntity> sessions = learningSessionRepository
                .findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySessionDateDesc(item.getId(), userId);
        int totalMinutes = sessions.stream().mapToInt(LearningSessionEntity::getDurationMinutes).sum();

        return LearningItemResponse.fromEntity(item, totalMinutes, resourceResponses);
    }
}
