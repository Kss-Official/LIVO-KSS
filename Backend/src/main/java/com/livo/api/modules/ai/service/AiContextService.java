package com.livo.api.modules.ai.service;

import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiContextService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public String buildUserContext(UUID userId) {
        LocalDate today = LocalDate.now();
        Instant dayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        List<TaskEntity> tasksToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(userId, today);
        long pendingTasks = tasksToday.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED).count();
        long completedTasks = tasksToday.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        List<TaskEntity> allTasks = taskRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .count();

        List<GoalEntity> activeGoals = goalRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, GoalStatus.IN_PROGRESS);
        List<HabitEntity> activeHabits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        List<HabitLogEntity> todayHabitLogs = habitLogRepository.findAllByUserIdAndLogDateAndDeletedAtIsNull(userId, today);
        Set<UUID> activeHabitIds = activeHabits.stream().map(HabitEntity::getId).collect(Collectors.toSet());
        long completedHabitsCount = todayHabitLogs.stream()
                .map(HabitLogEntity::getHabitId)
                .filter(activeHabitIds::contains)
                .distinct()
                .count();

        List<EventEntity> todayEvents = eventRepository.findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(userId, dayStart, dayEnd);

        StringBuilder sb = new StringBuilder();
        sb.append("You are LIVO AI, the intelligent personal Life OS advisor.\n");
        sb.append("Current System Date: ").append(today).append("\n\n");
        sb.append("--- USER STRUCTURED CONTEXT ---\n");
        sb.append("Tasks Scheduled Today: ").append(tasksToday.size())
                .append(" (Pending: ").append(pendingTasks).append(", Completed: ").append(completedTasks).append(")\n");
        sb.append("Overdue Tasks: ").append(overdueTasks).append("\n");
        sb.append("Active In-Progress Goals: ").append(activeGoals.size()).append("\n");
        sb.append("Daily Active Habits: ").append(activeHabits.size())
                .append(" (Completed Today: ").append(completedHabitsCount).append(")\n");
        sb.append("Calendar Events Today: ").append(todayEvents.size()).append("\n");

        if (!tasksToday.isEmpty()) {
            sb.append("\nTop Tasks For Today:\n");
            tasksToday.stream().limit(5).forEach(t -> sb.append("- [")
                    .append(t.getStatus()).append("] ")
                    .append(t.getTitle())
                    .append(" (Priority: ").append(t.getPriority()).append(")\n"));
        }

        if (!activeGoals.isEmpty()) {
            sb.append("\nActive Goals:\n");
            activeGoals.stream().limit(3).forEach(g -> sb.append("- ")
                    .append(g.getTitle())
                    .append(" (Target: ").append(g.getTargetDate()).append(")\n"));
        }

        sb.append("\nINSTRUCTIONS: Provide concise, high-impact, empathetic, and actionable advice. Use markdown headings and lists.");
        return sb.toString();
    }
}
