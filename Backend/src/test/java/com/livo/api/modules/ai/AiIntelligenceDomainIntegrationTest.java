package com.livo.api.modules.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.ai.dto.AiChatRequest;
import com.livo.api.modules.ai.dto.AiFeedbackRequest;
import com.livo.api.modules.ai.entity.enums.AiFeedbackType;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyId;
import com.livo.api.modules.ai.repository.AiProposalRepository;
import com.livo.api.modules.ai.repository.AiUsageDailyRepository;
import com.livo.api.modules.ai.service.AiProposalService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiIntelligenceDomainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private HabitService habitService;

    @Autowired
    private AiProposalService aiProposalService;

    @Autowired
    private AiProposalRepository aiProposalRepository;

    @Autowired
    private AiUsageDailyRepository aiUsageDailyRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "ai_user_" + UUID.randomUUID();
        String email = "ai_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Taylor AI Pilot")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should send message, create conversation, and receive AI co-pilot response with suggestions")
    void testAiChatAndConversationThread() throws Exception {
        AiChatRequest chatReq = AiChatRequest.builder()
                .message("What should I focus on today?")
                .operatingMode("FOCUS")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.suggestedReplies.length()").value(3))
                .andReturn();

        String convId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("conversationId").asText();

        // Check conversation is listed
        mockMvc.perform(get("/api/v1/ai/conversations")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").isNumber());

        // Check messages in conversation (USER + ASSISTANT)
        mockMvc.perform(get("/api/v1/ai/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // Delete conversation thread
        mockMvc.perform(delete("/api/v1/ai/conversations/" + convId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conversation thread deleted successfully"));
    }

    @Test
    @DisplayName("Should generate proposal card, confirm action, audit log mutation, and revert action")
    void testProposalGenerationConfirmationAndRevert() throws Exception {
        // 1. Create a task due today
        var task = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Heavy Database Migration")
                .dueDate(LocalDate.now())
                .build());

        // 2. Ask AI to rebalance / reschedule
        AiChatRequest chatReq = AiChatRequest.builder()
                .message("Please rebalance and move task to another day")
                .build();

        MvcResult chatResult = mockMvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proposalId").isNotEmpty())
                .andExpect(jsonPath("$.data.cards.type").value("PROPOSAL_CARD"))
                .andReturn();

        String proposalId = objectMapper.readTree(chatResult.getResponse().getContentAsString())
                .path("data").path("proposalId").asText();

        // 3. Verify proposal is PENDING
        mockMvc.perform(get("/api/v1/ai/proposals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 4. Confirm proposal
        mockMvc.perform(post("/api/v1/ai/proposals/" + proposalId + "/confirm")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.confirmedAt").isNotEmpty());

        // Verify task was moved to tomorrow
        TaskEntity updatedTask = taskRepository.findByIdAndUserId(task.getId(), userId).orElseThrow();
        assertThat(updatedTask.getDueDate()).isEqualTo(LocalDate.now().plusDays(1));

        // 5. Verify AI audit log was recorded
        mockMvc.perform(get("/api/v1/ai/audit-logs")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].actionType").value("REBALANCE_TASK"))
                .andExpect(jsonPath("$.data[0].source").value("AI"));

        // 6. Revert proposal
        mockMvc.perform(post("/api/v1/ai/proposals/" + proposalId + "/revert")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVERTED"))
                .andExpect(jsonPath("$.data.revertedAt").isNotEmpty());

        // Verify task was reverted to today
        TaskEntity revertedTask = taskRepository.findByIdAndUserId(task.getId(), userId).orElseThrow();
        assertThat(revertedTask.getDueDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should refresh proactive recommendations and record user feedback")
    void testProactiveRecommendationsAndFeedback() throws Exception {
        // 1. Create an overdue task
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Submit Expense Report")
                .dueDate(LocalDate.now().minusDays(2))
                .build());

        // 2. Create a habit
        habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Morning Meditation")
                .frequencyType(HabitFrequency.DAILY)
                .build());

        // 3. Refresh recommendations
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/ai/recommendations/refresh")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn();

        String recId = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .path("data").get(0).path("id").asText();

        // 4. Query active recommendations
        mockMvc.perform(get("/api/v1/ai/recommendations")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());

        // 5. Submit feedback (ACCEPTED)
        AiFeedbackRequest feedback = AiFeedbackRequest.builder()
                .feedbackType(AiFeedbackType.ACCEPTED)
                .userNote("Handled immediately")
                .build();

        mockMvc.perform(post("/api/v1/ai/recommendations/" + recId + "/feedback")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(recId));
    }

    @Test
    @DisplayName("Should run comprehensive user state analysis with real-time completed habits")
    void testUserStateAnalysis() throws Exception {
        // Create 1 task
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Finish Design Specs")
                .dueDate(LocalDate.now())
                .build());

        // Create 2 habits, complete 1 today
        var habit1 = habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Morning Run")
                .frequencyType(HabitFrequency.DAILY)
                .build());
        habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Evening Reading")
                .frequencyType(HabitFrequency.DAILY)
                .build());

        habitService.logHabit(userId, habit1.getId(), LogHabitRequest.builder()
                .logDate(LocalDate.now())
                .countCompleted(1)
                .build());

        mockMvc.perform(post("/api/v1/ai/analyze")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workloadPercentage").isNumber())
                .andExpect(jsonPath("$.data.pendingTaskCount").value(1))
                .andExpect(jsonPath("$.data.habitsTotalToday").value(2))
                .andExpect(jsonPath("$.data.habitsCompletedToday").value(1))
                .andExpect(jsonPath("$.data.summaryHeadline").isNotEmpty())
                .andExpect(jsonPath("$.data.executiveSummary").isNotEmpty());
    }

    @Test
    @DisplayName("Should enforce strict multi-tenant isolation across users for AI data")
    void testMultiTenantAiIsolation() throws Exception {
        // User A creates conversation
        AiChatRequest chatReq = AiChatRequest.builder().message("User A Secret Chat").build();
        MvcResult result = mockMvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isOk())
                .andReturn();

        String convIdA = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("conversationId").asText();

        // User B
        String uidB = "ai_b_" + UUID.randomUUID();
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email("b_" + UUID.randomUUID() + "@livo.test")
                .fullName("User B")
                .timezone("UTC")
                .build());

        // User B cannot view User A's conversations
        mockMvc.perform(get("/api/v1/ai/conversations")
                        .header("Authorization", "Bearer " + authB.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // User B cannot access User A's conversation messages (returns 404)
        mockMvc.perform(get("/api/v1/ai/conversations/" + convIdA + "/messages")
                        .header("Authorization", "Bearer " + authB.getAccessToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject confirming expired proposal with HTTP 400")
    void testConfirmExpiredProposalRejected() throws Exception {
        // Create an already-expired proposal
        AiProposalEntity expiredProposal = aiProposalRepository.save(AiProposalEntity.builder()
                .userId(userId)
                .actionType("REBALANCE_TASK")
                .proposalPayload(Map.of("taskId", UUID.randomUUID().toString()))
                .status(AiProposalStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(3600)) // expired 1 hour ago
                .build());

        mockMvc.perform(post("/api/v1/ai/proposals/" + expiredProposal.getId() + "/confirm")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expired")));
    }

    @Test
    @DisplayName("Should reject confirming unsupported entity proposal and not execute false mutation")
    void testConfirmUnsupportedEntityProposalRejected() throws Exception {
        // Create a proposal for BUDGET
        AiProposalEntity budgetProposal = aiProposalRepository.save(AiProposalEntity.builder()
                .userId(userId)
                .actionType("ADJUST_BUDGET")
                .proposalPayload(Map.of("budgetId", UUID.randomUUID().toString(), "newLimit", "5000"))
                .status(AiProposalStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(86400))
                .build());

        mockMvc.perform(post("/api/v1/ai/proposals/" + budgetProposal.getId() + "/confirm")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("BUDGET")));

        // Proposal MUST NOT be marked CONFIRMED
        AiProposalEntity fresh = aiProposalRepository.findById(budgetProposal.getId()).orElseThrow();
        assertThat(fresh.getStatus()).isEqualTo(AiProposalStatus.PENDING);
    }

    @Test
    @DisplayName("Should return 404 when target task in proposal does not exist")
    void testConfirmProposalTaskNotFound() throws Exception {
        UUID nonExistentTaskId = UUID.randomUUID();
        AiProposalEntity proposal = aiProposalRepository.save(AiProposalEntity.builder()
                .userId(userId)
                .actionType("REBALANCE_TASK")
                .proposalPayload(Map.of("taskId", nonExistentTaskId.toString(), "newDueDate", LocalDate.now().plusDays(2).toString()))
                .status(AiProposalStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(86400))
                .build());

        mockMvc.perform(post("/api/v1/ai/proposals/" + proposal.getId() + "/confirm")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject reverting proposal for unsupported entity type")
    void testRevertUnsupportedEntityProposalRejected() throws Exception {
        AiProposalEntity eventProposal = aiProposalRepository.save(AiProposalEntity.builder()
                .userId(userId)
                .actionType("RESCHEDULE_EVENT")
                .proposalPayload(Map.of("eventId", UUID.randomUUID().toString()))
                .undoPayload(Map.of("eventId", UUID.randomUUID().toString()))
                .status(AiProposalStatus.CONFIRMED)
                .confirmedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400))
                .build());

        mockMvc.perform(post("/api/v1/ai/proposals/" + eventProposal.getId() + "/revert")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("EVENT")));
    }

    @Test
    @DisplayName("Should enforce daily AI message quota atomically when quota is exceeded")
    void testDailyQuotaEnforcementWhenQuotaExceeded() throws Exception {
        LocalDate today = LocalDate.now();
        // Seed usage record with 50 chats (quota is 50)
        aiUsageDailyRepository.saveAndFlush(AiUsageDailyEntity.builder()
                .id(new AiUsageDailyId(userId, today))
                .chatCount(50)
                .voiceCount(0)
                .build());

        AiChatRequest chatReq = AiChatRequest.builder()
                .message("Will this message be rejected by quota limit?")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Daily AI message quota exceeded")));

        // Verify count did not increment past 50
        var usage = aiUsageDailyRepository.findByIdUserIdAndIdUsageDate(userId, today).orElseThrow();
        assertThat(usage.getChatCount()).isEqualTo(50);
    }
}

