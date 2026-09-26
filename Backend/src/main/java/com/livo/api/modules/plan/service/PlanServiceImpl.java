package com.livo.api.modules.plan.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.common.exception.ScheduleConflictException;
import com.livo.api.engines.conflict.ConflictDetector;
import com.livo.api.engines.conflict.SlotSuggestionEngine;
import com.livo.api.engines.overload.DayRebalanceEngine;
import com.livo.api.engines.overload.WorkloadEvaluator;
import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.plan.dto.ConflictCheckRequest;
import com.livo.api.modules.plan.dto.ConflictCheckResponse;
import com.livo.api.modules.plan.dto.ConflictResolutionRequest;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.DailyPlanResponse;
import com.livo.api.modules.plan.dto.ScheduleBlockResponse;
import com.livo.api.modules.plan.dto.UpdateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.WeekSummaryResponse;
import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import com.livo.api.modules.plan.repository.ScheduleBlockRepository;
import com.livo.api.modules.routine.dto.RoutineResponse;
import com.livo.api.modules.routine.repository.RoutineRepository;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final ScheduleBlockRepository scheduleBlockRepository;
    private final EventRepository eventRepository;
    private final RoutineRepository routineRepository;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private final ConflictDetector conflictDetector;
    private final SlotSuggestionEngine slotSuggestionEngine;
    private final WorkloadEvaluator workloadEvaluator;
    private final DayRebalanceEngine dayRebalanceEngine;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleBlockResponse> getScheduleBlocks(UUID userId, LocalDate date, LocalDate startDate, LocalDate endDate) {
        List<ScheduleBlockEntity> list;
        if (date != null) {
            list = scheduleBlockRepository.findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, date);
        } else if (startDate != null && endDate != null) {
            list = scheduleBlockRepository.findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(userId, startDate, endDate);
        } else {
            list = scheduleBlockRepository.findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, LocalDate.now());
        }

        return list.stream()
                .map(ScheduleBlockResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleBlockResponse getScheduleBlockById(UUID userId, UUID blockId) {
        ScheduleBlockEntity block = findBlockOrThrow(userId, blockId);
        return ScheduleBlockResponse.fromEntity(block);
    }

    @Override
    @Transactional
    public ScheduleBlockResponse createScheduleBlock(UUID userId, CreateScheduleBlockRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        if (!request.isAllowConflict()) {
            checkScheduleConflicts(userId, request.getBlockDate(), request.getStartTime(), request.getEndTime(), null);
        }

        ScheduleBlockEntity block = ScheduleBlockEntity.builder()
                .taskId(request.getTaskId())
                .eventId(request.getEventId())
                .habitId(request.getHabitId())
                .blockDate(request.getBlockDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .title(request.getTitle().trim())
                .category(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory().trim() : "PERSONAL")
                .isLocked(request.isLocked())
                .build();
        block.setUserId(userId);
        block.setVersion(1L);

        return ScheduleBlockResponse.fromEntity(scheduleBlockRepository.save(block));
    }

    @Override
    @Transactional
    public ScheduleBlockResponse updateScheduleBlock(UUID userId, UUID blockId, UpdateScheduleBlockRequest request) {
        ScheduleBlockEntity block = findBlockOrThrow(userId, blockId);

        LocalDate targetDate = request.getBlockDate() != null ? request.getBlockDate() : block.getBlockDate();
        LocalTime newStart = request.getStartTime() != null ? request.getStartTime() : block.getStartTime();
        LocalTime newEnd = request.getEndTime() != null ? request.getEndTime() : block.getEndTime();
        validateTimes(newStart, newEnd);
        if (!request.isAllowConflict()) {
            checkScheduleConflicts(userId, targetDate, newStart, newEnd, blockId);
        }

        if (request.getTaskId() != null) block.setTaskId(request.getTaskId());
        if (request.getEventId() != null) block.setEventId(request.getEventId());
        if (request.getHabitId() != null) block.setHabitId(request.getHabitId());
        if (request.getBlockDate() != null) block.setBlockDate(request.getBlockDate());
        if (request.getTitle() != null && !request.getTitle().isBlank()) block.setTitle(request.getTitle().trim());
        if (request.getCategory() != null && !request.getCategory().isBlank()) block.setCategory(request.getCategory().trim());
        if (request.getIsLocked() != null) block.setLocked(request.getIsLocked());

        block.setStartTime(newStart);
        block.setEndTime(newEnd);

        return ScheduleBlockResponse.fromEntity(scheduleBlockRepository.save(block));
    }

    @Override
    @Transactional
    public void deleteScheduleBlock(UUID userId, UUID blockId) {
        ScheduleBlockEntity block = findBlockOrThrow(userId, blockId);
        block.markDeleted();
        scheduleBlockRepository.save(block);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyPlanResponse getDailyPlan(UUID userId, LocalDate date) {
        LocalDate planDate = (date != null) ? date : LocalDate.now();

        // 1. Schedule blocks for day
        List<ScheduleBlockResponse> blocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, planDate)
                .stream()
                .map(ScheduleBlockResponse::fromEntity)
                .collect(Collectors.toList());

        // 2. Events for day
        Instant dayStart = planDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = planDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusMillis(1);
        List<EventResponse> events = eventRepository
                .findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(userId, dayStart, dayEnd)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());

        // 3. Routines active for this day of week (1=Mon..7=Sun)
        int dayOfWeek = planDate.getDayOfWeek().getValue();
        List<RoutineResponse> routines = routineRepository
                .findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                .stream()
                .filter(r -> r.getDaysOfWeek() != null && r.getDaysOfWeek().contains(dayOfWeek))
                .map(RoutineResponse::fromEntity)
                .collect(Collectors.toList());

        // 4. Tasks scheduled/due for this day
        List<TaskResponse> tasks = taskService.getTasks(userId, null, planDate, null, null, null, null, null);

        // 5. Workload math
        long blockMinutes = 0;
        for (ScheduleBlockResponse b : blocks) {
            if (b.getEndTime().isAfter(b.getStartTime())) {
                blockMinutes += Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
            }
        }

        long taskMinutes = tasks.stream()
                .mapToLong(t -> t.getDurationMins() != null ? t.getDurationMins() : 30)
                .sum();

        BigDecimal totalHours = BigDecimal.valueOf(blockMinutes + taskMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal maxPlannedHours = userPreferenceRepository.findByUserId(userId)
                .map(UserPreferenceEntity::getMaxPlannedHoursPerDay)
                .orElse(new BigDecimal("8.00"));

        boolean isOverloaded = totalHours.compareTo(maxPlannedHours) > 0;

        return DailyPlanResponse.builder()
                .date(planDate)
                .scheduleBlocks(blocks)
                .events(events)
                .routines(routines)
                .tasks(tasks)
                .plannedWorkloadHours(totalHours)
                .maxPlannedHours(maxPlannedHours)
                .isOverloaded(isOverloaded)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WeekSummaryResponse getWeekSummary(UUID userId, LocalDate startDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        List<WeekSummaryResponse.DayCapacityItem> dayItems = new ArrayList<>();
        double totalCapacity = 0.0;

        UserPreferenceEntity prefs = userPreferenceRepository.findByUserId(userId).orElse(null);
        LocalTime wakeTime = prefs != null ? prefs.getDailyWakeTime() : LocalTime.of(7, 0);
        LocalTime sleepTime = prefs != null ? prefs.getDailySleepTime() : LocalTime.of(23, 0);
        BigDecimal maxPlannedHours = prefs != null ? prefs.getMaxPlannedHoursPerDay() : new BigDecimal("8.00");

        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            DailyPlanResponse dailyPlan = getDailyPlan(userId, d);

            long routineMinutes = dailyPlan.getRoutines().stream()
                    .mapToLong(r -> {
                        if (r.getEndTime() != null && r.getStartTime() != null && r.getEndTime().isAfter(r.getStartTime())) {
                            return Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
                        }
                        return 60;
                    }).sum();

            long plannedMinutes = dailyPlan.getPlannedWorkloadHours()
                    .multiply(BigDecimal.valueOf(60)).longValue();

            WorkloadEvaluator.WorkloadAssessment assessment = workloadEvaluator.evaluate(
                    wakeTime, sleepTime, routineMinutes, plannedMinutes, maxPlannedHours
            );

            dayItems.add(WeekSummaryResponse.DayCapacityItem.builder()
                    .date(d)
                    .dayOfWeek(d.getDayOfWeek().name().substring(0, 3))
                    .plannedHours(assessment.getPlannedHours())
                    .availableHours(assessment.getAvailableHours())
                    .capacityPercentage(assessment.getCapacityPercentage())
                    .capacityLabel(assessment.getLabel())
                    .taskCount(dailyPlan.getTasks().size())
                    .eventCount(dailyPlan.getEvents().size())
                    .isOverloaded(assessment.isOverloaded())
                    .build());

            totalCapacity += assessment.getCapacityPercentage();
        }

        return WeekSummaryResponse.builder()
                .startDate(start)
                .endDate(start.plusDays(6))
                .days(dayItems)
                .averageCapacityPercentage(Math.round((totalCapacity / 7.0) * 10.0) / 10.0)
                .build();
    }

    @Override
    @Transactional
    public DailyPlanResponse autoPlanMyDay(UUID userId, LocalDate date) {
        LocalDate planDate = (date != null) ? date : LocalDate.now();
        DailyPlanResponse currentPlan = getDailyPlan(userId, planDate);

        List<TaskResponse> unscheduled = currentPlan.getTasks().stream()
                .filter(t -> t.getDueTime() == null)
                .collect(Collectors.toList());

        if (unscheduled.isEmpty()) {
            return currentPlan;
        }

        UserPreferenceEntity prefs = userPreferenceRepository.findByUserId(userId).orElse(null);
        LocalTime wakeTime = prefs != null ? prefs.getDailyWakeTime() : LocalTime.of(7, 0);
        LocalTime sleepTime = prefs != null ? prefs.getDailySleepTime() : LocalTime.of(23, 0);

        List<ConflictDetector.TimeInterval> occupied = new ArrayList<>();
        for (ScheduleBlockResponse b : currentPlan.getScheduleBlocks()) {
            occupied.add(ConflictDetector.TimeInterval.builder()
                    .id(b.getId()).title(b.getTitle()).startTime(b.getStartTime()).endTime(b.getEndTime()).type("BLOCK").build());
        }
        for (EventResponse e : currentPlan.getEvents()) {
            if (e.getStartTime() != null && e.getEndTime() != null) {
                occupied.add(ConflictDetector.TimeInterval.builder()
                        .id(e.getId()).title(e.getTitle())
                        .startTime(e.getStartTime().atZone(ZoneOffset.UTC).toLocalTime())
                        .endTime(e.getEndTime().atZone(ZoneOffset.UTC).toLocalTime())
                        .type("EVENT").build());
            }
        }
        for (RoutineResponse r : currentPlan.getRoutines()) {
            occupied.add(ConflictDetector.TimeInterval.builder()
                    .id(r.getId()).title(r.getTitle()).startTime(r.getStartTime()).endTime(r.getEndTime()).type("ROUTINE").build());
        }

        for (TaskResponse task : unscheduled) {
            int duration = (task.getDurationMins() != null && task.getDurationMins() > 0) ? task.getDurationMins() : 30;
            List<SlotSuggestionEngine.FreeSlot> slots = slotSuggestionEngine.findAvailableSlots(wakeTime, sleepTime, occupied, duration);
            if (!slots.isEmpty()) {
                SlotSuggestionEngine.FreeSlot chosen = slots.get(0);
                LocalTime end = chosen.getStartTime().plusMinutes(duration);
                if (end.isAfter(chosen.getEndTime())) end = chosen.getEndTime();

                ScheduleBlockEntity newBlock = ScheduleBlockEntity.builder()
                        .taskId(task.getId())
                        .blockDate(planDate)
                        .startTime(chosen.getStartTime())
                        .endTime(end)
                        .title(task.getTitle())
                        .category("WORK")
                        .isLocked(false)
                        .build();
                newBlock.setUserId(userId);
                newBlock.setVersion(1L);
                scheduleBlockRepository.save(newBlock);

                occupied.add(ConflictDetector.TimeInterval.builder()
                        .id(newBlock.getId()).title(newBlock.getTitle())
                        .startTime(chosen.getStartTime()).endTime(end).type("BLOCK").build());
            }
        }

        return getDailyPlan(userId, planDate);
    }

    @Override
    @Transactional
    public DayRebalanceEngine.RebalanceProposal rebalanceDay(UUID userId, LocalDate date) {
        DailyPlanResponse dailyPlan = getDailyPlan(userId, date);
        DayRebalanceEngine.RebalanceProposal proposal = dayRebalanceEngine.generateRebalancePlan(
                date, dailyPlan.isOverloaded(), dailyPlan.getTasks(), dailyPlan.getScheduleBlocks()
        );

        if (proposal.isRebalanceRequired()) {
            for (DayRebalanceEngine.RebalanceShift shift : proposal.getRecommendedShifts()) {
                if (shift.getScheduleBlockId() != null) {
                    scheduleBlockRepository.findByIdAndUserIdAndDeletedAtIsNull(shift.getScheduleBlockId(), userId)
                            .ifPresent(b -> {
                                b.setBlockDate(shift.getProposedDate());
                                scheduleBlockRepository.save(b);
                            });
                }
                if (shift.getTaskId() != null) {
                    taskRepository.findByIdAndUserIdAndDeletedAtIsNull(shift.getTaskId(), userId)
                            .ifPresent(t -> {
                                t.setDueDate(shift.getProposedDate());
                                taskRepository.save(t);
                            });
                }
            }
        }

        return proposal;
    }

    @Override
    @Transactional(readOnly = true)
    public ConflictCheckResponse checkConflicts(UUID userId, ConflictCheckRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        List<ScheduleBlockEntity> existing = scheduleBlockRepository
                .findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, request.getBlockDate());

        List<ConflictDetector.TimeInterval> intervals = new ArrayList<>();
        List<String> details = new ArrayList<>();

        for (ScheduleBlockEntity b : existing) {
            if (request.getExcludeBlockId() != null && b.getId().equals(request.getExcludeBlockId())) {
                continue;
            }
            intervals.add(ConflictDetector.TimeInterval.builder()
                    .id(b.getId()).title(b.getTitle()).startTime(b.getStartTime()).endTime(b.getEndTime()).type("BLOCK").build());

            if (conflictDetector.overlaps(request.getStartTime(), request.getEndTime(), b.getStartTime(), b.getEndTime())) {
                details.add(String.format("Conflicts with '%s' (%s - %s)", b.getTitle(), b.getStartTime(), b.getEndTime()));
            }
        }

        UserPreferenceEntity prefs = userPreferenceRepository.findByUserId(userId).orElse(null);
        LocalTime wakeTime = prefs != null ? prefs.getDailyWakeTime() : LocalTime.of(7, 0);
        LocalTime sleepTime = prefs != null ? prefs.getDailySleepTime() : LocalTime.of(23, 0);

        List<SlotSuggestionEngine.FreeSlot> slots = slotSuggestionEngine.findAvailableSlots(
                wakeTime, sleepTime, intervals, request.getRequestedDurationMinutes()
        );

        return ConflictCheckResponse.builder()
                .hasConflict(!details.isEmpty())
                .conflictDetails(details)
                .suggestedSlots(slots)
                .build();
    }

    @Override
    @Transactional
    public ScheduleBlockResponse resolveConflict(UUID userId, ConflictResolutionRequest request) {
        if (request.getAction() == ConflictResolutionRequest.ConflictAction.KEEP_BOTH) {
            if (request.getNewBlock() != null) {
                request.getNewBlock().setAllowConflict(true);
                return createScheduleBlock(userId, request.getNewBlock());
            } else if (request.getScheduleBlockId() != null) {
                UpdateScheduleBlockRequest up = UpdateScheduleBlockRequest.builder()
                        .allowConflict(true)
                        .startTime(request.getChosenStartTime())
                        .endTime(request.getChosenEndTime())
                        .blockDate(request.getTargetDate())
                        .build();
                return updateScheduleBlock(userId, request.getScheduleBlockId(), up);
            }
        } else if (request.getAction() == ConflictResolutionRequest.ConflictAction.CHOOSE_TIME) {
            if (request.getScheduleBlockId() != null && request.getChosenStartTime() != null && request.getChosenEndTime() != null) {
                return chooseConflictTime(userId, request.getScheduleBlockId(), request.getChosenStartTime(), request.getChosenEndTime());
            }
        } else if (request.getAction() == ConflictResolutionRequest.ConflictAction.ACCEPT_SUGGESTION) {
            if (request.getScheduleBlockId() != null) {
                return acceptConflictSuggestion(userId, request.getScheduleBlockId());
            }
        }
        throw new BadRequestException("Invalid conflict resolution parameters");
    }

    @Override
    @Transactional
    public ScheduleBlockResponse acceptConflictSuggestion(UUID userId, UUID blockId) {
        ScheduleBlockEntity block = findBlockOrThrow(userId, blockId);
        int duration = (int) Duration.between(block.getStartTime(), block.getEndTime()).toMinutes();

        ConflictCheckRequest checkReq = ConflictCheckRequest.builder()
                .blockDate(block.getBlockDate())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .excludeBlockId(blockId)
                .requestedDurationMinutes(Math.max(15, duration))
                .build();

        ConflictCheckResponse checkRes = checkConflicts(userId, checkReq);
        if (!checkRes.getSuggestedSlots().isEmpty()) {
            SlotSuggestionEngine.FreeSlot slot = checkRes.getSuggestedSlots().get(0);
            LocalTime newEnd = slot.getStartTime().plusMinutes(duration);
            block.setStartTime(slot.getStartTime());
            block.setEndTime(newEnd);
            return ScheduleBlockResponse.fromEntity(scheduleBlockRepository.save(block));
        }

        return ScheduleBlockResponse.fromEntity(block);
    }

    @Override
    @Transactional
    public ScheduleBlockResponse chooseConflictTime(UUID userId, UUID blockId, LocalTime startTime, LocalTime endTime) {
        UpdateScheduleBlockRequest req = UpdateScheduleBlockRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .allowConflict(false)
                .build();
        return updateScheduleBlock(userId, blockId, req);
    }

    @Override
    @Transactional
    public ScheduleBlockResponse keepBothConflict(UUID userId, CreateScheduleBlockRequest request) {
        request.setAllowConflict(true);
        return createScheduleBlock(userId, request);
    }

    private ScheduleBlockEntity findBlockOrThrow(UUID userId, UUID blockId) {
        return scheduleBlockRepository.findByIdAndUserIdAndDeletedAtIsNull(blockId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleBlock", "id", blockId));
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Schedule block start time and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("Schedule block end time must be after start time");
        }
    }

    private void checkScheduleConflicts(UUID userId, LocalDate date, LocalTime start, LocalTime end, UUID currentBlockId) {
        if (date == null) {
            return;
        }

        List<ScheduleBlockEntity> existingBlocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, date);

        for (ScheduleBlockEntity existing : existingBlocks) {
            if (currentBlockId != null && existing.getId().equals(currentBlockId)) {
                continue; // Ignore self when updating
            }
            if (start.isBefore(existing.getEndTime()) && end.isAfter(existing.getStartTime())) {
                throw new ScheduleConflictException(String.format(
                        "Schedule block conflicts with existing block '%s' (%s - %s)",
                        existing.getTitle(), existing.getStartTime(), existing.getEndTime()
                ));
            }
        }
    }
}
