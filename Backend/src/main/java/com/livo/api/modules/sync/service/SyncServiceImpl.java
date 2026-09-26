package com.livo.api.modules.sync.service;

import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.sync.dto.MutationResultResponse;
import com.livo.api.modules.sync.dto.OfflineMutationItemRequest;
import com.livo.api.modules.sync.dto.OfflineMutationResponse;
import com.livo.api.modules.sync.dto.ResolveConflictRequest;
import com.livo.api.modules.sync.dto.SyncItemResponse;
import com.livo.api.modules.sync.dto.SyncPullResponse;
import com.livo.api.modules.sync.dto.SyncPushRequest;
import com.livo.api.modules.sync.dto.SyncPushResponse;
import com.livo.api.modules.sync.entity.OfflineMutationEntity;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
import com.livo.api.modules.sync.repository.OfflineMutationRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final OfflineMutationRepository offlineMutationRepository;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final EventRepository eventRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public SyncPushResponse pushMutations(UUID userId, SyncPushRequest request) {
        List<MutationResultResponse> results = new ArrayList<>();
        int appliedCount = 0;
        int conflictCount = 0;
        int rejectedCount = 0;

        for (OfflineMutationItemRequest mutation : request.getMutations()) {
            // 1. Idempotency Check per user
            Optional<OfflineMutationEntity> existing = offlineMutationRepository.findByUserIdAndIdempotencyKey(userId, mutation.getIdempotencyKey());
            if (existing.isPresent()) {
                OfflineMutationEntity entity = existing.get();
                log.info("Idempotent hit for mutation {} (user: {})", mutation.getIdempotencyKey(), userId);
                results.add(MutationResultResponse.builder()
                        .idempotencyKey(entity.getIdempotencyKey())
                        .domainEntity(entity.getDomainEntity())
                        .action(entity.getAction())
                        .status(entity.getStatus())
                        .entityId(entity.getEntityId())
                        .serverVersion(entity.getBaseVersion())
                        .message("Idempotent response: mutation was previously processed")
                        .appliedAt(entity.getAppliedAt())
                        .build());

                if (entity.getStatus() == OfflineMutationStatus.APPLIED) appliedCount++;
                else if (entity.getStatus() == OfflineMutationStatus.CONFLICT) conflictCount++;
                else rejectedCount++;
                continue;
            }

            // 2. Process new mutation
            MutationResultResponse result = processSingleMutation(userId, mutation);

            // 3. Persist mutation record in offline_mutations table
            OfflineMutationEntity entityToSave = OfflineMutationEntity.builder()
                    .userId(userId)
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity(mutation.getDomainEntity().toUpperCase())
                    .action(mutation.getAction().toUpperCase())
                    .payload(mutation.getPayload() != null ? mutation.getPayload() : new HashMap<>())
                    .clientTimestamp(mutation.getClientTimestamp() != null ? mutation.getClientTimestamp() : Instant.now())
                    .entityId(result.getEntityId())
                    .baseVersion(result.getServerVersion() != null ? result.getServerVersion() : mutation.getBaseVersion())
                    .status(result.getStatus())
                    .appliedAt(result.getAppliedAt() != null ? result.getAppliedAt() : Instant.now())
                    .build();

            offlineMutationRepository.save(entityToSave);
            results.add(result);

            if (result.getStatus() == OfflineMutationStatus.APPLIED) appliedCount++;
            else if (result.getStatus() == OfflineMutationStatus.CONFLICT) conflictCount++;
            else rejectedCount++;
        }

        return SyncPushResponse.builder()
                .results(results)
                .appliedCount(appliedCount)
                .conflictCount(conflictCount)
                .rejectedCount(rejectedCount)
                .serverTimestamp(Instant.now())
                .build();
    }

    private MutationResultResponse processSingleMutation(UUID userId, OfflineMutationItemRequest mutation) {
        String domain = mutation.getDomainEntity().toUpperCase();
        String action = mutation.getAction().toUpperCase();
        Map<String, Object> payload = mutation.getPayload() != null ? mutation.getPayload() : Map.of();

        try {
            switch (domain) {
                case "TASK":
                    return processTaskMutation(userId, action, mutation, payload);
                case "GOAL":
                    return processGoalMutation(userId, action, mutation, payload);
                case "HABIT":
                    return processHabitMutation(userId, action, mutation, payload);
                case "EVENT":
                    return processEventMutation(userId, action, mutation, payload);
                case "EXPENSE":
                case "TRANSACTION":
                    return processTransactionMutation(userId, action, mutation, payload);
                default:
                    return MutationResultResponse.builder()
                            .idempotencyKey(mutation.getIdempotencyKey())
                            .domainEntity(domain)
                            .action(action)
                            .status(OfflineMutationStatus.REJECTED)
                            .entityId(mutation.getEntityId())
                            .message("Domain '" + domain + "' is not supported for offline synchronization")
                            .appliedAt(Instant.now())
                            .build();
            }
        } catch (Exception e) {
            log.error("Failed to process mutation {} for user {}: {}", mutation.getIdempotencyKey(), userId, e.getMessage());
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity(domain)
                    .action(action)
                    .status(OfflineMutationStatus.REJECTED)
                    .entityId(mutation.getEntityId())
                    .message("Error: " + e.getMessage())
                    .appliedAt(Instant.now())
                    .build();
        }
    }

    private MutationResultResponse processTaskMutation(UUID userId, String action, OfflineMutationItemRequest mutation, Map<String, Object> payload) {
        if ("CREATE".equals(action)) {
            String title = payload.getOrDefault("title", "Untitled Task").toString();
            String description = payload.get("description") != null ? payload.get("description").toString() : null;
            TaskPriority priority = payload.get("priority") != null ? TaskPriority.valueOf(payload.get("priority").toString()) : TaskPriority.MEDIUM;
            TaskStatus status = payload.get("status") != null ? TaskStatus.valueOf(payload.get("status").toString()) : TaskStatus.TODO;
            LocalDate dueDate = payload.get("dueDate") != null ? LocalDate.parse(payload.get("dueDate").toString()) : null;

            TaskEntity task = TaskEntity.builder()
                    .title(title)
                    .description(description)
                    .priority(priority)
                    .status(status)
                    .dueDate(dueDate)
                    .build();

            task.setUserId(userId);
            task.setVersion(1L);
            if (mutation.getEntityId() != null) {
                task.setId(mutation.getEntityId());
            }

            TaskEntity saved = taskRepository.save(task);
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("TASK")
                    .action(action)
                    .status(OfflineMutationStatus.APPLIED)
                    .entityId(saved.getId())
                    .serverVersion(saved.getVersion())
                    .message("Task created successfully")
                    .appliedAt(Instant.now())
                    .build();
        }

        if (mutation.getEntityId() == null) {
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("TASK")
                    .action(action)
                    .status(OfflineMutationStatus.REJECTED)
                    .message("entityId is required for " + action)
                    .appliedAt(Instant.now())
                    .build();
        }

        Optional<TaskEntity> opt = taskRepository.findByIdAndUserId(mutation.getEntityId(), userId);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("TASK")
                    .action(action)
                    .status(OfflineMutationStatus.REJECTED)
                    .entityId(mutation.getEntityId())
                    .message("Task not found or deleted")
                    .appliedAt(Instant.now())
                    .build();
        }

        TaskEntity task = opt.get();
        // Optimistic concurrency check
        if (mutation.getBaseVersion() != null && task.getVersion() > mutation.getBaseVersion()) {
            Map<String, Object> conflictDetails = new HashMap<>();
            conflictDetails.put("serverVersion", task.getVersion());
            conflictDetails.put("clientBaseVersion", mutation.getBaseVersion());
            conflictDetails.put("serverTitle", task.getTitle());
            conflictDetails.put("serverStatus", task.getStatus().name());

            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("TASK")
                    .action(action)
                    .status(OfflineMutationStatus.CONFLICT)
                    .entityId(task.getId())
                    .serverVersion(task.getVersion())
                    .conflictDetails(conflictDetails)
                    .message("Conflict: Server is at version " + task.getVersion() + ", client baseVersion was " + mutation.getBaseVersion())
                    .appliedAt(Instant.now())
                    .build();
        }

        if ("DELETE".equals(action)) {
            task.markDeleted();
            TaskEntity saved = taskRepository.save(task);
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("TASK")
                    .action(action)
                    .status(OfflineMutationStatus.APPLIED)
                    .entityId(saved.getId())
                    .serverVersion(saved.getVersion())
                    .message("Task deleted successfully")
                    .appliedAt(Instant.now())
                    .build();
        }

        // UPDATE
        if (payload.containsKey("title")) task.setTitle(payload.get("title").toString());
        if (payload.containsKey("description")) task.setDescription(payload.get("description") != null ? payload.get("description").toString() : null);
        if (payload.containsKey("priority")) task.setPriority(TaskPriority.valueOf(payload.get("priority").toString()));
        if (payload.containsKey("status")) task.setStatus(TaskStatus.valueOf(payload.get("status").toString()));
        if (payload.containsKey("dueDate")) task.setDueDate(payload.get("dueDate") != null ? LocalDate.parse(payload.get("dueDate").toString()) : null);

        TaskEntity updated = taskRepository.save(task);

        return MutationResultResponse.builder()
                .idempotencyKey(mutation.getIdempotencyKey())
                .domainEntity("TASK")
                .action(action)
                .status(OfflineMutationStatus.APPLIED)
                .entityId(updated.getId())
                .serverVersion(updated.getVersion())
                .message("Task updated successfully")
                .appliedAt(Instant.now())
                .build();
    }

    private MutationResultResponse processGoalMutation(UUID userId, String action, OfflineMutationItemRequest mutation, Map<String, Object> payload) {
        if ("CREATE".equals(action)) {
            String title = payload.getOrDefault("title", "Untitled Goal").toString();
            GoalTrackingType type = payload.get("trackingType") != null ? GoalTrackingType.valueOf(payload.get("trackingType").toString()) : GoalTrackingType.PERCENTAGE;
            LocalDate targetDate = payload.get("targetDate") != null ? LocalDate.parse(payload.get("targetDate").toString()) : LocalDate.now().plusMonths(3);

            GoalEntity goal = GoalEntity.builder()
                    .title(title)
                    .progressTrackingType(type)
                    .targetDate(targetDate)
                    .status(GoalStatus.NOT_STARTED)
                    .build();
            goal.setUserId(userId);
            goal.setVersion(1L);
            if (mutation.getEntityId() != null) goal.setId(mutation.getEntityId());

            GoalEntity saved = goalRepository.save(goal);
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("GOAL")
                    .action(action)
                    .status(OfflineMutationStatus.APPLIED)
                    .entityId(saved.getId())
                    .serverVersion(saved.getVersion())
                    .message("Goal created successfully")
                    .appliedAt(Instant.now())
                    .build();
        }

        if (mutation.getEntityId() == null) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("entityId required").build();
        }

        Optional<GoalEntity> opt = goalRepository.findByIdAndUserId(mutation.getEntityId(), userId);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("Goal not found").build();
        }

        GoalEntity goal = opt.get();
        if (mutation.getBaseVersion() != null && goal.getVersion() > mutation.getBaseVersion()) {
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity("GOAL")
                    .action(action)
                    .status(OfflineMutationStatus.CONFLICT)
                    .entityId(goal.getId())
                    .serverVersion(goal.getVersion())
                    .message("Conflict: Server is at version " + goal.getVersion())
                    .appliedAt(Instant.now())
                    .build();
        }

        if ("DELETE".equals(action)) {
            goal.markDeleted();
            goalRepository.save(goal);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(goal.getId()).serverVersion(goal.getVersion()).message("Goal deleted").appliedAt(Instant.now()).build();
        }

        if (payload.containsKey("title")) goal.setTitle(payload.get("title").toString());
        if (payload.containsKey("status")) goal.setStatus(GoalStatus.valueOf(payload.get("status").toString()));
        goalRepository.save(goal);
        return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(goal.getId()).serverVersion(goal.getVersion()).message("Goal updated").appliedAt(Instant.now()).build();
    }

    private MutationResultResponse processHabitMutation(UUID userId, String action, OfflineMutationItemRequest mutation, Map<String, Object> payload) {
        if ("CREATE".equals(action)) {
            String title = payload.getOrDefault("title", "Daily Habit").toString();
            HabitFrequency freq = payload.get("frequency") != null ? HabitFrequency.valueOf(payload.get("frequency").toString()) : HabitFrequency.DAILY;
            HabitEntity habit = HabitEntity.builder()
                    .title(title)
                    .frequencyType(freq)
                    .build();
            habit.setUserId(userId);
            habit.setVersion(1L);
            if (mutation.getEntityId() != null) habit.setId(mutation.getEntityId());

            HabitEntity saved = habitRepository.save(habit);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("HABIT").action(action).status(OfflineMutationStatus.APPLIED).entityId(saved.getId()).serverVersion(saved.getVersion()).message("Habit created").appliedAt(Instant.now()).build();
        }

        if (mutation.getEntityId() == null) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("entityId required").build();
        }

        Optional<HabitEntity> opt = habitRepository.findByIdAndUserId(mutation.getEntityId(), userId);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("Habit not found").build();
        }

        HabitEntity habit = opt.get();
        if (mutation.getBaseVersion() != null && habit.getVersion() > mutation.getBaseVersion()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("HABIT").status(OfflineMutationStatus.CONFLICT).entityId(habit.getId()).serverVersion(habit.getVersion()).message("Conflict").appliedAt(Instant.now()).build();
        }

        if ("DELETE".equals(action)) {
            habit.markDeleted();
            habitRepository.save(habit);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(habit.getId()).serverVersion(habit.getVersion()).message("Habit deleted").appliedAt(Instant.now()).build();
        }

        if (payload.containsKey("title")) habit.setTitle(payload.get("title").toString());
        habitRepository.save(habit);
        return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(habit.getId()).serverVersion(habit.getVersion()).message("Habit updated").appliedAt(Instant.now()).build();
    }

    private MutationResultResponse processEventMutation(UUID userId, String action, OfflineMutationItemRequest mutation, Map<String, Object> payload) {
        if ("CREATE".equals(action)) {
            String title = payload.getOrDefault("title", "New Event").toString();
            Instant start = payload.get("startTime") != null ? Instant.parse(payload.get("startTime").toString()) : Instant.now().plusSeconds(3600);
            Instant end = payload.get("endTime") != null ? Instant.parse(payload.get("endTime").toString()) : start.plusSeconds(3600);

            EventEntity event = EventEntity.builder()
                    .title(title)
                    .startTime(start)
                    .endTime(end)
                    .format(EventFormat.IN_PERSON)
                    .priority(EventPriority.MEDIUM)
                    .build();
            event.setUserId(userId);
            event.setVersion(1L);
            if (mutation.getEntityId() != null) event.setId(mutation.getEntityId());

            EventEntity saved = eventRepository.save(event);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("EVENT").action(action).status(OfflineMutationStatus.APPLIED).entityId(saved.getId()).serverVersion(saved.getVersion()).message("Event created").appliedAt(Instant.now()).build();
        }

        if (mutation.getEntityId() == null) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("entityId required").build();
        }

        Optional<EventEntity> opt = eventRepository.findByIdAndUserId(mutation.getEntityId(), userId);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("Event not found").build();
        }

        EventEntity event = opt.get();
        if (mutation.getBaseVersion() != null && event.getVersion() > mutation.getBaseVersion()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("EVENT").status(OfflineMutationStatus.CONFLICT).entityId(event.getId()).serverVersion(event.getVersion()).message("Conflict").appliedAt(Instant.now()).build();
        }

        if ("DELETE".equals(action)) {
            event.markDeleted();
            eventRepository.save(event);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(event.getId()).serverVersion(event.getVersion()).message("Event deleted").appliedAt(Instant.now()).build();
        }

        if (payload.containsKey("title")) event.setTitle(payload.get("title").toString());
        eventRepository.save(event);
        return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(event.getId()).serverVersion(event.getVersion()).message("Event updated").appliedAt(Instant.now()).build();
    }

    private MutationResultResponse processTransactionMutation(UUID userId, String action, OfflineMutationItemRequest mutation, Map<String, Object> payload) {
        if ("CREATE".equals(action)) {
            String title = payload.getOrDefault("title", "Expense Item").toString();
            BigDecimal amount = payload.get("amount") != null ? new BigDecimal(payload.get("amount").toString()) : BigDecimal.ZERO;
            TransactionType type = payload.get("type") != null ? TransactionType.valueOf(payload.get("type").toString()) : TransactionType.EXPENSE;
            PaymentMethod method = payload.get("paymentMethod") != null ? PaymentMethod.valueOf(payload.get("paymentMethod").toString()) : PaymentMethod.UPI;

            TransactionEntity tx = TransactionEntity.builder()
                    .title(title)
                    .amount(amount)
                    .currency(payload.getOrDefault("currency", "INR").toString())
                    .type(type)
                    .paymentMethod(method)
                    .category(payload.getOrDefault("category", "General").toString())
                    .transactionDate(LocalDate.now())
                    .transactionTime(LocalTime.now())
                    .build();
            tx.setUserId(userId);
            tx.setVersion(1L);
            if (mutation.getEntityId() != null) tx.setId(mutation.getEntityId());

            TransactionEntity saved = transactionRepository.save(tx);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("EXPENSE").action(action).status(OfflineMutationStatus.APPLIED).entityId(saved.getId()).serverVersion(saved.getVersion()).message("Expense created").appliedAt(Instant.now()).build();
        }

        if (mutation.getEntityId() == null) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("entityId required").build();
        }

        Optional<TransactionEntity> opt = transactionRepository.findByIdAndUserId(mutation.getEntityId(), userId);
        if (opt.isEmpty() || opt.get().isDeleted()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.REJECTED).message("Transaction not found").build();
        }

        TransactionEntity tx = opt.get();
        if (mutation.getBaseVersion() != null && tx.getVersion() > mutation.getBaseVersion()) {
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).domainEntity("EXPENSE").status(OfflineMutationStatus.CONFLICT).entityId(tx.getId()).serverVersion(tx.getVersion()).message("Conflict").appliedAt(Instant.now()).build();
        }

        if ("DELETE".equals(action)) {
            tx.markDeleted();
            transactionRepository.save(tx);
            return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(tx.getId()).serverVersion(tx.getVersion()).message("Transaction deleted").appliedAt(Instant.now()).build();
        }

        if (payload.containsKey("title")) tx.setTitle(payload.get("title").toString());
        if (payload.containsKey("amount")) tx.setAmount(new BigDecimal(payload.get("amount").toString()));
        transactionRepository.save(tx);
        return MutationResultResponse.builder().idempotencyKey(mutation.getIdempotencyKey()).status(OfflineMutationStatus.APPLIED).entityId(tx.getId()).serverVersion(tx.getVersion()).message("Transaction updated").appliedAt(Instant.now()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SyncPullResponse pullChanges(UUID userId, Instant since) {
        Instant effectiveSince = (since != null) ? since : Instant.EPOCH;

        // 1. Fetch modified/created/deleted items after effectiveSince
        List<SyncItemResponse> taskItems = taskRepository.findAllByUserIdAndUpdatedAtAfter(userId, effectiveSince).stream()
                .map(t -> SyncItemResponse.builder()
                        .id(t.getId())
                        .domainEntity("TASK")
                        .version(t.getVersion())
                        .deleted(t.isDeleted())
                        .deletedAt(t.getDeletedAt())
                        .updatedAt(t.getUpdatedAt())
                        .data(Map.of(
                                "title", t.getTitle(),
                                "status", t.getStatus() != null ? t.getStatus().name() : "TODO",
                                "priority", t.getPriority() != null ? t.getPriority().name() : "MEDIUM"
                        ))
                        .build())
                .collect(Collectors.toList());

        List<SyncItemResponse> goalItems = goalRepository.findAllByUserIdAndUpdatedAtAfter(userId, effectiveSince).stream()
                .map(g -> SyncItemResponse.builder()
                        .id(g.getId())
                        .domainEntity("GOAL")
                        .version(g.getVersion())
                        .deleted(g.isDeleted())
                        .deletedAt(g.getDeletedAt())
                        .updatedAt(g.getUpdatedAt())
                        .data(Map.of(
                                "title", g.getTitle(),
                                "status", g.getStatus() != null ? g.getStatus().name() : "NOT_STARTED"
                        ))
                        .build())
                .collect(Collectors.toList());

        List<SyncItemResponse> habitItems = habitRepository.findAllByUserIdAndUpdatedAtAfter(userId, effectiveSince).stream()
                .map(h -> SyncItemResponse.builder()
                        .id(h.getId())
                        .domainEntity("HABIT")
                        .version(h.getVersion())
                        .deleted(h.isDeleted())
                        .deletedAt(h.getDeletedAt())
                        .updatedAt(h.getUpdatedAt())
                        .data(Map.of(
                                "title", h.getTitle(),
                                "frequency", h.getFrequencyType() != null ? h.getFrequencyType().name() : "DAILY"
                        ))
                        .build())
                .collect(Collectors.toList());

        List<SyncItemResponse> eventItems = eventRepository.findAllByUserIdAndUpdatedAtAfter(userId, effectiveSince).stream()
                .map(e -> SyncItemResponse.builder()
                        .id(e.getId())
                        .domainEntity("EVENT")
                        .version(e.getVersion())
                        .deleted(e.isDeleted())
                        .deletedAt(e.getDeletedAt())
                        .updatedAt(e.getUpdatedAt())
                        .data(Map.of(
                                "title", e.getTitle(),
                                "startTime", e.getStartTime().toString(),
                                "endTime", e.getEndTime().toString()
                        ))
                        .build())
                .collect(Collectors.toList());

        List<SyncItemResponse> expenseItems = transactionRepository.findAllByUserIdAndUpdatedAtAfter(userId, effectiveSince).stream()
                .map(x -> SyncItemResponse.builder()
                        .id(x.getId())
                        .domainEntity("EXPENSE")
                        .version(x.getVersion())
                        .deleted(x.isDeleted())
                        .deletedAt(x.getDeletedAt())
                        .updatedAt(x.getUpdatedAt())
                        .data(Map.of(
                                "title", x.getTitle(),
                                "amount", x.getAmount(),
                                "currency", x.getCurrency()
                        ))
                        .build())
                .collect(Collectors.toList());

        int total = taskItems.size() + goalItems.size() + habitItems.size() + eventItems.size() + expenseItems.size();

        return SyncPullResponse.builder()
                .serverTimestamp(Instant.now())
                .since(effectiveSince)
                .tasks(taskItems)
                .goals(goalItems)
                .habits(habitItems)
                .events(eventItems)
                .expenses(expenseItems)
                .totalChanges(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfflineMutationResponse> getMutations(UUID userId, OfflineMutationStatus status) {
        List<OfflineMutationEntity> list;
        if (status != null) {
            list = offlineMutationRepository.findAllByUserIdAndStatusOrderByAppliedAtDesc(userId, status);
        } else {
            list = offlineMutationRepository.findAllByUserIdOrderByAppliedAtDesc(userId);
        }

        return list.stream()
                .map(OfflineMutationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MutationResultResponse resolveConflict(UUID userId, ResolveConflictRequest request) {
        OfflineMutationEntity mutation = offlineMutationRepository.findByUserIdAndIdempotencyKey(userId, request.getIdempotencyKey())
                .orElseThrow(() -> new ResourceNotFoundException("Mutation not found with idempotency key: " + request.getIdempotencyKey()));

        String strategy = request.getStrategy().toUpperCase();

        if ("SERVER_WINS".equals(strategy)) {
            mutation.setStatus(OfflineMutationStatus.REJECTED);
            offlineMutationRepository.save(mutation);
            return MutationResultResponse.builder()
                    .idempotencyKey(mutation.getIdempotencyKey())
                    .domainEntity(mutation.getDomainEntity())
                    .action(mutation.getAction())
                    .status(OfflineMutationStatus.REJECTED)
                    .entityId(mutation.getEntityId())
                    .serverVersion(mutation.getBaseVersion())
                    .message("Resolved: Server version retained, offline change discarded")
                    .appliedAt(Instant.now())
                    .build();
        }

        // CLIENT_WINS or MERGE
        Map<String, Object> finalPayload = (request.getResolvedPayload() != null && !request.getResolvedPayload().isEmpty())
                ? request.getResolvedPayload()
                : mutation.getPayload();

        String domain = mutation.getDomainEntity() != null ? mutation.getDomainEntity().toUpperCase() : "";

        if (mutation.getEntityId() != null) {
            switch (domain) {
                case "TASK":
                    taskRepository.findByIdAndUserId(mutation.getEntityId(), userId).ifPresent(task -> {
                        if (finalPayload.containsKey("title")) task.setTitle(finalPayload.get("title").toString());
                        if (finalPayload.containsKey("description")) task.setDescription(finalPayload.get("description") != null ? finalPayload.get("description").toString() : null);
                        if (finalPayload.containsKey("priority")) task.setPriority(TaskPriority.valueOf(finalPayload.get("priority").toString()));
                        if (finalPayload.containsKey("status")) task.setStatus(TaskStatus.valueOf(finalPayload.get("status").toString()));
                        if (finalPayload.containsKey("dueDate")) task.setDueDate(finalPayload.get("dueDate") != null ? LocalDate.parse(finalPayload.get("dueDate").toString()) : null);
                        taskRepository.save(task);
                    });
                    break;
                case "GOAL":
                    goalRepository.findByIdAndUserId(mutation.getEntityId(), userId).ifPresent(goal -> {
                        if (finalPayload.containsKey("title")) goal.setTitle(finalPayload.get("title").toString());
                        if (finalPayload.containsKey("status")) goal.setStatus(GoalStatus.valueOf(finalPayload.get("status").toString()));
                        goalRepository.save(goal);
                    });
                    break;
                case "HABIT":
                    habitRepository.findByIdAndUserId(mutation.getEntityId(), userId).ifPresent(habit -> {
                        if (finalPayload.containsKey("title")) habit.setTitle(finalPayload.get("title").toString());
                        if (finalPayload.containsKey("frequency")) habit.setFrequencyType(HabitFrequency.valueOf(finalPayload.get("frequency").toString()));
                        habitRepository.save(habit);
                    });
                    break;
                case "EVENT":
                    eventRepository.findByIdAndUserId(mutation.getEntityId(), userId).ifPresent(event -> {
                        if (finalPayload.containsKey("title")) event.setTitle(finalPayload.get("title").toString());
                        if (finalPayload.containsKey("startTime")) event.setStartTime(Instant.parse(finalPayload.get("startTime").toString()));
                        if (finalPayload.containsKey("endTime")) event.setEndTime(Instant.parse(finalPayload.get("endTime").toString()));
                        eventRepository.save(event);
                    });
                    break;
                case "EXPENSE":
                case "TRANSACTION":
                    transactionRepository.findByIdAndUserId(mutation.getEntityId(), userId).ifPresent(tx -> {
                        if (finalPayload.containsKey("title")) tx.setTitle(finalPayload.get("title").toString());
                        if (finalPayload.containsKey("amount")) tx.setAmount(new BigDecimal(finalPayload.get("amount").toString()));
                        if (finalPayload.containsKey("category")) tx.setCategory(finalPayload.get("category").toString());
                        transactionRepository.save(tx);
                    });
                    break;
                default:
                    log.warn("Conflict resolution requested for unhandled domain: {}", domain);
                    break;
            }
        }

        mutation.setStatus(OfflineMutationStatus.APPLIED);
        offlineMutationRepository.save(mutation);

        return MutationResultResponse.builder()
                .idempotencyKey(mutation.getIdempotencyKey())
                .domainEntity(mutation.getDomainEntity())
                .action(mutation.getAction())
                .status(OfflineMutationStatus.APPLIED)
                .entityId(mutation.getEntityId())
                .serverVersion(mutation.getBaseVersion() != null ? mutation.getBaseVersion() + 1 : 2L)
                .message("Resolved: Client/merged version force-applied")
                .appliedAt(Instant.now())
                .build();
    }
}
