package com.livo.api.modules.goal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.ReorderMilestonesRequest;
import com.livo.api.modules.goal.dto.UpdateGoalProgressRequest;
import com.livo.api.modules.goal.dto.UpdateGoalRequest;
import com.livo.api.modules.goal.dto.UpdateMilestoneRequest;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import com.livo.api.modules.goal.repository.GoalProgressHistoryRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoalManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private GoalProgressHistoryRepository goalProgressHistoryRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "goal_user_" + UUID.randomUUID();
        String email = "goal_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Goal Manager")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should perform full CRUD and filtering on Goals")
    void testCreateAndQueryGoalsWithFiltering() throws Exception {
        // 1. Create Goal 1: System Design (PERCENTAGE)
        CreateGoalRequest goal1Req = CreateGoalRequest.builder()
                .title("Master System Design")
                .description("Prepare for Staff Engineer interview loop")
                .relatedArea("LEARNING")
                .targetDescription("Solve 50 large scale architecture problems")
                .category("CAREER")
                .priority(GoalPriority.HIGH)
                .targetDate(LocalDate.now().plusMonths(6))
                .progressTrackingType(GoalTrackingType.PERCENTAGE)
                .currentValue(BigDecimal.valueOf(25.00))
                .status(GoalStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goal1Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Master System Design"))
                .andExpect(jsonPath("$.data.progressPercentage").value(25.00));

        // 2. Create Goal 2: Half Marathon (NUMERICAL)
        CreateGoalRequest goal2Req = CreateGoalRequest.builder()
                .title("Run Half Marathon")
                .description("Train for 21.1 km race")
                .relatedArea("HEALTH")
                .category("FITNESS")
                .priority(GoalPriority.MEDIUM)
                .progressTrackingType(GoalTrackingType.NUMERICAL)
                .targetValue(BigDecimal.valueOf(21.10))
                .currentValue(BigDecimal.valueOf(5.275))
                .unit("km")
                .status(GoalStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goal2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Run Half Marathon"))
                .andExpect(jsonPath("$.data.progressPercentage").value(25.00)); // 5.275 / 21.1 = 25%

        // 3. Query all goals
        mockMvc.perform(get("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 4. Query with filter: relatedArea=HEALTH
        mockMvc.perform(get("/api/v1/goals")
                        .param("relatedArea", "HEALTH")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Run Half Marathon"));
    }

    @Test
    @DisplayName("Should manage Milestone lifecycle, reordering, and dynamic progress calculation")
    void testMilestonesLifecycleAndProgressCalculation() throws Exception {
        // 1. Create MILESTONE_BASED goal
        CreateGoalRequest goalReq = CreateGoalRequest.builder()
                .title("Launch Mobile App")
                .relatedArea("TASKS")
                .progressTrackingType(GoalTrackingType.MILESTONE_BASED)
                .status(GoalStatus.IN_PROGRESS)
                .build();

        MvcResult goalResult = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode goalNode = objectMapper.readTree(goalResult.getResponse().getContentAsString());
        UUID goalId = UUID.fromString(goalNode.path("data").path("id").asText());

        // 2. Add 3 milestones
        MvcResult m1Result = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Design UI Mockups")
                                .targetDate(LocalDate.now().plusWeeks(1))
                                .sortOrder(1)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m1Id = UUID.fromString(objectMapper.readTree(m1Result.getResponse().getContentAsString()).path("data").path("id").asText());

        MvcResult m2Result = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Develop Core Features")
                                .sortOrder(2)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m2Id = UUID.fromString(objectMapper.readTree(m2Result.getResponse().getContentAsString()).path("data").path("id").asText());

        MvcResult m3Result = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Deploy to Play Store")
                                .sortOrder(3)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m3Id = UUID.fromString(objectMapper.readTree(m3Result.getResponse().getContentAsString()).path("data").path("id").asText());

        // 3. Verify initial 0% progress on goal
        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.milestonesCount").value(3))
                .andExpect(jsonPath("$.data.completedMilestonesCount").value(0))
                .andExpect(jsonPath("$.data.progressPercentage").value(0.0));

        // 4. Toggle Milestone 1 complete -> expect 33.33% progress
        mockMvc.perform(patch("/api/v1/goals/" + goalId + "/milestones/" + m1Id + "/toggle")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedMilestonesCount").value(1))
                .andExpect(jsonPath("$.data.progressPercentage").value(33.33));

        // 5. Reorder Milestones (reverse order: m3, m2, m1)
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m3Id, m2Id, m1Id))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(m3Id.toString()))
                .andExpect(jsonPath("$.data[0].sortOrder").value(1));

        // 6. Delete Milestone 3 and Milestone 2, leaving only m1 (which is completed)
        mockMvc.perform(delete("/api/v1/goals/" + goalId + "/milestones/" + m3Id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/goals/" + goalId + "/milestones/" + m2Id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 7. Check goal: 1 milestone total, 1 completed -> 100.0% progress & status COMPLETED!
        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.milestonesCount").value(1))
                .andExpect(jsonPath("$.data.completedMilestonesCount").value(1))
                .andExpect(jsonPath("$.data.progressPercentage").value(100.0))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should reject partial or duplicate list in reorderMilestones with 400")
    void testReorderMilestonesRejectsPartialOrDuplicateList() throws Exception {
        // 1. Create MILESTONE_BASED goal
        CreateGoalRequest goalReq = CreateGoalRequest.builder()
                .title("Complete Backend Security Audit")
                .relatedArea("TASKS")
                .progressTrackingType(GoalTrackingType.MILESTONE_BASED)
                .status(GoalStatus.IN_PROGRESS)
                .build();

        MvcResult goalResult = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID goalId = UUID.fromString(objectMapper.readTree(goalResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Create 3 milestones
        MvcResult m1Res = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Phase 1 - Static Analysis")
                                .sortOrder(1)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m1Id = UUID.fromString(objectMapper.readTree(m1Res.getResponse().getContentAsString()).path("data").path("id").asText());

        MvcResult m2Res = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Phase 2 - Penetration Testing")
                                .sortOrder(2)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m2Id = UUID.fromString(objectMapper.readTree(m2Res.getResponse().getContentAsString()).path("data").path("id").asText());

        MvcResult m3Res = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Phase 3 - Remediation")
                                .sortOrder(3)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID m3Id = UUID.fromString(objectMapper.readTree(m3Res.getResponse().getContentAsString()).path("data").path("id").asText());

        // 3. Partial list (only 2 of 3 milestones) -> expect 400 Bad Request
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1Id, m2Id))
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Reorder list must contain all 3 milestones")));

        // 4. Duplicate ID list -> expect 400 Bad Request
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1Id, m1Id, m2Id))
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("duplicate")));

        // 5. Foreign/unknown ID list -> expect 400 Bad Request
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1Id, m2Id, UUID.randomUUID()))
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("complete permutation")));
    }

    @Test
    @DisplayName("Should track Numerical Goal progress updates and maintain audit history")
    void testNumericalGoalProgressTrackingAndHistory() throws Exception {
        // 1. Create Numerical Goal: Save $10,000
        CreateGoalRequest goalReq = CreateGoalRequest.builder()
                .title("Emergency Fund")
                .relatedArea("FINANCE")
                .progressTrackingType(GoalTrackingType.NUMERICAL)
                .targetValue(BigDecimal.valueOf(10000.00))
                .currentValue(BigDecimal.valueOf(2000.00))
                .unit("USD")
                .status(GoalStatus.IN_PROGRESS)
                .build();

        MvcResult goalResult = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID goalId = UUID.fromString(objectMapper.readTree(goalResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Update progress to $6,000
        UpdateGoalProgressRequest progress1 = UpdateGoalProgressRequest.builder()
                .currentValue(BigDecimal.valueOf(6000.00))
                .notes("Added freelance payout to emergency fund")
                .recordedDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/progress")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progress1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentValue").value(6000.00))
                .andExpect(jsonPath("$.data.progressPercentage").value(60.00))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 3. Update progress to $10,000 (reaching target) -> auto-completes
        UpdateGoalProgressRequest progress2 = UpdateGoalProgressRequest.builder()
                .currentValue(BigDecimal.valueOf(10000.00))
                .notes("Target met! Reached full $10,000 threshold")
                .recordedDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/progress")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progress2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentValue").value(10000.00))
                .andExpect(jsonPath("$.data.progressPercentage").value(100.00))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 4. Query Progress History
        mockMvc.perform(get("/api/v1/goals/" + goalId + "/history")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3)) // initial 2000 + update 6000 + update 10000
                .andExpect(jsonPath("$.data[0].newValue").value(10000.00))
                .andExpect(jsonPath("$.data[0].notes").value("Target met! Reached full $10,000 threshold"));
    }

    @Test
    @DisplayName("Should update goal details and soft-delete goal with cascading milestones")
    void testGoalUpdateAndSoftDelete() throws Exception {
        CreateGoalRequest goalReq = CreateGoalRequest.builder()
                .title("Learn Spanish")
                .relatedArea("LEARNING")
                .build();

        MvcResult goalResult = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID goalId = UUID.fromString(objectMapper.readTree(goalResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Add a milestone
        mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("Finish A1 Level")
                                .build())))
                .andExpect(status().isCreated());

        // Update goal
        UpdateGoalRequest updateReq = UpdateGoalRequest.builder()
                .title("Master Spanish Fluency")
                .description("Conversational fluency for summer trip")
                .priority(GoalPriority.URGENT)
                .build();

        mockMvc.perform(put("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Master Spanish Fluency"))
                .andExpect(jsonPath("$.data.priority").value("URGENT"));

        // Delete goal
        mockMvc.perform(delete("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Verify goal cannot be retrieved
        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // Verify milestone is also soft-deleted
        assertThat(milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should enforce validation rules and isolate goals between users")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: NUMERICAL goal without positive targetValue should fail
        CreateGoalRequest invalidNumerical = CreateGoalRequest.builder()
                .title("Invalid Numerical Goal")
                .progressTrackingType(GoalTrackingType.NUMERICAL)
                .targetValue(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNumerical)))
                .andExpect(status().isBadRequest());

        // 2. Create goal for user 1
        GoalEntity goal = GoalEntity.builder()
                .title("User 1 Secret Goal")
                .status(GoalStatus.IN_PROGRESS)
                .build();
        goal.setUserId(userId);
        goal.setVersion(1L);
        goal = goalRepository.save(goal);

        // 3. User 2 should NOT be able to access or modify User 1's goal
        String otherUid = "other_user_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other User")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/goals/" + goal.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/goals/" + goal.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
