package com.livo.api.modules.goal.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.MilestoneResponse;
import com.livo.api.modules.goal.dto.ReorderMilestonesRequest;
import com.livo.api.modules.goal.dto.UpdateMilestoneRequest;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    @Transactional
    public MilestoneResponse addMilestone(UUID userId, UUID goalId, CreateMilestoneRequest request) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);

        int sortOrder = request.getSortOrder() != null
                ? request.getSortOrder()
                : (int) (milestoneRepository.countByGoalIdAndUserIdAndDeletedAtIsNull(goalId, userId) + 1);

        MilestoneEntity milestone = MilestoneEntity.builder()
                .goalId(goalId)
                .title(request.getTitle())
                .targetDate(request.getTargetDate())
                .sortOrder(sortOrder)
                .isAiGenerated(request.isAiGenerated())
                .isCompleted(false)
                .build();
        milestone.setUserId(userId);
        milestone.setVersion(1L);

        MilestoneEntity saved = milestoneRepository.save(milestone);
        syncGoalMilestoneProgress(goal, userId);

        log.info("Created milestone {} for goal {} by user {}", saved.getId(), goalId, userId);
        return MilestoneResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByGoal(UUID userId, UUID goalId) {
        findGoalOrThrow(userId, goalId);
        return milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId, userId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MilestoneResponse updateMilestone(UUID userId, UUID goalId, UUID milestoneId, UpdateMilestoneRequest request) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);
        MilestoneEntity milestone = findMilestoneOrThrow(userId, goalId, milestoneId);

        if (request.getTitle() != null) {
            milestone.setTitle(request.getTitle());
        }
        if (request.getTargetDate() != null) {
            milestone.setTargetDate(request.getTargetDate());
        }
        if (request.getIsCompleted() != null) {
            milestone.setCompleted(request.getIsCompleted());
        }
        if (request.getSortOrder() != null) {
            milestone.setSortOrder(request.getSortOrder());
        }

        MilestoneEntity updated = milestoneRepository.save(milestone);
        syncGoalMilestoneProgress(goal, userId);

        log.info("Updated milestone {} for goal {} by user {}", milestoneId, goalId, userId);
        return MilestoneResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public MilestoneResponse toggleMilestoneCompletion(UUID userId, UUID goalId, UUID milestoneId) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);
        MilestoneEntity milestone = findMilestoneOrThrow(userId, goalId, milestoneId);

        milestone.setCompleted(!milestone.isCompleted());
        MilestoneEntity updated = milestoneRepository.save(milestone);
        syncGoalMilestoneProgress(goal, userId);

        log.info("Toggled milestone {} completion to {} by user {}", milestoneId, updated.isCompleted(), userId);
        return MilestoneResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public List<MilestoneResponse> reorderMilestones(UUID userId, UUID goalId, ReorderMilestonesRequest request) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);
        List<UUID> orderedIds = request.getMilestoneIds();
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BadRequestException("Milestone IDs list cannot be empty");
        }

        List<MilestoneEntity> milestones = milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId, userId);

        if (orderedIds.size() != milestones.size()) {
            throw new BadRequestException(String.format(
                    "Reorder list must contain all %d milestones for goal %s, but received %d",
                    milestones.size(), goalId, orderedIds.size()
            ));
        }

        Set<UUID> uniqueOrderedIds = new HashSet<>(orderedIds);
        if (uniqueOrderedIds.size() != orderedIds.size()) {
            throw new BadRequestException("Reorder list contains duplicate milestone IDs");
        }

        Set<UUID> existingIds = milestones.stream().map(MilestoneEntity::getId).collect(Collectors.toSet());
        if (!uniqueOrderedIds.equals(existingIds)) {
            throw new BadRequestException("Reorder list must contain a complete permutation of all existing active milestones for goal " + goalId);
        }

        Map<UUID, MilestoneEntity> milestoneMap = milestones.stream()
                .collect(Collectors.toMap(MilestoneEntity::getId, Function.identity()));

        int currentOrder = 1;
        for (UUID id : orderedIds) {
            MilestoneEntity m = milestoneMap.get(id);
            m.setSortOrder(currentOrder++);
        }

        List<MilestoneEntity> updatedMilestones = milestoneRepository.saveAll(milestones);

        return updatedMilestones.stream()
                .sorted(Comparator.comparingInt(MilestoneEntity::getSortOrder))
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMilestone(UUID userId, UUID goalId, UUID milestoneId) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);
        MilestoneEntity milestone = findMilestoneOrThrow(userId, goalId, milestoneId);

        milestone.setDeletedAt(Instant.now());
        milestoneRepository.save(milestone);
        syncGoalMilestoneProgress(goal, userId);

        log.info("Soft-deleted milestone {} from goal {} by user {}", milestoneId, goalId, userId);
    }

    private GoalEntity findGoalOrThrow(UUID userId, UUID goalId) {
        return goalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));
    }

    private MilestoneEntity findMilestoneOrThrow(UUID userId, UUID goalId, UUID milestoneId) {
        return milestoneRepository.findByIdAndGoalIdAndUserIdAndDeletedAtIsNull(milestoneId, goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + milestoneId));
    }

    private void syncGoalMilestoneProgress(GoalEntity goal, UUID userId) {
        if (goal.getProgressTrackingType() == GoalTrackingType.MILESTONE_BASED) {
            long total = milestoneRepository.countByGoalIdAndUserIdAndDeletedAtIsNull(goal.getId(), userId);
            long completed = milestoneRepository.countByGoalIdAndUserIdAndIsCompletedTrueAndDeletedAtIsNull(goal.getId(), userId);
            if (total > 0) {
                BigDecimal progress = BigDecimal.valueOf((double) completed / total * 100.0).setScale(2, RoundingMode.HALF_UP);
                goal.setCurrentValue(progress);
                if (completed == total && goal.getStatus() == GoalStatus.IN_PROGRESS) {
                    goal.setStatus(GoalStatus.COMPLETED);
                } else if (completed < total && goal.getStatus() == GoalStatus.COMPLETED) {
                    goal.setStatus(GoalStatus.IN_PROGRESS);
                }
            } else {
                goal.setCurrentValue(BigDecimal.ZERO);
            }
            goalRepository.save(goal);
        }
    }
}
