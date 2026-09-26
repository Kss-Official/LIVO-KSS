package com.livo.api.modules.home.service;

import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.repository.AiRecommendationRepository;
import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.entity.BudgetEntity;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.BudgetRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.home.dto.*;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import com.livo.api.modules.plan.repository.ScheduleBlockRepository;
import com.livo.api.modules.routine.entity.RoutineEntity;
import com.livo.api.modules.routine.repository.RoutineRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final TaskRepository taskRepository;
    private final ScheduleBlockRepository scheduleBlockRepository;
    private final EventRepository eventRepository;
    private final RoutineRepository routineRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final NotificationRepository notificationRepository;

    private static final List<DailyQuoteDto> CURATED_QUOTES = List.of(
            new DailyQuoteDto("Simplicity is the soul of efficiency.", "Austin Freeman"),
            new DailyQuoteDto("Action is the foundational key to all success.", "Pablo Picasso"),
            new DailyQuoteDto("Focus on being productive instead of busy.", "Tim Ferriss"),
            new DailyQuoteDto("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Will Durant"),
            new DailyQuoteDto("The secret of getting ahead is getting started.", "Mark Twain"),
            new DailyQuoteDto("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
            new DailyQuoteDto("Deep work is the ability to focus without distraction on a cognitively demanding task.", "Cal Newport"),
            new DailyQuoteDto("Time is what we want most, but what we use worst.", "William Penn"),
            new DailyQuoteDto("Order and simplification are the first steps toward the mastery of a subject.", "Thomas Mann"),
            new DailyQuoteDto("It is not that we have a short time to live, but that we waste a lot of it.", "Seneca")
    );

    @Override
    @Transactional(readOnly = true)
    public HomeDashboardResponse getHomeDashboard(UUID userId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 1. User Profile & Timezone
        UserEntity user = userRepository.findById(userId).orElse(null);
        String timezone = (user != null && user.getTimezone() != null) ? user.getTimezone() : "Asia/Kolkata";
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            zoneId = ZoneId.of("Asia/Kolkata");
        }

        LocalTime currentTime = LocalTime.now(zoneId);
        String userName = (user != null && user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName().trim().split("\\s+")[0]
                : "Pilot";
        String greeting = generateGreeting(currentTime, userName);

        // 2. Daily Quote
        DailyQuoteDto dailyQuote = getDailyQuote(targetDate);

        // 3. Active Task ("Now" Indicator)
        ActiveTaskCard activeTask = resolveActiveTask(userId);

        // 4. Priority Carousel (Top 3 Tasks)
        List<PriorityTaskCard> priorityCarousel = resolvePriorityCarousel(userId, targetDate);

        // 5. Next Up Schedule Block
        NextUpCard nextUpSchedule = resolveNextUpSchedule(userId, targetDate, currentTime, zoneId);

        // 6. Habits Checklist & Dots
        HabitsProgressSummary habitsProgress = resolveHabitsProgress(userId, targetDate);

        // 7. Goals Progress
        GoalsProgressSummary goalsProgress = resolveGoalsProgress(userId);

        // 8. Daily Life Score
        DailyLifeScoreDto dailyLifeScore = calculateDailyLifeScore(userId, targetDate, habitsProgress, goalsProgress);

        // 9. Workload Summary
        WorkloadSummaryDto workloadSummary = resolveWorkloadSummary(userId, targetDate);

        // 10. Finance Snapshot
        FinanceSnapshotDto financeSnapshot = resolveFinanceSnapshot(userId, targetDate);

        // 11. Proactive AI Recommendation
        HomeRecommendationDto recommendation = resolveTopRecommendation(userId);

        // 12. Unread Notifications Count
        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(userId);

        log.debug("Built Home Dashboard for user {} on {}: Score={}, Tasks={}, Habits={}/{}",
                userId, targetDate, dailyLifeScore.getOverallScore(), priorityCarousel.size(),
                habitsProgress.getCompletedToday(), habitsProgress.getTotalScheduledToday());

        return HomeDashboardResponse.builder()
                .date(targetDate)
                .greeting(greeting)
                .dailyQuote(dailyQuote)
                .dailyLifeScore(dailyLifeScore)
                .activeTask(activeTask)
                .priorityCarousel(priorityCarousel)
                .nextUpSchedule(nextUpSchedule)
                .habitsProgress(habitsProgress)
                .goalsProgress(goalsProgress)
                .workloadSummary(workloadSummary)
                .financeSnapshot(financeSnapshot)
                .proactiveRecommendation(recommendation)
                .unreadNotificationCount(unreadCount)
                .build();
    }

    @Override
    public DailyQuoteDto getDailyQuote(LocalDate date) {
        LocalDate d = (date != null) ? date : LocalDate.now();
        int index = Math.abs(d.hashCode()) % CURATED_QUOTES.size();
        return CURATED_QUOTES.get(index);
    }

    private String generateGreeting(LocalTime time, String firstName) {
        if (time.isBefore(LocalTime.of(12, 0))) {
            return "Good morning, " + firstName;
        } else if (time.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon, " + firstName;
        } else {
            return "Good evening, " + firstName;
        }
    }

    private ActiveTaskCard resolveActiveTask(UUID userId) {
        List<TaskEntity> inProgress = taskRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, TaskStatus.IN_PROGRESS);
        for (TaskEntity task : inProgress) {
            if (task.getStartedAt() != null) {
                long elapsed = Duration.between(task.getStartedAt(), Instant.now()).toMinutes();
                if (elapsed < 0) elapsed = 0;
                return ActiveTaskCard.builder()
                        .taskId(task.getId())
                        .title(task.getTitle())
                        .priority(task.getPriority())
                        .status(task.getStatus())
                        .startedAt(task.getStartedAt())
                        .elapsedMinutes(elapsed)
                        .projectLabel(task.getProjectLabel())
                        .build();
            }
        }
        return null;
    }

    private List<PriorityTaskCard> resolvePriorityCarousel(UUID userId, LocalDate targetDate) {
        List<TaskEntity> candidates = taskRepository.findPriorityCandidates(
                userId, targetDate, org.springframework.data.domain.PageRequest.of(0, 3)
        );

        List<PriorityTaskCard> cards = new ArrayList<>();
        int total = candidates.size();
        for (int i = 0; i < total; i++) {
            TaskEntity t = candidates.get(i);
            int rank = i + 1;
            String reason = determinePriorityReason(t, targetDate);

            cards.add(PriorityTaskCard.builder()
                    .taskId(t.getId())
                    .title(t.getTitle())
                    .priority(t.getPriority())
                    .status(t.getStatus())
                    .dueDate(t.getDueDate())
                    .dueTime(t.getDueTime())
                    .durationMins(t.getDurationMins())
                    .projectLabel(t.getProjectLabel())
                    .rank(rank)
                    .rankLabel(rank + " of " + total)
                    .whyLivoPicked(reason)
                    .build());
        }

        return cards;
    }

    private int getPriorityWeight(TaskPriority priority) {
        if (priority == null) return 1;
        return switch (priority) {
            case URGENT -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private String determinePriorityReason(TaskEntity t, LocalDate targetDate) {
        if (t.getDueDate() != null && t.getDueDate().isBefore(targetDate)) {
            return "Overdue task requiring immediate recovery";
        }
        if (t.getStatus() == TaskStatus.IN_PROGRESS) {
            return "Currently in progress";
        }
        if (t.getPriority() == TaskPriority.URGENT) {
            return "Urgent priority scheduled for today";
        }
        if (t.getPriority() == TaskPriority.HIGH) {
            return "High impact task scheduled for today";
        }
        return "Top priority task aligned with your daily schedule";
    }

    public NextUpCard resolveNextUpSchedule(UUID userId, LocalDate targetDate, LocalTime currentTime, ZoneId zoneId) {
        // 1. Schedule blocks for today
        List<ScheduleBlockEntity> blocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, targetDate);

        // 2. Events for today
        Instant dayStart = targetDate.atStartOfDay(zoneId).toInstant();
        Instant dayEnd = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1);
        List<EventEntity> events = eventRepository
                .findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(userId, dayStart, dayEnd);

        // 3. Routines for today's day of week (1=Mon..7=Sun)
        int dayOfWeek = targetDate.getDayOfWeek().getValue();
        List<RoutineEntity> routines = routineRepository
                .findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId).stream()
                .filter(r -> r.getDaysOfWeek() != null && r.getDaysOfWeek().contains(dayOfWeek))
                .toList();

        // Flatten all commitments
        List<NextUpCard> items = new ArrayList<>();
        LocalDateTime currentDateTime = targetDate.atTime(currentTime);

        for (ScheduleBlockEntity b : blocks) {
            boolean liveNow;
            if (b.getEndTime().isBefore(b.getStartTime())) {
                liveNow = !currentTime.isBefore(b.getStartTime()) || currentTime.isBefore(b.getEndTime());
            } else {
                liveNow = !currentTime.isBefore(b.getStartTime()) && currentTime.isBefore(b.getEndTime());
            }
            items.add(NextUpCard.builder()
                    .id(b.getId())
                    .type("TASK_BLOCK")
                    .title(b.getTitle())
                    .startTime(b.getStartTime())
                    .endTime(b.getEndTime())
                    .locationOrCategory(b.getCategory())
                    .isLiveNow(liveNow)
                    .build());
        }

        for (EventEntity e : events) {
            LocalDateTime evStartDateTime = e.getStartTime().atZone(zoneId).toLocalDateTime();
            LocalDateTime evEndDateTime = (e.getEndTime() != null)
                    ? e.getEndTime().atZone(zoneId).toLocalDateTime()
                    : evStartDateTime.plusMinutes(60);
            boolean liveNow = !currentDateTime.isBefore(evStartDateTime) && currentDateTime.isBefore(evEndDateTime);
            items.add(NextUpCard.builder()
                    .id(e.getId())
                    .type("EVENT")
                    .title(e.getTitle())
                    .startTime(evStartDateTime.toLocalTime())
                    .endTime(evEndDateTime.toLocalTime())
                    .locationOrCategory(e.getLocation() != null ? e.getLocation() : (e.getCategory() != null ? e.getCategory() : "EVENT"))
                    .isLiveNow(liveNow)
                    .build());
        }

        for (RoutineEntity r : routines) {
            boolean liveNow;
            if (r.getEndTime().isBefore(r.getStartTime())) {
                liveNow = !currentTime.isBefore(r.getStartTime()) || currentTime.isBefore(r.getEndTime());
            } else {
                liveNow = !currentTime.isBefore(r.getStartTime()) && currentTime.isBefore(r.getEndTime());
            }
            items.add(NextUpCard.builder()
                    .id(r.getId())
                    .type("ROUTINE")
                    .title(r.getTitle())
                    .startTime(r.getStartTime())
                    .endTime(r.getEndTime())
                    .locationOrCategory("ROUTINE")
                    .isLiveNow(liveNow)
                    .build());
        }

        if (items.isEmpty()) {
            return null;
        }

        // Check if anything is currently live
        Optional<NextUpCard> liveItem = items.stream().filter(NextUpCard::isLiveNow).findFirst();
        if (liveItem.isPresent()) {
            return liveItem.get();
        }

        // Otherwise find upcoming item with startTime >= currentTime
        return items.stream()
                .filter(i -> !i.getStartTime().isBefore(currentTime))
                .min(Comparator.comparing(NextUpCard::getStartTime))
                .orElse(items.get(0)); // fallback to first item of the day
    }

    private HabitsProgressSummary resolveHabitsProgress(UUID userId, LocalDate targetDate) {
        List<HabitEntity> habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        int dayOfWeek = targetDate.getDayOfWeek().getValue();

        List<HabitEntity> scheduledHabits = habits.stream()
                .filter(h -> isHabitScheduledForDay(h, dayOfWeek, targetDate))
                .toList();

        List<HabitLogEntity> logs = habitLogRepository.findAllByUserIdAndLogDateAndDeletedAtIsNull(userId, targetDate);
        Set<UUID> completedHabitIds = logs.stream()
                .map(HabitLogEntity::getHabitId)
                .collect(Collectors.toSet());

        List<HabitDotDto> dots = new ArrayList<>();
        int completedCount = 0;

        for (HabitEntity h : scheduledHabits) {
            boolean checkedIn = completedHabitIds.contains(h.getId());
            if (checkedIn) completedCount++;

            dots.add(HabitDotDto.builder()
                    .habitId(h.getId())
                    .title(h.getTitle())
                    .iconKey(h.getIconKey())
                    .colorHex(h.getColorHex())
                    .currentStreak(h.getCurrentStreak())
                    .isCheckedInToday(checkedIn)
                    .build());
        }

        double rate = scheduledHabits.isEmpty() ? 100.0 : (completedCount * 100.0) / scheduledHabits.size();
        rate = BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return HabitsProgressSummary.builder()
                .totalScheduledToday(scheduledHabits.size())
                .completedToday(completedCount)
                .completionRate(rate)
                .habits(dots)
                .build();
    }

    private boolean isHabitScheduledForDay(HabitEntity habit, int dayOfWeek, LocalDate date) {
        if (habit.getStartDate() != null && date.isBefore(habit.getStartDate())) return false;
        if (habit.getEndDate() != null && date.isAfter(habit.getEndDate())) return false;

        HabitFrequency freq = habit.getFrequencyType();
        if (freq == null || freq == HabitFrequency.DAILY) return true;
        if (freq == HabitFrequency.WEEKDAYS) return dayOfWeek >= 1 && dayOfWeek <= 5;
        if (freq == HabitFrequency.WEEKLY) {
            if (habit.getCustomDays() != null && !habit.getCustomDays().isEmpty()) {
                return habit.getCustomDays().contains(dayOfWeek);
            }
            return dayOfWeek == 1;
        }
        if (freq == HabitFrequency.SPECIFIC_DAYS || freq == HabitFrequency.CUSTOM) {
            return habit.getCustomDays() != null && habit.getCustomDays().contains(dayOfWeek);
        }
        return true;
    }

    private GoalsProgressSummary resolveGoalsProgress(UUID userId) {
        List<GoalEntity> activeGoals = goalRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, GoalStatus.IN_PROGRESS);

        long totalMilestones = 0;
        long completedMilestones = 0;
        List<TopGoalDto> topGoals = new ArrayList<>();

        if (!activeGoals.isEmpty()) {
            List<UUID> goalIds = activeGoals.stream().map(GoalEntity::getId).collect(Collectors.toList());
            List<MilestoneEntity> allMilestones = milestoneRepository
                    .findAllByGoalIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalIds, userId);

            Map<UUID, List<MilestoneEntity>> milestonesByGoal = allMilestones.stream()
                    .collect(Collectors.groupingBy(MilestoneEntity::getGoalId));

            for (GoalEntity goal : activeGoals) {
                List<MilestoneEntity> goalMilestones = milestonesByGoal.getOrDefault(goal.getId(), Collections.emptyList());
                long totalM = goalMilestones.size();
                long compM = goalMilestones.stream().filter(MilestoneEntity::isCompleted).count();

                totalMilestones += totalM;
                completedMilestones += compM;

                Double progress = com.livo.api.modules.goal.dto.GoalResponse.calculateProgressPercentage(
                        goal.getProgressTrackingType(),
                        goal.getCurrentValue(),
                        goal.getTargetValue(),
                        totalM,
                        compM
                );
                BigDecimal progressBD = BigDecimal.valueOf(progress).setScale(2, RoundingMode.HALF_UP);

                topGoals.add(TopGoalDto.builder()
                        .goalId(goal.getId())
                        .title(goal.getTitle())
                        .progressPercentage(progressBD)
                        .targetValue(goal.getTargetValue())
                        .currentValue(goal.getCurrentValue())
                        .unit(goal.getUnit())
                        .milestonesCompleted(compM)
                        .totalMilestones(totalM)
                        .build());
            }
        }

        // Sort top goals by progress percentage descending, take top 3
        topGoals.sort((a, b) -> b.getProgressPercentage().compareTo(a.getProgressPercentage()));
        if (topGoals.size() > 3) {
            topGoals = topGoals.subList(0, 3);
        }

        double progressPct;
        String label;
        if (totalMilestones > 0) {
            progressPct = (completedMilestones * 100.0) / totalMilestones;
            label = "Goals " + completedMilestones + "/" + totalMilestones;
        } else if (!topGoals.isEmpty()) {
            double avgProgress = topGoals.stream()
                    .map(TopGoalDto::getProgressPercentage)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0.0);
            progressPct = avgProgress;
            label = activeGoals.size() + " Active Goal" + (activeGoals.size() > 1 ? "s" : "");
        } else {
            progressPct = 100.0;
            label = "All Goals Complete";
        }

        progressPct = BigDecimal.valueOf(progressPct).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return GoalsProgressSummary.builder()
                .activeGoalsCount(activeGoals.size())
                .totalMilestones(totalMilestones)
                .completedMilestones(completedMilestones)
                .progressPercentage(progressPct)
                .label(label)
                .topGoals(topGoals)
                .build();
    }

    private DailyLifeScoreDto calculateDailyLifeScore(UUID userId, LocalDate targetDate,
                                                     HabitsProgressSummary habits, GoalsProgressSummary goals) {
        // Task completion rate for tasks scheduled/due today
        List<TaskEntity> tasksToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(userId, targetDate);
        double taskRate;
        if (tasksToday.isEmpty()) {
            taskRate = 100.0;
        } else {
            long completed = tasksToday.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .count();
            taskRate = (completed * 100.0) / tasksToday.size();
        }

        double habitRate = habits.getCompletionRate();
        double goalRate = goals.getProgressPercentage();

        // Formula: Score = (0.40 * TaskRate) + (0.35 * HabitRate) + (0.25 * GoalRate)
        double score = (0.40 * taskRate) + (0.35 * habitRate) + (0.25 * goalRate);
        int overallScore = (int) Math.round(score);
        if (overallScore < 0) overallScore = 0;
        if (overallScore > 100) overallScore = 100;

        String label;
        if (overallScore >= 85) label = "Exceptional";
        else if (overallScore >= 65) label = "On Track";
        else if (overallScore >= 40) label = "Building Momentum";
        else label = "Needs Attention";

        return DailyLifeScoreDto.builder()
                .overallScore(overallScore)
                .taskCompletionRate(BigDecimal.valueOf(taskRate).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .habitCheckinRate(BigDecimal.valueOf(habitRate).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .goalPaceRate(BigDecimal.valueOf(goalRate).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .ratingLabel(label)
                .build();
    }

    private WorkloadSummaryDto resolveWorkloadSummary(UUID userId, LocalDate targetDate) {
        List<ScheduleBlockEntity> blocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(userId, targetDate);
        List<TaskEntity> tasks = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(userId, targetDate);

        long blockMinutes = 0;
        for (ScheduleBlockEntity b : blocks) {
            if (b.getEndTime().isAfter(b.getStartTime())) {
                blockMinutes += Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
            }
        }

        long taskMinutes = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                .mapToLong(t -> t.getDurationMins() != null ? t.getDurationMins() : 30)
                .sum();

        BigDecimal plannedHours = BigDecimal.valueOf(blockMinutes + taskMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal maxPlannedHours = userPreferenceRepository.findByUserId(userId)
                .map(UserPreferenceEntity::getMaxPlannedHoursPerDay)
                .orElse(new BigDecimal("8.00"));

        // Available Waking Hours = 16.0 - routinesDuration
        int dayOfWeek = targetDate.getDayOfWeek().getValue();
        List<RoutineEntity> routines = routineRepository.findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId).stream()
                .filter(r -> r.getDaysOfWeek() != null && r.getDaysOfWeek().contains(dayOfWeek))
                .toList();

        long routineMinutes = 0;
        for (RoutineEntity r : routines) {
            if (r.getEndTime().isAfter(r.getStartTime())) {
                routineMinutes += Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
            }
        }

        BigDecimal routineHours = BigDecimal.valueOf(routineMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal availableWakingHours = BigDecimal.valueOf(16.0).subtract(routineHours);
        if (availableWakingHours.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            availableWakingHours = BigDecimal.valueOf(1.0);
        }

        double capacityPct = plannedHours.divide(availableWakingHours, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        String status;
        if (capacityPct > 100.0) status = "OVERLOADED";
        else if (capacityPct >= 85.0) status = "BALANCED";
        else if (capacityPct >= 50.0) status = "OPTIMAL";
        else status = "LIGHT";

        boolean isOverloaded = plannedHours.compareTo(maxPlannedHours) > 0 || capacityPct > 100.0;

        return WorkloadSummaryDto.builder()
                .plannedWorkloadHours(plannedHours)
                .maxPlannedHours(maxPlannedHours)
                .availableWakingHours(availableWakingHours)
                .capacityPercentage(capacityPct)
                .workloadStatus(status)
                .isOverloaded(isOverloaded)
                .build();
    }

    private FinanceSnapshotDto resolveFinanceSnapshot(UUID userId, LocalDate targetDate) {
        LocalDate monthStart = targetDate.withDayOfMonth(1);
        LocalDate monthEnd = targetDate.withDayOfMonth(targetDate.lengthOfMonth());

        List<TransactionEntity> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(userId, monthStart, monthEnd);

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (TransactionEntity tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(tx.getAmount());
            } else if (tx.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(tx.getAmount());
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        List<BudgetEntity> budgets = budgetRepository.findAllByUserIdAndMonthStartAndDeletedAtIsNull(userId, monthStart);
        BigDecimal totalBudgetLimit = BigDecimal.ZERO;
        boolean isNearAlert = false;

        for (BudgetEntity b : budgets) {
            totalBudgetLimit = totalBudgetLimit.add(b.getMonthlyLimit());
            // Category spend check
            BigDecimal catSpend = transactions.stream()
                    .filter(tx -> tx.getType() == TransactionType.EXPENSE && b.getCategory().equalsIgnoreCase(tx.getCategory()))
                    .map(TransactionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
                double pct = catSpend.divide(b.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                if (pct >= b.getAlertThresholdPercent()) {
                    isNearAlert = true;
                }
            }
        }

        double budgetUsedPercentage = 0.0;
        if (totalBudgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPercentage = totalExpenses.divide(totalBudgetLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        String monthStr = targetDate.getYear() + "-" + String.format("%02d", targetDate.getMonthValue());

        return FinanceSnapshotDto.builder()
                .currentMonth(monthStr)
                .monthTotalExpense(totalExpenses.setScale(2, RoundingMode.HALF_UP))
                .monthTotalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP))
                .netSavings(netSavings.setScale(2, RoundingMode.HALF_UP))
                .monthlyBudgetLimit(totalBudgetLimit.setScale(2, RoundingMode.HALF_UP))
                .budgetUsedPercentage(budgetUsedPercentage)
                .isNearBudgetAlert(isNearAlert)
                .build();
    }

    private HomeRecommendationDto resolveTopRecommendation(UUID userId) {
        List<AiRecommendationEntity> active = aiRecommendationRepository.findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        for (AiRecommendationEntity r : active) {
            if (r.getFeedbackType() == null) {
                return HomeRecommendationDto.builder()
                        .id(r.getId())
                        .type(r.getType() != null ? r.getType().name() : "GENERAL")
                        .title(r.getTitle())
                        .reason(r.getReason())
                        .actionType(r.getActionType())
                        .relatedEntityId(r.getRelatedEntityId())
                        .build();
            }
        }
        return null;
    }
}
