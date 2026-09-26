package com.livo.api.modules.goal.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.GoalProgressHistoryResponse;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.dto.MilestoneResponse;
import com.livo.api.modules.goal.dto.UpdateGoalProgressRequest;
import com.livo.api.modules.goal.dto.UpdateGoalRequest;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.GoalProgressHistoryEntity;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import com.livo.api.modules.goal.repository.GoalProgressHistoryRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final GoalProgressHistoryRepository goalProgressHistoryRepository;

    @Override
    @Transactional
    public GoalResponse createGoal(UUID userId, CreateGoalRequest request) {
        GoalTrackingType trackingType = request.getProgressTrackingType() != null
                ? request.getProgressTrackingType()
                : GoalTrackingType.PERCENTAGE;

        BigDecimal targetValue = request.getTargetValue();
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target value must be greater than zero");
        }
        if (trackingType == GoalTrackingType.NUMERICAL) {
            if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target value must be greater than zero for numerical goals");
            }
        } else if (trackingType == GoalTrackingType.PERCENTAGE && targetValue == null) {
            targetValue = BigDecimal.valueOf(100.00);
        }

        BigDecimal currentValue = request.getCurrentValue() != null ? request.getCurrentValue() : BigDecimal.ZERO;
        if (currentValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Current value cannot be negative");
        }

        GoalEntity goal = GoalEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .relatedArea(request.getRelatedArea())
                .targetDescription(request.getTargetDescription())
                .category(request.getCategory() != null ? request.getCategory() : "PERSONAL")
                .priority(request.getPriority() != null ? request.getPriority() : GoalPriority.MEDIUM)
                .targetDate(request.getTargetDate())
                .progressTrackingType(trackingType)
                .targetValue(targetValue)
                .currentValue(currentValue)
                .unit(request.getUnit())
                .reminderFrequency(request.getReminderFrequency())
                .status(request.getStatus() != null ? request.getStatus() : GoalStatus.IN_PROGRESS)
                .build();
        goal.setUserId(userId);
        goal.setVersion(1L);

        GoalEntity saved = goalRepository.save(goal);

        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            recordProgressHistory(saved.getId(), userId, BigDecimal.ZERO, currentValue, "Initial goal progress recorded", LocalDate.now());
        }

        log.info("Created goal {} ({}) for user {}", saved.getId(), saved.getTitle(), userId);
        return GoalResponse.fromEntity(saved, new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID userId, UUID goalId) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);
        List<MilestoneResponse> milestones = getMilestoneResponses(userId, goalId);
        return GoalResponse.fromEntity(goal, milestones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getAllGoals(UUID userId, GoalStatus status, String relatedArea) {
        List<GoalEntity> goals;
        if (status != null && relatedArea != null) {
            goals = goalRepository.findAllByUserIdAndStatusAndRelatedAreaAndDeletedAtIsNull(userId, status, relatedArea);
        } else if (status != null) {
            goals = goalRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, status);
        } else if (relatedArea != null) {
            goals = goalRepository.findAllByUserIdAndRelatedAreaAndDeletedAtIsNull(userId, relatedArea);
        } else {
            goals = goalRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        }

        if (goals.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> goalIds = goals.stream().map(GoalEntity::getId).collect(Collectors.toList());

        List<MilestoneEntity> allMilestones = milestoneRepository
                .findAllByGoalIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalIds, userId);

        Map<UUID, List<MilestoneResponse>> milestonesByGoalId = allMilestones.stream()
                .collect(Collectors.groupingBy(
                        MilestoneEntity::getGoalId,
                        Collectors.mapping(MilestoneResponse::fromEntity, Collectors.toList())
                ));

        return goals.stream()
                .map(goal -> GoalResponse.fromEntity(
                        goal,
                        milestonesByGoalId.getOrDefault(goal.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);

        if (request.getTitle() != null) {
            goal.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getRelatedArea() != null) {
            goal.setRelatedArea(request.getRelatedArea());
        }
        if (request.getTargetDescription() != null) {
            goal.setTargetDescription(request.getTargetDescription());
        }
        if (request.getCategory() != null) {
            goal.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            goal.setPriority(request.getPriority());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getProgressTrackingType() != null) {
            goal.setProgressTrackingType(request.getProgressTrackingType());
            if (request.getProgressTrackingType() == GoalTrackingType.NUMERICAL && (goal.getTargetValue() == null || goal.getTargetValue().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new BadRequestException("Target value must be greater than zero for numerical goals");
            }
        }
        if (request.getTargetValue() != null) {
            if (request.getTargetValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target value must be greater than zero");
            }
            goal.setTargetValue(request.getTargetValue());
        }
        if (request.getUnit() != null) {
            goal.setUnit(request.getUnit());
        }
        if (request.getReminderFrequency() != null) {
            goal.setReminderFrequency(request.getReminderFrequency());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        if (request.getCurrentValue() != null) {
            if (request.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Current value cannot be negative");
            }
            if (request.getCurrentValue().compareTo(goal.getCurrentValue()) != 0) {
                BigDecimal previousValue = goal.getCurrentValue();
                goal.setCurrentValue(request.getCurrentValue());
                recordProgressHistory(goalId, userId, previousValue, request.getCurrentValue(), "Progress value updated via goal edit", LocalDate.now());
            }
        }

        GoalEntity saved = goalRepository.save(goal);
        List<MilestoneResponse> milestones = getMilestoneResponses(userId, goalId);
        log.info("Updated goal {} for user {}", goalId, userId);
        return GoalResponse.fromEntity(saved, milestones);
    }

    @Override
    @Transactional
    public GoalResponse updateGoalProgress(UUID userId, UUID goalId, UpdateGoalProgressRequest request) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);

        if (request.getCurrentValue() == null || request.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Current value cannot be negative");
        }

        BigDecimal previousValue = goal.getCurrentValue() != null ? goal.getCurrentValue() : BigDecimal.ZERO;
        BigDecimal newValue = request.getCurrentValue();
        LocalDate recordedDate = request.getRecordedDate() != null ? request.getRecordedDate() : LocalDate.now();

        goal.setCurrentValue(newValue);

        if (goal.getTargetValue() != null && newValue.compareTo(goal.getTargetValue()) >= 0
                && goal.getStatus() == GoalStatus.IN_PROGRESS) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        GoalEntity saved = goalRepository.save(goal);
        recordProgressHistory(goalId, userId, previousValue, newValue, request.getNotes(), recordedDate);

        List<MilestoneResponse> milestones = getMilestoneResponses(userId, goalId);
        log.info("Updated progress on goal {} from {} to {} for user {}", goalId, previousValue, newValue, userId);
        return GoalResponse.fromEntity(saved, milestones);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        GoalEntity goal = findGoalOrThrow(userId, goalId);

        Instant now = Instant.now();
        goal.setDeletedAt(now);
        goalRepository.save(goal);

        List<MilestoneEntity> milestones = milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId, userId);
        for (MilestoneEntity milestone : milestones) {
            milestone.setDeletedAt(now);
            milestoneRepository.save(milestone);
        }

        log.info("Soft-deleted goal {} and its {} milestones for user {}", goalId, milestones.size(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalProgressHistoryResponse> getGoalProgressHistory(UUID userId, UUID goalId) {
        findGoalOrThrow(userId, goalId);
        return goalProgressHistoryRepository.findAllByGoalIdAndUserIdOrderByRecordedDateDescCreatedAtDesc(goalId, userId)
                .stream()
                .map(GoalProgressHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private GoalEntity findGoalOrThrow(UUID userId, UUID goalId) {
        return goalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));
    }

    private List<MilestoneResponse> getMilestoneResponses(UUID userId, UUID goalId) {
        return milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId, userId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void recordProgressHistory(UUID goalId, UUID userId, BigDecimal prev, BigDecimal next, String notes, LocalDate recordedDate) {
        GoalProgressHistoryEntity history = GoalProgressHistoryEntity.builder()
                .goalId(goalId)
                .userId(userId)
                .recordedDate(recordedDate)
                .previousValue(prev)
                .newValue(next)
                .notes(notes)
                .createdAt(Instant.now())
                .build();
        goalProgressHistoryRepository.save(history);
    }
}
