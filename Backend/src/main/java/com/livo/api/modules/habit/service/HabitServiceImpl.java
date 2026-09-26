package com.livo.api.modules.habit.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitHistoryResponse;
import com.livo.api.modules.habit.dto.HabitLogResponse;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.dto.UpdateHabitRequest;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final GoalRepository goalRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public HabitResponse createHabit(UUID userId, CreateHabitRequest request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        if (request.getEndDate() != null && request.getEndDate().isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }

        List<Integer> customDays = resolveCustomDays(request.getFrequencyType(), request.getCustomDays());

        HabitEntity habit = HabitEntity.builder()
                .goalId(request.getGoalId())
                .title(request.getTitle())
                .description(request.getDescription())
                .motivationNote(request.getMotivationNote())
                .iconKey(request.getIconKey() != null ? request.getIconKey() : "WATER")
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#10B981")
                .frequencyType(request.getFrequencyType() != null ? request.getFrequencyType() : HabitFrequency.DAILY)
                .customDays(customDays)
                .targetCount(request.getTargetCount() > 0 ? request.getTargetCount() : 1)
                .targetUnit(request.getTargetUnit() != null ? request.getTargetUnit() : "times")
                .preferredTime(request.getPreferredTime())
                .preferredClockTime(request.getPreferredClockTime())
                .reminderEnabled(request.isReminderEnabled())
                .reminderTime(request.getReminderTime())
                .currentStreak(0)
                .longestStreak(0)
                .startDate(startDate)
                .endDate(request.getEndDate())
                .isArchived(false)
                .build();
        habit.setUserId(userId);
        habit.setVersion(1L);

        HabitEntity saved = habitRepository.save(habit);
        log.info("Created habit {} ({}) for user {}", saved.getId(), saved.getTitle(), userId);
        return HabitResponse.fromEntity(saved, false, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public HabitResponse getHabitById(UUID userId, UUID habitId) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);
        return mapToHabitResponse(habit, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitResponse> getHabits(UUID userId, Boolean archived, UUID goalId) {
        List<HabitEntity> habits;
        if (archived != null && archived) {
            habits = habitRepository.findAllByUserIdAndIsArchivedTrueAndDeletedAtIsNull(userId);
        } else if (archived != null && !archived) {
            habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        } else if (goalId != null) {
            habits = habitRepository.findAllByUserIdAndGoalIdAndDeletedAtIsNull(userId, goalId);
        } else {
            habits = habitRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        }

        return habits.stream()
                .map(habit -> mapToHabitResponse(habit, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HabitResponse updateHabit(UUID userId, UUID habitId, UpdateHabitRequest request) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
            habit.setGoalId(request.getGoalId());
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : habit.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : habit.getEndDate();
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (request.getTitle() != null) {
            habit.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            habit.setDescription(request.getDescription());
        }
        if (request.getMotivationNote() != null) {
            habit.setMotivationNote(request.getMotivationNote());
        }
        if (request.getIconKey() != null) {
            habit.setIconKey(request.getIconKey());
        }
        if (request.getColorHex() != null) {
            habit.setColorHex(request.getColorHex());
        }
        if (request.getFrequencyType() != null) {
            habit.setFrequencyType(request.getFrequencyType());
            habit.setCustomDays(resolveCustomDays(request.getFrequencyType(), request.getCustomDays()));
        } else if (request.getCustomDays() != null) {
            habit.setCustomDays(resolveCustomDays(habit.getFrequencyType(), request.getCustomDays()));
        }
        if (request.getTargetCount() != null) {
            habit.setTargetCount(request.getTargetCount());
        }
        if (request.getTargetUnit() != null) {
            habit.setTargetUnit(request.getTargetUnit());
        }
        if (request.getPreferredTime() != null) {
            habit.setPreferredTime(request.getPreferredTime());
        }
        if (request.getPreferredClockTime() != null) {
            habit.setPreferredClockTime(request.getPreferredClockTime());
        }
        if (request.getReminderEnabled() != null) {
            habit.setReminderEnabled(request.getReminderEnabled());
        }
        if (request.getReminderTime() != null) {
            habit.setReminderTime(request.getReminderTime());
        }
        if (request.getStartDate() != null) {
            habit.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            habit.setEndDate(request.getEndDate());
        }
        if (request.getIsArchived() != null) {
            habit.setArchived(request.getIsArchived());
        }

        HabitEntity updated = habitRepository.save(habit);
        recalculateStreaks(updated, userId);
        return mapToHabitResponse(updated, userId);
    }

    @Override
    @Transactional
    public void deleteHabit(UUID userId, UUID habitId) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);

        Instant now = Instant.now();
        habit.setDeletedAt(now);
        habitRepository.save(habit);

        List<HabitLogEntity> logs = habitLogRepository.findAllByHabitIdAndUserIdAndDeletedAtIsNull(habitId, userId);
        for (HabitLogEntity logEntry : logs) {
            logEntry.setDeletedAt(now);
            habitLogRepository.save(logEntry);
        }

        log.info("Soft-deleted habit {} and its {} logs for user {}", habitId, logs.size(), userId);
    }

    @Override
    @Transactional
    public HabitResponse toggleArchive(UUID userId, UUID habitId) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);
        habit.setArchived(!habit.isArchived());
        HabitEntity updated = habitRepository.save(habit);
        log.info("Toggled archive status to {} for habit {} by user {}", updated.isArchived(), habitId, userId);
        return mapToHabitResponse(updated, userId);
    }

    @Override
    @Transactional
    public HabitResponse logHabit(UUID userId, UUID habitId, LogHabitRequest request) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);
        int previousStreak = habit.getCurrentStreak();
        LocalDate logDate = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();
        int count = request.getCountCompleted() > 0 ? request.getCountCompleted() : 1;

        Optional<HabitLogEntity> existingOpt = habitLogRepository.findByHabitIdAndUserIdAndLogDateAndDeletedAtIsNull(habitId, userId, logDate);
        if (existingOpt.isPresent()) {
            HabitLogEntity existing = existingOpt.get();
            existing.setCountCompleted(existing.getCountCompleted() + count);
            habitLogRepository.save(existing);
        } else {
            HabitLogEntity newLog = HabitLogEntity.builder()
                    .habitId(habitId)
                    .logDate(logDate)
                    .countCompleted(count)
                    .loggedAt(Instant.now())
                    .build();
            newLog.setUserId(userId);
            newLog.setVersion(1L);
            habitLogRepository.save(newLog);
        }

        recalculateStreaks(habit, userId);
        int newStreak = habit.getCurrentStreak();
        boolean streakIncreased = newStreak > previousStreak;

        eventPublisher.publishEvent(new com.livo.api.common.event.HabitCheckedInEvent(
                habit.getId(), userId, habit.getGoalId(), habit.getTitle(), logDate, newStreak, previousStreak, streakIncreased
        ));
        log.info("Logged habit {} for date {} by user {}", habitId, logDate, userId);
        return mapToHabitResponse(habit, userId);
    }

    @Override
    @Transactional
    public HabitResponse unlogHabit(UUID userId, UUID habitId, LocalDate logDate) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);
        LocalDate targetDate = logDate != null ? logDate : LocalDate.now();

        Optional<HabitLogEntity> logOpt = habitLogRepository.findByHabitIdAndUserIdAndLogDateAndDeletedAtIsNull(habitId, userId, targetDate);
        if (logOpt.isPresent()) {
            HabitLogEntity logEntry = logOpt.get();
            logEntry.setDeletedAt(Instant.now());
            habitLogRepository.save(logEntry);
            log.info("Unlogged habit {} for date {} by user {}", habitId, targetDate, userId);
        }

        recalculateStreaks(habit, userId);
        return mapToHabitResponse(habit, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public HabitHistoryResponse getHabitHistory(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        HabitEntity habit = findHabitOrThrow(userId, habitId);

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<HabitLogEntity> logEntities = habitLogRepository.findAllByHabitIdAndUserIdAndLogDateBetweenAndDeletedAtIsNullOrderByLogDateAsc(habitId, userId, start, end);
        List<HabitLogResponse> logs = logEntities.stream()
                .map(HabitLogResponse::fromEntity)
                .collect(Collectors.toList());

        long totalCompletions = habitLogRepository.countByHabitIdAndUserIdAndDeletedAtIsNull(habitId, userId);

        // Calculate completion rate in range
        long scheduledDays = 0;
        long completedDays = 0;
        Set<LocalDate> completedDates = logEntities.stream()
                .filter(l -> l.getCountCompleted() >= habit.getTargetCount())
                .map(HabitLogEntity::getLogDate)
                .collect(Collectors.toSet());

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (isHabitScheduledOn(habit, cursor)) {
                scheduledDays++;
                if (completedDates.contains(cursor)) {
                    completedDays++;
                }
            }
            cursor = cursor.plusDays(1);
        }

        double rate = scheduledDays > 0
                ? Math.round(((double) completedDays / scheduledDays * 100.0) * 100.0) / 100.0
                : 0.0;

        return HabitHistoryResponse.builder()
                .habitId(habitId)
                .startDate(start)
                .endDate(end)
                .currentStreak(habit.getCurrentStreak())
                .longestStreak(habit.getLongestStreak())
                .totalCompletions(totalCompletions)
                .completionRate(rate)
                .logs(logs)
                .build();
    }

    private HabitEntity findHabitOrThrow(UUID userId, UUID habitId) {
        return habitRepository.findByIdAndUserIdAndDeletedAtIsNull(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found: " + habitId));
    }

    private HabitResponse mapToHabitResponse(HabitEntity habit, UUID userId) {
        LocalDate today = LocalDate.now();
        Optional<HabitLogEntity> todayLog = habitLogRepository.findByHabitIdAndUserIdAndLogDateAndDeletedAtIsNull(habit.getId(), userId, today);
        boolean completedToday = todayLog.map(l -> l.getCountCompleted() >= habit.getTargetCount()).orElse(false);
        int todayCount = todayLog.map(HabitLogEntity::getCountCompleted).orElse(0);
        long totalCompletions = habitLogRepository.countByHabitIdAndUserIdAndDeletedAtIsNull(habit.getId(), userId);

        return HabitResponse.fromEntity(habit, completedToday, todayCount, totalCompletions);
    }

    private void recalculateStreaks(HabitEntity habit, UUID userId) {
        List<HabitLogEntity> logs = habitLogRepository.findAllByHabitIdAndUserIdAndDeletedAtIsNullOrderByLogDateDesc(habit.getId(), userId);
        Set<LocalDate> completedDates = logs.stream()
                .filter(l -> l.getCountCompleted() >= habit.getTargetCount())
                .map(HabitLogEntity::getLogDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        if (isHabitScheduledOn(habit, today)) {
            if (!completedDates.contains(today)) {
                checkDate = today.minusDays(1);
            }
        } else {
            checkDate = today.minusDays(1);
        }

        int currentStreak = 0;
        while (checkDate != null && !checkDate.isBefore(habit.getStartDate())) {
            if (isHabitScheduledOn(habit, checkDate)) {
                if (completedDates.contains(checkDate)) {
                    currentStreak++;
                } else {
                    break;
                }
            }
            checkDate = checkDate.minusDays(1);
        }

        habit.setCurrentStreak(currentStreak);
        if (currentStreak > habit.getLongestStreak()) {
            habit.setLongestStreak(currentStreak);
        }
        habitRepository.save(habit);
    }

    private boolean isHabitScheduledOn(HabitEntity habit, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon .. 7=Sun
        switch (habit.getFrequencyType()) {
            case WEEKDAYS:
                return dayOfWeek >= 1 && dayOfWeek <= 5;
            case WEEKLY:
            case SPECIFIC_DAYS:
            case CUSTOM:
                return habit.getCustomDays() != null && habit.getCustomDays().contains(dayOfWeek);
            case DAILY:
            default:
                return true;
        }
    }

    private List<Integer> resolveCustomDays(HabitFrequency frequency, List<Integer> requestedDays) {
        if (frequency == null) {
            return new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));
        }
        switch (frequency) {
            case WEEKDAYS:
                return new ArrayList<>(List.of(1, 2, 3, 4, 5));
            case DAILY:
                return new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));
            case SPECIFIC_DAYS:
            case CUSTOM:
            case WEEKLY:
                if (requestedDays != null && !requestedDays.isEmpty()) {
                    List<Integer> validDays = requestedDays.stream()
                            .filter(d -> d != null && d >= 1 && d <= 7)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    if (!validDays.isEmpty()) {
                        return validDays;
                    }
                }
                return new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));
            default:
                return new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7));
        }
    }
}
