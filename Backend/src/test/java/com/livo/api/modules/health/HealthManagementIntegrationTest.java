package com.livo.api.modules.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.health.dto.CreateHealthEntryRequest;
import com.livo.api.modules.health.dto.UpdateHealthEntryRequest;
import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
import com.livo.api.modules.health.repository.HealthEntryRepository;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private HealthEntryRepository healthEntryRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "health_user_" + UUID.randomUUID();
        String email = "health_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Healthy Athlete")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should create health entries, link to goal, and query with filters")
    void testCreateAndQueryHealthEntries() throws Exception {
        // 1. Create Goal
        var goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Run a Half Marathon")
                .relatedArea("HEALTH")
                .build());

        // 2. Create Workout entry
        CreateHealthEntryRequest workout = CreateHealthEntryRequest.builder()
                .goalId(goal.getId())
                .title("Morning Tempo Run")
                .description("Interval training at 5:15 pace")
                .healthType(HealthType.WORKOUT)
                .entryDate(LocalDate.now())
                .entryTime(LocalTime.of(6, 30))
                .durationMins(45)
                .intensity(HealthIntensity.HIGH)
                .metricsJson(Map.of("distanceKm", 8.5, "caloriesBurned", 520, "avgHeartRate", 162))
                .notes("Felt energetic, good hydration")
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workout)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Morning Tempo Run"))
                .andExpect(jsonPath("$.data.healthType").value("WORKOUT"))
                .andExpect(jsonPath("$.data.durationMins").value(45))
                .andExpect(jsonPath("$.data.intensity").value("HIGH"))
                .andExpect(jsonPath("$.data.metricsJson.distanceKm").value(8.5))
                .andExpect(jsonPath("$.data.goalId").value(goal.getId().toString()));

        // 3. Create Nutrition entry
        CreateHealthEntryRequest nutrition = CreateHealthEntryRequest.builder()
                .title("Post-Run Protein Shake & Oats")
                .healthType(HealthType.NUTRITION)
                .entryDate(LocalDate.now())
                .entryTime(LocalTime.of(8, 0))
                .metricsJson(Map.of("calories", 550, "proteinGrams", 40, "carbsGrams", 65))
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nutrition)))
                .andExpect(status().isCreated());

        // 4. Query all entries
        mockMvc.perform(get("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 5. Query by goalId
        mockMvc.perform(get("/api/v1/health/entries")
                        .param("goalId", goal.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Morning Tempo Run"));

        // 6. Query by healthType=WORKOUT
        mockMvc.perform(get("/api/v1/health/entries")
                        .param("healthType", "WORKOUT")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Morning Tempo Run"));
    }

    @Test
    @DisplayName("Should calculate daily health summary including workout minutes and sleep")
    void testDailySummaryCalculations() throws Exception {
        LocalDate testDate = LocalDate.now().minusDays(1);

        // 1. Sleep entry
        CreateHealthEntryRequest sleep = CreateHealthEntryRequest.builder()
                .title("Night Rest")
                .healthType(HealthType.SLEEP)
                .entryDate(testDate)
                .metricsJson(Map.of("sleepHours", 7.5, "deepSleepHours", 2.1))
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleep)))
                .andExpect(status().isCreated());

        // 2. Workout entry (60 mins)
        CreateHealthEntryRequest workout = CreateHealthEntryRequest.builder()
                .title("CrossFit WOD")
                .healthType(HealthType.WORKOUT)
                .entryDate(testDate)
                .durationMins(60)
                .intensity(HealthIntensity.HIGH)
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workout)))
                .andExpect(status().isCreated());

        // 3. Mental Health entry
        CreateHealthEntryRequest mental = CreateHealthEntryRequest.builder()
                .title("Guided Breathwork")
                .healthType(HealthType.MENTAL_HEALTH)
                .entryDate(testDate)
                .durationMins(15)
                .metricsJson(Map.of("moodScore", 8))
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mental)))
                .andExpect(status().isCreated());

        // 4. Fetch daily summary
        mockMvc.perform(get("/api/v1/health/daily")
                        .param("date", testDate.toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries").value(3))
                .andExpect(jsonPath("$.data.totalWorkoutMinutes").value(60))
                .andExpect(jsonPath("$.data.totalSleepHours").value(7.5))
                .andExpect(jsonPath("$.data.entriesByType.WORKOUT").value(1))
                .andExpect(jsonPath("$.data.entriesByType.SLEEP").value(1))
                .andExpect(jsonPath("$.data.entriesByType.MENTAL_HEALTH").value(1));
    }

    @Test
    @DisplayName("Should aggregate health analytics over date range")
    void testHealthStatsAggregation() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Workout 1: 40 mins, MODERATE
        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateHealthEntryRequest.builder()
                                .title("Strength Training Upper Body")
                                .healthType(HealthType.WORKOUT)
                                .entryDate(today.minusDays(3))
                                .durationMins(40)
                                .intensity(HealthIntensity.MODERATE)
                                .build())))
                .andExpect(status().isCreated());

        // 2. Workout 2: 50 mins, HIGH
        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateHealthEntryRequest.builder()
                                .title("HIIT Session")
                                .healthType(HealthType.WORKOUT)
                                .entryDate(today.minusDays(1))
                                .durationMins(50)
                                .intensity(HealthIntensity.HIGH)
                                .build())))
                .andExpect(status().isCreated());

        // 3. Annual Checkup
        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateHealthEntryRequest.builder()
                                .title("Biometric Screening")
                                .healthType(HealthType.CHECKUP)
                                .entryDate(today.minusDays(2))
                                .metricsJson(Map.of("bloodPressure", "118/76", "restingHeartRate", 58))
                                .build())))
                .andExpect(status().isCreated());

        // 4. Query Stats
        mockMvc.perform(get("/api/v1/health/stats")
                        .param("startDate", today.minusDays(7).toString())
                        .param("endDate", today.toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries").value(3))
                .andExpect(jsonPath("$.data.totalWorkoutSessions").value(2))
                .andExpect(jsonPath("$.data.totalWorkoutMinutes").value(90))
                .andExpect(jsonPath("$.data.averageWorkoutDurationMins").value(45.0))
                .andExpect(jsonPath("$.data.entriesByType.WORKOUT").value(2))
                .andExpect(jsonPath("$.data.entriesByType.CHECKUP").value(1))
                .andExpect(jsonPath("$.data.intensityBreakdown.HIGH").value(1))
                .andExpect(jsonPath("$.data.intensityBreakdown.MODERATE").value(1));
    }

    @Test
    @DisplayName("Should update health entry and soft-delete")
    void testUpdateAndSoftDelete() throws Exception {
        CreateHealthEntryRequest entryReq = CreateHealthEntryRequest.builder()
                .title("Evening Walk")
                .healthType(HealthType.WORKOUT)
                .entryDate(LocalDate.now())
                .durationMins(25)
                .intensity(HealthIntensity.LOW)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Update title and duration
        mockMvc.perform(put("/api/v1/health/entries/" + id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateHealthEntryRequest.builder()
                                .title("Brisk Evening Walk & Jog")
                                .durationMins(40)
                                .intensity(HealthIntensity.MODERATE)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Brisk Evening Walk & Jog"))
                .andExpect(jsonPath("$.data.durationMins").value(40))
                .andExpect(jsonPath("$.data.intensity").value("MODERATE"));

        // Delete entry
        mockMvc.perform(delete("/api/v1/health/entries/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Fetch -> 404
        mockMvc.perform(get("/api/v1/health/entries/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        assertThat(healthEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should validate constraints and ensure multi-tenant user isolation")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: Duration <= 0
        CreateHealthEntryRequest invalidDuration = CreateHealthEntryRequest.builder()
                .title("Zero Minute Workout")
                .healthType(HealthType.WORKOUT)
                .entryDate(LocalDate.now())
                .durationMins(0)
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDuration)))
                .andExpect(status().isBadRequest());

        // 2. Validation: Duration > 1440
        CreateHealthEntryRequest excessDuration = CreateHealthEntryRequest.builder()
                .title("Marathon of 30 Hours")
                .healthType(HealthType.WORKOUT)
                .entryDate(LocalDate.now())
                .durationMins(1800)
                .build();

        mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessDuration)))
                .andExpect(status().isBadRequest());

        // 3. User isolation
        CreateHealthEntryRequest valid = CreateHealthEntryRequest.builder()
                .title("Private Health Record")
                .healthType(HealthType.MEDICAL)
                .entryDate(LocalDate.now())
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/health/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID validId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        String otherUid = "other_health_user_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_health_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Health User")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/health/entries/" + validId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/health/entries/" + validId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
