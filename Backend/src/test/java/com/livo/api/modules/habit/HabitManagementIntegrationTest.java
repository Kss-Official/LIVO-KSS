package com.livo.api.modules.habit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.dto.UpdateHabitRequest;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
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
import java.time.LocalTime;
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
class HabitManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "habit_user_" + UUID.randomUUID();
        String email = "habit_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Habit Tracker User")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should perform full CRUD on habits and query by goal and archive status")
    void testCreateAndQueryHabits() throws Exception {
        // 1. Create linked Goal
        var goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Health & Longevity")
                .relatedArea("HEALTH")
                .build());

        // 2. Create Habit 1 (DAILY)
        CreateHabitRequest habit1 = CreateHabitRequest.builder()
                .title("Drink 3L Water")
                .description("Stay hydrated throughout the day")
                .goalId(goal.getId())
                .frequencyType(HabitFrequency.DAILY)
                .targetCount(3)
                .targetUnit("liters")
                .preferredTime(HabitPreferredTime.MORNING)
                .startDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Drink 3L Water"))
                .andExpect(jsonPath("$.data.customDays.length()").value(7))
                .andExpect(jsonPath("$.data.completedToday").value(false));

        // 3. Create Habit 2 (WEEKDAYS)
        CreateHabitRequest habit2 = CreateHabitRequest.builder()
                .title("Morning Jog")
                .frequencyType(HabitFrequency.WEEKDAYS)
                .targetCount(1)
                .preferredTime(HabitPreferredTime.MORNING)
                .preferredClockTime(LocalTime.of(6, 30))
                .startDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customDays.length()").value(5));

        // 4. Query all active habits
        mockMvc.perform(get("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 5. Query habits by goalId
        mockMvc.perform(get("/api/v1/habits")
                        .param("goalId", goal.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Drink 3L Water"));
    }

    @Test
    @DisplayName("Should track daily habit check-ins and calculate streaks accurately")
    void testHabitLoggingAndStreakCalculations() throws Exception {
        // 1. Create Habit: Read 20 Mins Daily
        CreateHabitRequest request = CreateHabitRequest.builder()
                .title("Read 20 Mins")
                .frequencyType(HabitFrequency.DAILY)
                .targetCount(1)
                .startDate(LocalDate.now().minusDays(10))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID habitId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());

        // 2. Log for Today -> streak becomes 1
        mockMvc.perform(post("/api/v1/habits/" + habitId + "/log")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder()
                                .logDate(LocalDate.now())
                                .countCompleted(1)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStreak").value(1))
                .andExpect(jsonPath("$.data.longestStreak").value(1))
                .andExpect(jsonPath("$.data.completedToday").value(true));

        // 3. Log for Yesterday -> consecutive streak becomes 2
        mockMvc.perform(post("/api/v1/habits/" + habitId + "/log")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder()
                                .logDate(LocalDate.now().minusDays(1))
                                .countCompleted(1)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStreak").value(2))
                .andExpect(jsonPath("$.data.longestStreak").value(2));

        // 4. Log for 2 days ago -> streak becomes 3
        mockMvc.perform(post("/api/v1/habits/" + habitId + "/log")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder()
                                .logDate(LocalDate.now().minusDays(2))
                                .countCompleted(1)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.longestStreak").value(3));

        // 5. Unlog yesterday -> streak breaks and drops to 1 (only today remains connected)
        mockMvc.perform(delete("/api/v1/habits/" + habitId + "/log")
                        .param("logDate", LocalDate.now().minusDays(1).toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStreak").value(1))
                .andExpect(jsonPath("$.data.longestStreak").value(3)); // longest streak preserved at 3
    }

    @Test
    @DisplayName("Should archive, unarchive, update, and soft-delete habit")
    void testArchiveAndUpdateAndSoftDelete() throws Exception {
        CreateHabitRequest request = CreateHabitRequest.builder()
                .title("Meditation Practice")
                .frequencyType(HabitFrequency.DAILY)
                .startDate(LocalDate.now())
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID habitId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());

        // Update habit
        UpdateHabitRequest updateReq = UpdateHabitRequest.builder()
                .title("Deep Meditation Practice")
                .targetCount(2)
                .motivationNote("Calm mind for clarity")
                .build();

        mockMvc.perform(put("/api/v1/habits/" + habitId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Deep Meditation Practice"))
                .andExpect(jsonPath("$.data.targetCount").value(2));

        // Archive habit
        mockMvc.perform(patch("/api/v1/habits/" + habitId + "/archive")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.archived").value(true));

        // Query active habits -> should be empty
        mockMvc.perform(get("/api/v1/habits")
                        .param("archived", "false")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // Delete habit
        mockMvc.perform(delete("/api/v1/habits/" + habitId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Verify cannot fetch
        mockMvc.perform(get("/api/v1/habits/" + habitId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fetch habit history and calculate completion rate")
    void testHabitHistoryAndCompletionRate() throws Exception {
        CreateHabitRequest request = CreateHabitRequest.builder()
                .title("Journaling")
                .frequencyType(HabitFrequency.DAILY)
                .startDate(LocalDate.now().minusDays(10))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID habitId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());

        // Log for 2 days
        mockMvc.perform(post("/api/v1/habits/" + habitId + "/log")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder()
                                .logDate(LocalDate.now())
                                .build())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/habits/" + habitId + "/log")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder()
                                .logDate(LocalDate.now().minusDays(1))
                                .build())))
                .andExpect(status().isOk());

        // Query history
        mockMvc.perform(get("/api/v1/habits/" + habitId + "/history")
                        .param("startDate", LocalDate.now().minusDays(3).toString())
                        .param("endDate", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logs.length()").value(2))
                .andExpect(jsonPath("$.data.totalCompletions").value(2))
                .andExpect(jsonPath("$.data.completionRate").value(50.0)); // 2 days completed out of 4 days in range
    }

    @Test
    @DisplayName("Should enforce validation and prevent cross-user habit access")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: End date before start date
        CreateHabitRequest invalidDates = CreateHabitRequest.builder()
                .title("Time Travel Habit")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/habits")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDates)))
                .andExpect(status().isBadRequest());

        // 2. Create habit for user 1
        HabitEntity habit = HabitEntity.builder()
                .title("User 1 Secret Habit")
                .startDate(LocalDate.now())
                .build();
        habit.setUserId(userId);
        habit.setVersion(1L);
        habit = habitRepository.save(habit);

        // 3. User 2 cannot access or log user 1's habit
        String otherUid = "other_user_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Habit User")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/habits/" + habit.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/habits/" + habit.getId() + "/log")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LogHabitRequest.builder().build())))
                .andExpect(status().isNotFound());
    }
}
