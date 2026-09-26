package com.livo.api.modules.ai.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.ai.dto.AiAuditLogResponse;
import com.livo.api.modules.ai.dto.AiProposalResponse;
import com.livo.api.modules.ai.entity.AiAuditLogEntity;
import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.enums.AiAuditSource;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
import com.livo.api.modules.ai.entity.enums.AiRelatedEntityType;
import com.livo.api.modules.ai.repository.AiAuditLogRepository;
import com.livo.api.modules.ai.repository.AiProposalRepository;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProposalService {

    private final AiProposalRepository aiProposalRepository;
    private final AiAuditLogRepository aiAuditLogRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public AiProposalEntity createProposal(UUID userId, UUID conversationId, String actionType, Map<String, Object> payload, Map<String, Object> undoPayload) {
        AiProposalEntity proposal = AiProposalEntity.builder()
                .userId(userId)
                .conversationId(conversationId)
                .actionType(actionType)
                .proposalPayload(payload != null ? payload : new HashMap<>())
                .undoPayload(undoPayload != null ? undoPayload : new HashMap<>())
                .status(AiProposalStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(86400 * 3)) // 3 days
                .build();

        return aiProposalRepository.save(proposal);
    }

    @Transactional(readOnly = true)
    public List<AiProposalResponse> getProposals(UUID userId, AiProposalStatus status) {
        List<AiProposalEntity> list = (status != null)
                ? aiProposalRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                : aiProposalRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return list.stream().map(AiProposalResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public AiProposalResponse confirmProposal(UUID userId, UUID proposalId) {
        AiProposalEntity proposal = aiProposalRepository.findByIdAndUserId(proposalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != AiProposalStatus.PENDING) {
            throw new BadRequestException("Proposal cannot be confirmed in status: " + proposal.getStatus());
        }

        if (proposal.getExpiresAt() != null && proposal.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Proposal has expired and can no longer be confirmed (expired at: " + proposal.getExpiresAt() + ")");
        }

        String actionType = proposal.getActionType();
        Map<String, Object> payload = proposal.getProposalPayload();
        AiRelatedEntityType entityType = resolveEntityType(actionType, payload);

        if (entityType == null) {
            throw new BadRequestException("Unknown or unsupported proposal action: " + actionType);
        }

        UUID affectedEntityId;
        switch (entityType) {
            case TASK -> affectedEntityId = applyTaskMutation(userId, proposal, payload);
            default -> throw new BadRequestException(
                    String.format("Automated execution for entity type '%s' (action: %s) is not currently supported", entityType, actionType)
            );
        }

        // Record AI audit log with genuine entity type and ID
        AiAuditLogEntity audit = AiAuditLogEntity.builder()
                .userId(userId)
                .proposalId(proposalId)
                .actionType(actionType)
                .entityType(entityType.name())
                .entityId(affectedEntityId)
                .oldValue(proposal.getUndoPayload())
                .newValue(proposal.getProposalPayload())
                .source(AiAuditSource.AI)
                .createdAt(Instant.now())
                .build();
        aiAuditLogRepository.save(audit);

        proposal.setStatus(AiProposalStatus.CONFIRMED);
        proposal.setConfirmedAt(Instant.now());
        AiProposalEntity saved = aiProposalRepository.save(proposal);

        log.info("Confirmed AI proposal {} ({}) for user {}", proposalId, actionType, userId);
        return AiProposalResponse.fromEntity(saved);
    }

    @Transactional
    public AiProposalResponse revertProposal(UUID userId, UUID proposalId) {
        AiProposalEntity proposal = aiProposalRepository.findByIdAndUserId(proposalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != AiProposalStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED proposals can be reverted");
        }

        Map<String, Object> undo = proposal.getUndoPayload();
        Map<String, Object> payload = proposal.getProposalPayload();
        String actionType = proposal.getActionType();
        AiRelatedEntityType entityType = resolveEntityType(actionType, payload);

        if (entityType == null) {
            throw new BadRequestException("Unknown or unsupported proposal action for revert: " + actionType);
        }

        if (undo == null || undo.isEmpty()) {
            throw new BadRequestException("Proposal cannot be reverted: undo payload is missing");
        }

        UUID affectedEntityId;
        switch (entityType) {
            case TASK -> affectedEntityId = revertTaskMutation(userId, payload, undo);
            default -> throw new BadRequestException(
                    String.format("Revert for entity type '%s' is not supported", entityType)
            );
        }

        AiAuditLogEntity audit = AiAuditLogEntity.builder()
                .userId(userId)
                .proposalId(proposalId)
                .actionType("REVERT_" + proposal.getActionType())
                .entityType(entityType.name())
                .entityId(affectedEntityId)
                .oldValue(payload)
                .newValue(undo)
                .source(AiAuditSource.USER)
                .createdAt(Instant.now())
                .build();
        aiAuditLogRepository.save(audit);

        proposal.setStatus(AiProposalStatus.REVERTED);
        proposal.setRevertedAt(Instant.now());
        AiProposalEntity saved = aiProposalRepository.save(proposal);

        log.info("Reverted AI proposal {} ({}) for user {}", proposalId, actionType, userId);
        return AiProposalResponse.fromEntity(saved);
    }

    @Transactional
    public AiProposalResponse rejectProposal(UUID userId, UUID proposalId) {
        AiProposalEntity proposal = aiProposalRepository.findByIdAndUserId(proposalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != AiProposalStatus.PENDING) {
            throw new BadRequestException("Only PENDING proposals can be rejected");
        }

        proposal.setStatus(AiProposalStatus.REJECTED);
        AiProposalEntity saved = aiProposalRepository.save(proposal);
        log.info("Rejected AI proposal {} for user {}", proposalId, userId);
        return AiProposalResponse.fromEntity(saved);
    }

    private UUID applyTaskMutation(UUID userId, AiProposalEntity proposal, Map<String, Object> payload) {
        if (!payload.containsKey("taskId")) {
            throw new BadRequestException("Proposal payload missing required 'taskId'");
        }

        UUID taskId;
        try {
            taskId = UUID.fromString(payload.get("taskId").toString());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid 'taskId' in proposal payload: " + payload.get("taskId"));
        }

        TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        // Store pre-mutation values in undo payload if not yet set
        if (proposal.getUndoPayload() == null || proposal.getUndoPayload().isEmpty()) {
            Map<String, Object> undo = new HashMap<>();
            if (task.getDueDate() != null) undo.put("dueDate", task.getDueDate().toString());
            if (task.getDueTime() != null) undo.put("dueTime", task.getDueTime().toString());
            undo.put("status", task.getStatus().name());
            proposal.setUndoPayload(undo);
        }

        // Apply mutation
        if (payload.containsKey("newDueDate")) {
            task.setDueDate(LocalDate.parse(payload.get("newDueDate").toString()));
        }
        if (payload.containsKey("newDueTime")) {
            task.setDueTime(LocalTime.parse(payload.get("newDueTime").toString()));
        }
        if (payload.containsKey("newStatus")) {
            task.setStatus(TaskStatus.valueOf(payload.get("newStatus").toString()));
        }

        taskRepository.save(task);
        return task.getId();
    }

    private UUID revertTaskMutation(UUID userId, Map<String, Object> payload, Map<String, Object> undo) {
        if (!payload.containsKey("taskId")) {
            throw new BadRequestException("Proposal payload missing required 'taskId'");
        }

        UUID taskId;
        try {
            taskId = UUID.fromString(payload.get("taskId").toString());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid 'taskId' in proposal payload: " + payload.get("taskId"));
        }

        TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (undo.containsKey("dueDate")) task.setDueDate(LocalDate.parse(undo.get("dueDate").toString()));
        if (undo.containsKey("dueTime")) task.setDueTime(LocalTime.parse(undo.get("dueTime").toString()));
        if (undo.containsKey("status")) task.setStatus(TaskStatus.valueOf(undo.get("status").toString()));
        taskRepository.save(task);

        return task.getId();
    }

    public AiRelatedEntityType resolveEntityType(String actionType, Map<String, Object> payload) {
        if (actionType != null) {
            String upper = actionType.toUpperCase();
            for (AiRelatedEntityType type : AiRelatedEntityType.values()) {
                if (upper.contains(type.name())) {
                    return type;
                }
            }
        }
        if (payload != null) {
            if (payload.containsKey("taskId")) return AiRelatedEntityType.TASK;
            if (payload.containsKey("eventId")) return AiRelatedEntityType.EVENT;
            if (payload.containsKey("goalId")) return AiRelatedEntityType.GOAL;
            if (payload.containsKey("habitId")) return AiRelatedEntityType.HABIT;
            if (payload.containsKey("learningId") || payload.containsKey("itemId")) return AiRelatedEntityType.LEARNING;
            if (payload.containsKey("transactionId")) return AiRelatedEntityType.TRANSACTION;
            if (payload.containsKey("budgetId")) return AiRelatedEntityType.BUDGET;
            if (payload.containsKey("tripId")) return AiRelatedEntityType.TRIP;
            if (payload.containsKey("healthId") || payload.containsKey("metricId")) return AiRelatedEntityType.HEALTH;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<AiAuditLogResponse> getAuditLogs(UUID userId, Pageable pageable) {
        Pageable effectivePageable = (pageable != null && pageable.isPaged())
                ? pageable
                : PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        return aiAuditLogRepository.findAllByUserId(userId, effectivePageable).stream()
                .map(AiAuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}


