package com.livo.api.modules.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.CreateLearningResourceRequest;
import com.livo.api.modules.learning.dto.LogLearningSessionRequest;
import com.livo.api.modules.learning.dto.UpdateLearningItemRequest;
import com.livo.api.modules.learning.entity.LearningItemEntity;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningResourceType;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.repository.LearningItemRepository;
import com.livo.api.modules.learning.repository.LearningResourceRepository;
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

import java.time.LocalDate;
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
class LearningManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "learning_user_" + UUID.randomUUID();
        String email = "learn_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Lifelong Learner")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should perform CRUD on learning items and query with filters")
    void testCreateAndQueryLearningItems() throws Exception {
        // 1. Create linked Goal
        var goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Master Distributed Architecture")
                .relatedArea("LEARNING")
                .build());

        // 2. Create Learning Item 1: Distributed Systems
        CreateLearningItemRequest item1 = CreateLearningItemRequest.builder()
                .title("Distributed Systems Architecture")
                .description("In-depth study of consensus and sharding")
                .objective("Build fault-tolerant distributed services")
                .goalId(goal.getId())
                .learningType(LearningType.COURSE)
                .category("ENGINEERING")
                .difficultyLevel(DifficultyLevel.ADVANCED)
                .targetStudyTimeMinutes(45)
                .startDate(LocalDate.now())
                .targetCompletionDate(LocalDate.now().plusMonths(3))
                .status(LearningStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Distributed Systems Architecture"))
                .andExpect(jsonPath("$.data.difficultyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.data.category").value("ENGINEERING"));

        // 3. Create Learning Item 2: Clean Architecture Book
        CreateLearningItemRequest item2 = CreateLearningItemRequest.builder()
                .title("Clean Architecture Book")
                .learningType(LearningType.BOOK)
                .category("DESIGN")
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .targetStudyTimeMinutes(30)
                .startDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isCreated());

        // 4. Query all learning items
        mockMvc.perform(get("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 5. Query by goalId
        mockMvc.perform(get("/api/v1/learning/items")
                        .param("goalId", goal.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Distributed Systems Architecture"));
    }

    @Test
    @DisplayName("Should manage learning resources, toggle completion, and update resource metadata")
    void testLearningResourceLifecycle() throws Exception {
        CreateLearningItemRequest itemReq = CreateLearningItemRequest.builder()
                .title("Kubernetes Deep Dive")
                .learningType(LearningType.COURSE)
                .startDate(LocalDate.now())
                .build();

        MvcResult itemResult = mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID itemId = UUID.fromString(objectMapper.readTree(itemResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // 1. Add Resource 1 (Documentation)
        CreateLearningResourceRequest res1 = CreateLearningResourceRequest.builder()
                .title("Official Kubernetes Docs")
                .resourceType(LearningResourceType.WEBSITE)
                .url("https://kubernetes.io/docs/home/")
                .sortOrder(1)
                .build();

        MvcResult res1Result = mockMvc.perform(post("/api/v1/learning/items/" + itemId + "/resources")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(res1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Official Kubernetes Docs"))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andReturn();

        UUID res1Id = UUID.fromString(objectMapper.readTree(res1Result.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Add Resource 2 (Video)
        CreateLearningResourceRequest res2 = CreateLearningResourceRequest.builder()
                .title("CKA Exam Prep Course")
                .resourceType(LearningResourceType.VIDEO)
                .url("https://youtube.com/cka-prep")
                .sortOrder(2)
                .build();

        mockMvc.perform(post("/api/v1/learning/items/" + itemId + "/resources")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(res2)))
                .andExpect(status().isCreated());

        // 3. Toggle Resource 1 completed
        mockMvc.perform(patch("/api/v1/learning/items/" + itemId + "/resources/" + res1Id + "/toggle")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        // 4. Verify item includes resources count and completed count
        mockMvc.perform(get("/api/v1/learning/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resourcesCount").value(2))
                .andExpect(jsonPath("$.data.completedResourcesCount").value(1));

        // 5. Delete Resource 1
        mockMvc.perform(delete("/api/v1/learning/items/" + itemId + "/resources/" + res1Id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        assertThat(learningResourceRepository.findByIdAndLearningItemIdAndUserIdAndDeletedAtIsNull(res1Id, itemId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should track study sessions, update learning time, and aggregate stats")
    void testStudySessionsAndLearningStats() throws Exception {
        // 1. Create Learning Item
        CreateLearningItemRequest itemReq = CreateLearningItemRequest.builder()
                .title("Rust for Systems Programming")
                .learningType(LearningType.COURSE)
                .category("RUST")
                .startDate(LocalDate.now())
                .build();

        MvcResult itemResult = mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID itemId = UUID.fromString(objectMapper.readTree(itemResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Log Session 1 (45 mins)
        mockMvc.perform(post("/api/v1/learning/sessions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogLearningSessionRequest.builder()
                                .learningItemId(itemId)
                                .sessionDate(LocalDate.now())
                                .durationMinutes(45)
                                .notes("Memory safety, ownership, and borrowing")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.durationMinutes").value(45));

        // 3. Log Session 2 (75 mins)
        mockMvc.perform(post("/api/v1/learning/sessions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogLearningSessionRequest.builder()
                                .learningItemId(itemId)
                                .sessionDate(LocalDate.now().minusDays(1))
                                .durationMinutes(75)
                                .notes("Lifetimes and smart pointers")
                                .build())))
                .andExpect(status().isCreated());

        // 4. Fetch Item -> totalStudyTimeMinutes = 120 (2 hours)
        mockMvc.perform(get("/api/v1/learning/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudyTimeMinutes").value(120));

        // 5. Query Learning Stats
        mockMvc.perform(get("/api/v1/learning/stats")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalStudyMinutes").value(120))
                .andExpect(jsonPath("$.data.totalStudyHours").value(2.0))
                .andExpect(jsonPath("$.data.totalSessions").value(2))
                .andExpect(jsonPath("$.data.studyMinutesByCategory.RUST").value(120));
    }

    @Test
    @DisplayName("Should auto-complete item when reaching 100% progress and cascade soft-delete")
    void testProgressAutoCompleteAndSoftDelete() throws Exception {
        CreateLearningItemRequest itemReq = CreateLearningItemRequest.builder()
                .title("Algorithms Sprint")
                .progressPercentage((short) 50)
                .startDate(LocalDate.now())
                .build();

        MvcResult itemResult = mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID itemId = UUID.fromString(objectMapper.readTree(itemResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Update progress to 100% -> auto-completes status
        mockMvc.perform(put("/api/v1/learning/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateLearningItemRequest.builder()
                                .progressPercentage((short) 100)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercentage").value(100))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Delete item
        mockMvc.perform(delete("/api/v1/learning/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Fetch -> 404
        mockMvc.perform(get("/api/v1/learning/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should validate constraints and ensure multi-tenant user isolation")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: targetCompletionDate before startDate
        CreateLearningItemRequest invalidDates = CreateLearningItemRequest.builder()
                .title("Time Travel Course")
                .startDate(LocalDate.now().plusWeeks(2))
                .targetCompletionDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDates)))
                .andExpect(status().isBadRequest());

        // 2. Validation: session durationMinutes > 1440
        CreateLearningItemRequest validItem = CreateLearningItemRequest.builder()
                .title("Valid Item")
                .startDate(LocalDate.now())
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/learning/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID validItemId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());

        mockMvc.perform(post("/api/v1/learning/sessions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogLearningSessionRequest.builder()
                                .learningItemId(validItemId)
                                .durationMinutes(2000)
                                .build())))
                .andExpect(status().isBadRequest());

        // 3. User isolation
        String otherUid = "other_learn_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_learn_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Learner")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/learning/items/" + validItemId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/learning/items/" + validItemId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
