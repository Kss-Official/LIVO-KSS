package com.livo.api.modules.ai.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.ai.client.LivoAiClient;
import com.livo.api.modules.ai.dto.AiAnalysisResponse;
import com.livo.api.modules.ai.dto.AiChatRequest;
import com.livo.api.modules.ai.dto.AiChatResponse;
import com.livo.api.modules.ai.dto.AiConversationResponse;
import com.livo.api.modules.ai.dto.AiMessageResponse;
import com.livo.api.modules.ai.dto.AiRecommendationResponse;
import com.livo.api.modules.ai.entity.AiConversationEntity;
import com.livo.api.modules.ai.entity.AiMessageEntity;
import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyId;
import com.livo.api.modules.ai.entity.enums.AiMessageRole;
import com.livo.api.modules.ai.repository.AiConversationRepository;
import com.livo.api.modules.ai.repository.AiMessageRepository;
import com.livo.api.modules.ai.repository.AiUsageDailyRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;
    private final AiUsageDailyRepository aiUsageDailyRepository;
    private final AiContextService aiContextService;
    private final AiRecommendationService aiRecommendationService;
    private final AiProposalService aiProposalService;
    private final LivoAiClient livoAiClient;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final EventRepository eventRepository;

    @Value("${livo.ai.daily-quota:50}")
    private int dailyQuota;

    @Transactional
    public AiChatResponse sendMessage(UUID userId, AiChatRequest request) {
        LocalDate today = LocalDate.now();

        // 1. Atomic Quota Enforcement
        int rowsUpdated = aiUsageDailyRepository.incrementChatCountIfWithinQuota(userId, today, dailyQuota);
        if (rowsUpdated == 0) {
            throw new BadRequestException("Daily AI message quota exceeded (" + dailyQuota + " messages/day). Resets at midnight.");
        }

        // 2. Resolve or Create Conversation
        AiConversationEntity conversation;
        if (request.getConversationId() != null) {
            conversation = aiConversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + request.getConversationId()));
        } else {
            String initialTitle = request.getMessage().trim();
            if (initialTitle.length() > 40) {
                initialTitle = initialTitle.substring(0, 37) + "...";
            }
            conversation = AiConversationEntity.builder()
                    .userId(userId)
                    .title(initialTitle)
                    .build();
            conversation = aiConversationRepository.save(conversation);
        }

        // 3. Save User Message
        AiMessageEntity userMsg = AiMessageEntity.builder()
                .conversationId(conversation.getId())
                .userId(userId)
                .role(AiMessageRole.USER)
                .content(request.getMessage().trim())
                .operatingMode(request.getOperatingMode())
                .attachmentIds(request.getAttachmentIds())
                .build();
        aiMessageRepository.save(userMsg);

        // 4. Build Real-time Context & Fetch History
        String context = aiContextService.buildUserContext(userId);

        List<AiMessageEntity> historyEntities = aiMessageRepository.findAllByConversationIdAndUserIdOrderByCreatedAtAsc(conversation.getId(), userId);
        List<Map<String, String>> history = new ArrayList<>();
        for (AiMessageEntity m : historyEntities) {
            if (m.getId() != null && !m.getId().equals(userMsg.getId())) {
                history.add(Map.of("role", m.getRole().name().toLowerCase(), "content", m.getContent()));
            }
        }

        // 5. Generate Response via Client
        String assistantText = livoAiClient.generateChatResponse(context, request.getMessage(), history);

        // 6. Check for Controlled Action Proposal
        UUID proposalId = null;
        Map<String, Object> cards = new HashMap<>();
        String lowerMsg = request.getMessage().toLowerCase();

        if (lowerMsg.contains("rebalance") || lowerMsg.contains("reschedule") || lowerMsg.contains("move task")) {
            List<TaskEntity> pendingTasks = taskRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                    .toList();

            if (!pendingTasks.isEmpty()) {
                TaskEntity taskToMove = pendingTasks.get(0);
                LocalDate newDate = LocalDate.now().plusDays(1);
                LocalTime newTime = LocalTime.of(10, 0);

                Map<String, Object> proposalPayload = new HashMap<>();
                proposalPayload.put("taskId", taskToMove.getId().toString());
                proposalPayload.put("taskTitle", taskToMove.getTitle());
                proposalPayload.put("newDueDate", newDate.toString());
                proposalPayload.put("newDueTime", newTime.toString());

                Map<String, Object> undoPayload = new HashMap<>();
                if (taskToMove.getDueDate() != null) undoPayload.put("dueDate", taskToMove.getDueDate().toString());
                if (taskToMove.getDueTime() != null) undoPayload.put("dueTime", taskToMove.getDueTime().toString());
                undoPayload.put("status", taskToMove.getStatus().name());

                AiProposalEntity proposal = aiProposalService.createProposal(
                        userId, conversation.getId(), "REBALANCE_TASK", proposalPayload, undoPayload
                );
                proposalId = proposal.getId();

                cards.put("type", "PROPOSAL_CARD");
                cards.put("proposalId", proposalId.toString());
                cards.put("actionType", "REBALANCE_TASK");
                cards.put("title", "Proposed Action: Reschedule " + taskToMove.getTitle());
                cards.put("description", "Move from today to " + newDate + " at " + newTime);
            }
        }

        List<String> suggestedReplies = List.of(
                "Focus on Top Priority",
                "Plan My Afternoon",
                "Review Active Goals"
        );

        // 7. Save Assistant Message
        AiMessageEntity assistantMsg = AiMessageEntity.builder()
                .conversationId(conversation.getId())
                .userId(userId)
                .role(AiMessageRole.ASSISTANT)
                .content(assistantText)
                .operatingMode(request.getOperatingMode())
                .cardsJson(cards.isEmpty() ? null : cards)
                .suggestedRepliesJson(suggestedReplies)
                .build();
        assistantMsg = aiMessageRepository.save(assistantMsg);

        log.info("Processed AI chat message for user {} in conversation {}", userId, conversation.getId());

        int currentChatCount = aiUsageDailyRepository.findByIdUserIdAndIdUsageDate(userId, today)
                .map(AiUsageDailyEntity::getChatCount)
                .orElse(dailyQuota);

        return AiChatResponse.builder()
                .conversationId(conversation.getId())
                .messageId(assistantMsg.getId())
                .content(assistantMsg.getContent())
                .operatingMode(assistantMsg.getOperatingMode())
                .cards(cards.isEmpty() ? null : cards)
                .suggestedReplies(suggestedReplies)
                .proposalId(proposalId)
                .dailyUsageRemaining(Math.max(0, dailyQuota - currentChatCount))
                .createdAt(assistantMsg.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AiConversationResponse> getConversations(UUID userId) {
        return aiConversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(AiConversationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AiMessageResponse> getConversationMessages(UUID userId, UUID conversationId) {
        aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        return aiMessageRepository.findAllByConversationIdAndUserIdOrderByCreatedAtAsc(conversationId, userId).stream()
                .map(AiMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AiConversationResponse createConversation(UUID userId, String title) {
        AiConversationEntity conv = AiConversationEntity.builder()
                .userId(userId)
                .title((title != null && !title.isBlank()) ? title : "New Chat")
                .build();
        return AiConversationResponse.fromEntity(aiConversationRepository.save(conv));
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        AiConversationEntity conv = aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AiConversation", "id", conversationId));
        aiConversationRepository.delete(conv);
    }

    @Transactional
    public AiAnalysisResponse analyzeUserState(UUID userId) {
        LocalDate today = LocalDate.now();
        Instant dayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        List<TaskEntity> tasksToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(userId, today);
        long pendingTasks = tasksToday.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED).count();
        List<TaskEntity> allTasks = taskRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .count();

        List<HabitEntity> habits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(userId);
        List<HabitLogEntity> todayHabitLogs = habitLogRepository.findAllByUserIdAndLogDateAndDeletedAtIsNull(userId, today);
        Set<UUID> activeHabitIds = habits.stream().map(HabitEntity::getId).collect(Collectors.toSet());
        long habitsCompletedToday = todayHabitLogs.stream()
                .map(HabitLogEntity::getHabitId)
                .filter(activeHabitIds::contains)
                .distinct()
                .count();

        List<GoalEntity> goals = goalRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(userId, GoalStatus.IN_PROGRESS);
        List<EventEntity> events = eventRepository.findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(userId, dayStart, dayEnd);

        int workload = Math.min(100, (int) (pendingTasks * 20 + events.size() * 15));
        List<AiRecommendationResponse> recommendations = aiRecommendationService.refreshRecommendations(userId);

        String headline = (workload > 80) ? "High Workload Alert: Consider Day Rebalancing" : "Optimal Workload: High Probability of Goal Achievement";
        String summary = "You currently have " + pendingTasks + " pending tasks for today, " + overdueTasks + " overdue items, and " + habitsCompletedToday + "/" + habits.size() + " active habits completed. Workload is estimated at " + workload + "%.";

        return AiAnalysisResponse.builder()
                .workloadPercentage(workload)
                .pendingTaskCount(pendingTasks)
                .overdueTaskCount(overdueTasks)
                .habitsCompletedToday(habitsCompletedToday)
                .habitsTotalToday(habits.size())
                .activeGoalsCount(goals.size())
                .todayEventsCount(events.size())
                .summaryHeadline(headline)
                .executiveSummary(summary)
                .recommendations(recommendations)
                .build();
    }
}
