package com.livo.api.modules.insights.service;

import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.insights.dto.*;
import com.livo.api.modules.learning.entity.LearningSessionEntity;
import com.livo.api.modules.learning.repository.LearningSessionRepository;
import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import com.livo.api.modules.plan.repository.ScheduleBlockRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightsServiceImpl implements InsightsService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ScheduleBlockRepository scheduleBlockRepository;
    private final EventRepository eventRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final LearningSessionRepository learningSessionRepository;

    private static final Map<String, String> CATEGORY_COLORS = Map.of(
            "WORK", "#3B82F6",
            "PERSONAL", "#10B981",
            "LEARNING", "#8B5CF6",
            "HEALTH", "#EC4899",
            "TRAVEL", "#F59E0B",
            "OTHERS", "#6B7280"
    );

    @Override
    @Transactional(readOnly = true)
    public ComprehensiveInsightsResponse getComprehensiveInsights(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        TimeDistributionResponse timeDist = getTimeDistribution(userId, start, end);
        ProductivityHistogramResponse histogram = getProductivityHistogram(userId, start, end);
        WeeklyComparisonResponse weeklyComp = getWeeklyComparison(userId, end);
        HabitCorrelationResponse habitCorr = getHabitCorrelations(userId, start, end);
        UserStatsResponse userStats = getUserStats(userId);

        // Weekly expenses
        BigDecimal totalExpensesThisWeek = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(userId, start, end)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return ComprehensiveInsightsResponse.builder()
                .startDate(start)
                .endDate(end)
                .timeDistribution(timeDist)
                .productivityHistogram(histogram)
                .weeklyComparison(weeklyComp)
                .habitCorrelations(habitCorr)
                .userStats(userStats)
                .totalExpensesThisWeek(totalExpensesThisWeek)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeDistributionResponse getTimeDistribution(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        Map<String, Long> categoryMinutes = new HashMap<>();
        for (String cat : CATEGORY_COLORS.keySet()) {
            categoryMinutes.put(cat, 0L);
        }

        // 1. Schedule blocks
        List<ScheduleBlockEntity> blocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(userId, start, end);
        for (ScheduleBlockEntity b : blocks) {
            if (b.getEndTime() != null && b.getStartTime() != null && b.getEndTime().isAfter(b.getStartTime())) {
                long mins = Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
                String cat = normalizeCategory(b.getCategory());
                categoryMinutes.put(cat, categoryMinutes.getOrDefault(cat, 0L) + mins);
            }
        }

        // 2. Events in range
        ZoneId zoneId = getUserZoneId(userId);
        Instant rangeStart = start.atStartOfDay(zoneId).toInstant();
        Instant rangeEnd = end.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1);
        List<EventEntity> events = eventRepository
                .findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(userId, rangeStart, rangeEnd);
        for (EventEntity e : events) {
            long mins = 60;
            if (e.getEndTime() != null && e.getEndTime().isAfter(e.getStartTime())) {
                mins = Duration.between(e.getStartTime(), e.getEndTime()).toMinutes();
            }
            String cat = normalizeCategory(e.getCategory());
            categoryMinutes.put(cat, categoryMinutes.getOrDefault(cat, 0L) + mins);
        }

        // 3. Tasks in range (if tasks have actual duration or durationMins and no schedule block)
        List<TaskEntity> tasks = taskRepository
                .findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, start, end);
        for (TaskEntity t : tasks) {
            long mins = t.getActualDurationMins() != null ? t.getActualDurationMins()
                    : (t.getDurationMins() != null ? t.getDurationMins() : 30);
            String cat = normalizeCategory(t.getProjectLabel());
            categoryMinutes.put(cat, categoryMinutes.getOrDefault(cat, 0L) + mins);
        }

        long totalTracked = categoryMinutes.values().stream().mapToLong(Long::longValue).sum();
        BigDecimal totalTrackedHours = BigDecimal.valueOf(totalTracked)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        List<CategoryTimeDistributionDto> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryMinutes.entrySet()) {
            long mins = entry.getValue();
            double pct = (totalTracked > 0) ? (mins * 100.0) / totalTracked : 0.0;
            pct = BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue();

            list.add(CategoryTimeDistributionDto.builder()
                    .category(entry.getKey())
                    .totalMinutes(mins)
                    .totalHours(BigDecimal.valueOf(mins).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP))
                    .percentage(pct)
                    .colorHex(CATEGORY_COLORS.getOrDefault(entry.getKey(), "#6B7280"))
                    .build());
        }

        list.sort((a, b) -> Long.compare(b.getTotalMinutes(), a.getTotalMinutes()));

        return TimeDistributionResponse.builder()
                .startDate(start)
                .endDate(end)
                .totalTrackedMinutes(totalTracked)
                .totalTrackedHours(totalTrackedHours)
                .categories(list)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductivityHistogramResponse getProductivityHistogram(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(29);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        ZoneId zoneId = getUserZoneId(userId);

        int[] taskCounts = new int[24];
        long[] focusMins = new long[24];

        // 1. Completed tasks in date range
        List<TaskEntity> tasks = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, start, end)
                .stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .toList();

        for (TaskEntity t : tasks) {
            int hour;
            if (t.getStartedAt() != null) {
                hour = t.getStartedAt().atZone(zoneId).getHour();
            } else if (t.getDueTime() != null) {
                hour = t.getDueTime().getHour();
            } else if (t.getUpdatedAt() != null) {
                hour = t.getUpdatedAt().atZone(zoneId).getHour();
            } else {
                hour = 10; // default focus hour
            }
            if (hour >= 0 && hour < 24) {
                taskCounts[hour]++;
                long dur = t.getActualDurationMins() != null ? t.getActualDurationMins()
                        : (t.getDurationMins() != null ? t.getDurationMins() : 30);
                focusMins[hour] += dur;
            }
        }

        // 2. Schedule blocks in range
        List<ScheduleBlockEntity> blocks = scheduleBlockRepository
                .findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(userId, start, end);
        for (ScheduleBlockEntity b : blocks) {
            if (b.getStartTime() != null && b.getEndTime() != null) {
                int hour = b.getStartTime().getHour();
                if (hour >= 0 && hour < 24) {
                    long dur = Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
                    if (dur > 0) focusMins[hour] += dur;
                }
            }
        }

        // Find peak hour
        int peakHour = 10;
        long maxVal = -1;
        for (int h = 0; h < 24; h++) {
            long score = focusMins[h] + (taskCounts[h] * 30L);
            if (score > maxVal) {
                maxVal = score;
                peakHour = h;
            }
        }

        List<HourlyProductivityDto> hourlyList = new ArrayList<>();
        int totalTasks = 0;
        long totalMins = 0;

        for (int h = 0; h < 24; h++) {
            totalTasks += taskCounts[h];
            totalMins += focusMins[h];

            hourlyList.add(HourlyProductivityDto.builder()
                    .hour(h)
                    .label(formatHourLabel(h))
                    .completedTasksCount(taskCounts[h])
                    .focusMinutes(focusMins[h])
                    .isPeakHour(h == peakHour)
                    .build());
        }

        String peakWindow = formatHourWindow(peakHour);

        return ProductivityHistogramResponse.builder()
                .peakHour(peakHour)
                .peakHourWindow(peakWindow)
                .totalCompletedTasks(totalTasks)
                .totalFocusMinutes(totalMins)
                .hourlyDistribution(hourlyList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyComparisonResponse getWeeklyComparison(UUID userId, LocalDate referenceDate) {
        LocalDate ref = (referenceDate != null) ? referenceDate : LocalDate.now();
        LocalDate currStart = ref.with(DayOfWeek.MONDAY);
        LocalDate currEnd = currStart.plusDays(6);
        LocalDate prevStart = currStart.minusWeeks(1);
        LocalDate prevEnd = prevStart.plusDays(6);

        // 1. Tasks completed
        long currTasks = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, currStart, currEnd).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        long prevTasks = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, prevStart, prevEnd).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();

        // 2. Focus hours (schedule blocks + task duration)
        long currBlockMins = scheduleBlockRepository
                .findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(userId, currStart, currEnd).stream()
                .filter(b -> b.getEndTime().isAfter(b.getStartTime()))
                .mapToLong(b -> Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                .sum();
        long prevBlockMins = scheduleBlockRepository
                .findAllByUserIdAndBlockDateBetweenAndDeletedAtIsNullOrderByBlockDateAscStartTimeAsc(userId, prevStart, prevEnd).stream()
                .filter(b -> b.getEndTime().isAfter(b.getStartTime()))
                .mapToLong(b -> Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                .sum();

        BigDecimal currFocusHours = BigDecimal.valueOf(currBlockMins).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        BigDecimal prevFocusHours = BigDecimal.valueOf(prevBlockMins).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);

        // 3. Habit check-ins
        List<HabitEntity> userHabits = habitRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        Set<UUID> habitIds = userHabits.stream().map(HabitEntity::getId).collect(Collectors.toSet());

        long currHabitCheckins = habitIds.isEmpty() ? 0 :
                habitLogRepository.countByHabitIdInAndUserIdAndLogDateBetweenAndDeletedAtIsNull(habitIds, userId, currStart, currEnd);
        long prevHabitCheckins = habitIds.isEmpty() ? 0 :
                habitLogRepository.countByHabitIdInAndUserIdAndLogDateBetweenAndDeletedAtIsNull(habitIds, userId, prevStart, prevEnd);

        // 4. Total expenses
        BigDecimal currExpenses = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(userId, currStart, currEnd).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prevExpenses = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(userId, prevStart, prevEnd).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WeekComparisonMetricDto> metrics = new ArrayList<>();
        metrics.add(buildMetric("TASKS_COMPLETED", "Tasks Completed", BigDecimal.valueOf(currTasks), BigDecimal.valueOf(prevTasks), true));
        metrics.add(buildMetric("FOCUS_HOURS", "Focus Hours", currFocusHours, prevFocusHours, true));
        metrics.add(buildMetric("HABIT_CHECKINS", "Habit Check-ins", BigDecimal.valueOf(currHabitCheckins), BigDecimal.valueOf(prevHabitCheckins), true));
        metrics.add(buildMetric("TOTAL_EXPENSES", "Total Expenses", currExpenses, prevExpenses, false));

        double overallDelta = (metrics.get(0).getPercentageChange() + metrics.get(1).getPercentageChange() + metrics.get(2).getPercentageChange()) / 3.0;
        overallDelta = BigDecimal.valueOf(overallDelta).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return WeeklyComparisonResponse.builder()
                .currentWeekStart(currStart)
                .currentWeekEnd(currEnd)
                .previousWeekStart(prevStart)
                .previousWeekEnd(prevEnd)
                .overallProductivityDelta(overallDelta)
                .metrics(metrics)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitCorrelationResponse getHabitCorrelations(UUID userId, int days) {
        int evalDays = (days > 0 && days <= 90) ? days : 30;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(evalDays - 1);
        return getHabitCorrelations(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public HabitCorrelationResponse getHabitCorrelations(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(29);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        int evalDays = Math.max(1, (int) ChronoUnit.DAYS.between(start, end) + 1);

        List<HabitEntity> habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        List<TaskEntity> tasks = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, start, end);

        // Daily task completion stats
        Map<LocalDate, Long> totalTasksByDate = new HashMap<>();
        Map<LocalDate, Long> completedTasksByDate = new HashMap<>();

        for (TaskEntity t : tasks) {
            LocalDate d = t.getDueDate();
            if (d != null) {
                totalTasksByDate.put(d, totalTasksByDate.getOrDefault(d, 0L) + 1);
                if (t.getStatus() == TaskStatus.COMPLETED) {
                    completedTasksByDate.put(d, completedTasksByDate.getOrDefault(d, 0L) + 1);
                }
            }
        }

        List<HabitCorrelationCardDto> cards = new ArrayList<>();

        for (HabitEntity habit : habits) {
            List<HabitLogEntity> logs = habitLogRepository
                    .findAllByHabitIdAndUserIdAndLogDateBetweenAndDeletedAtIsNullOrderByLogDateAsc(habit.getId(), userId, start, end);
            Set<LocalDate> loggedDates = logs.stream().map(HabitLogEntity::getLogDate).collect(Collectors.toSet());

            List<Double> habitDayRates = new ArrayList<>();
            List<Double> nonHabitDayRates = new ArrayList<>();

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                long total = totalTasksByDate.getOrDefault(d, 0L);
                if (total > 0) {
                    double rate = (completedTasksByDate.getOrDefault(d, 0L) * 100.0) / total;
                    if (loggedDates.contains(d)) {
                        habitDayRates.add(rate);
                    } else {
                        nonHabitDayRates.add(rate);
                    }
                }
            }

            double avgHabitRate = habitDayRates.isEmpty() ? 75.0 : habitDayRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double avgNonHabitRate = nonHabitDayRates.isEmpty() ? 50.0 : nonHabitDayRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double lift = avgHabitRate - avgNonHabitRate;
            lift = BigDecimal.valueOf(lift).setScale(1, RoundingMode.HALF_UP).doubleValue();

            String insight;
            if (lift > 0) {
                insight = "On days you completed " + habit.getTitle() + ", your task completion rate was " + Math.abs(lift) + "% higher.";
            } else if (lift < 0) {
                insight = "Habit " + habit.getTitle() + " days exhibited rigorous task volume with steady performance.";
            } else {
                insight = "Habit " + habit.getTitle() + " provides steady baseline consistency across your schedule.";
            }

            cards.add(HabitCorrelationCardDto.builder()
                    .habitId(habit.getId())
                    .habitTitle(habit.getTitle())
                    .iconKey(habit.getIconKey())
                    .colorHex(habit.getColorHex())
                    .sampleDaysEvaluated(evalDays)
                    .taskCompletionRateOnHabitDays(BigDecimal.valueOf(avgHabitRate).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .taskCompletionRateOnNonHabitDays(BigDecimal.valueOf(avgNonHabitRate).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .liftPercentage(lift)
                    .insightText(insight)
                    .build());
        }

        cards.sort((a, b) -> Double.compare(b.getLiftPercentage(), a.getLiftPercentage()));

        String headline = cards.isEmpty()
                ? "Maintain consistent daily habits to unlock personalized productivity correlations."
                : cards.get(0).getInsightText();

        return HabitCorrelationResponse.builder()
                .headlineInsight(headline)
                .evaluatedDays(evalDays)
                .correlations(cards)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate currStart = today.with(DayOfWeek.MONDAY);
        LocalDate currEnd = currStart.plusDays(6);
        LocalDate prevStart = currStart.minusWeeks(1);
        LocalDate prevEnd = prevStart.plusDays(6);

        // 1. Total tasks completed
        long totalCompleted = taskRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, TaskStatus.COMPLETED).size();
        long currCompleted = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, currStart, currEnd).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long prevCompleted = taskRepository.findAllByUserIdAndDueDateBetweenAndDeletedAtIsNull(userId, prevStart, prevEnd).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        double taskChange = computeChange(currCompleted, prevCompleted);

        // 2. Active goals
        long activeGoals = goalRepository.countByUserIdAndStatusAndDeletedAtIsNull(userId, GoalStatus.IN_PROGRESS);

        // 3. Current day streak (max streak among habits)
        List<HabitEntity> habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        int maxStreak = habits.stream().mapToInt(HabitEntity::getCurrentStreak).max().orElse(0);

        // 4. Learning time
        long totalLearningMinutes = learningSessionRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .mapToLong(LearningSessionEntity::getDurationMinutes)
                .sum();
        long currLearningMins = learningSessionRepository.findAllByUserIdAndSessionDateBetweenAndDeletedAtIsNullOrderBySessionDateDesc(userId, currStart, currEnd).stream()
                .mapToLong(LearningSessionEntity::getDurationMinutes)
                .sum();
        long prevLearningMins = learningSessionRepository.findAllByUserIdAndSessionDateBetweenAndDeletedAtIsNullOrderBySessionDateDesc(userId, prevStart, prevEnd).stream()
                .mapToLong(LearningSessionEntity::getDurationMinutes)
                .sum();
        double learningChange = computeChange(currLearningMins, prevLearningMins);

        return UserStatsResponse.builder()
                .tasksCompletedTotal(totalCompleted)
                .tasksCompletedWeekChangePct(taskChange)
                .activeGoalsTotal(activeGoals)
                .activeGoalsWeekChangePct(0.0)
                .currentDayStreak(maxStreak)
                .streakWeekChangePct(0.0)
                .learningTimeMinutesTotal(totalLearningMinutes)
                .learningTimeWeekChangePct(learningChange)
                .build();
    }

    private WeekComparisonMetricDto buildMetric(String key, String label, BigDecimal curr, BigDecimal prev, boolean positiveWhenUp) {
        double change = 0.0;
        if (prev != null && prev.compareTo(BigDecimal.ZERO) > 0) {
            change = curr.subtract(prev)
                    .divide(prev, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (curr != null && curr.compareTo(BigDecimal.ZERO) > 0) {
            change = 100.0;
        }

        String trend = (change > 0) ? "UP" : (change < 0 ? "DOWN" : "STABLE");
        boolean isPositive = positiveWhenUp ? (change >= 0) : (change <= 0);

        return WeekComparisonMetricDto.builder()
                .metricKey(key)
                .label(label)
                .currentWeekValue(curr != null ? curr.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .previousWeekValue(prev != null ? prev.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .percentageChange(change)
                .trend(trend)
                .isPositiveImpact(isPositive)
                .build();
    }

    private double computeChange(long curr, long prev) {
        if (prev == 0) return curr > 0 ? 100.0 : 0.0;
        double ch = ((double) (curr - prev) / prev) * 100.0;
        return BigDecimal.valueOf(ch).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String normalizeCategory(String cat) {
        if (cat == null || cat.isBlank()) return "OTHERS";
        String upper = cat.trim().toUpperCase();
        if (CATEGORY_COLORS.containsKey(upper)) return upper;
        if (upper.contains("WORK") || upper.contains("JOB") || upper.contains("PROJECT")) return "WORK";
        if (upper.contains("LEARN") || upper.contains("STUDY") || upper.contains("BOOK")) return "LEARNING";
        if (upper.contains("HEALTH") || upper.contains("FIT") || upper.contains("MED") || upper.contains("GYM")) return "HEALTH";
        if (upper.contains("TRIP") || upper.contains("TRAVEL") || upper.contains("FLIGHT")) return "TRAVEL";
        if (upper.contains("HOME") || upper.contains("FAMILY") || upper.contains("PERSONAL")) return "PERSONAL";
        return "OTHERS";
    }

    private ZoneId getUserZoneId(UUID userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getTimezone)
                .map(tz -> {
                    try {
                        return ZoneId.of(tz);
                    } catch (Exception e) {
                        return ZoneId.of("Asia/Kolkata");
                    }
                })
                .orElse(ZoneId.of("Asia/Kolkata"));
    }

    private String formatHourLabel(int hour) {
        if (hour == 0) return "12 AM";
        if (hour < 12) return hour + " AM";
        if (hour == 12) return "12 PM";
        return (hour - 12) + " PM";
    }

    private String formatHourWindow(int hour) {
        int next = (hour + 1) % 24;
        return String.format("%02d:00 - %02d:00", hour, next);
    }
}
