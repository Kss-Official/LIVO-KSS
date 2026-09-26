package com.livo.api.modules.insights;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.service.EventService;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.service.FinanceService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.LogLearningSessionRequest;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.service.LearningService;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.service.PlanService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsightsDomainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HabitService habitService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private PlanService planService;

    @Autowired
    private EventService eventService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private LearningService learningService;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "insights_user_" + UUID.randomUUID();
        String email = "insights_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Alex Insights Pilot")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should return full comprehensive insights feed with all sub-engines populated")
    void testComprehensiveInsightsFeed() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Create and complete tasks
        TaskResponse t1 = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Complete API Documentation")
                .dueDate(today)
                .dueTime(LocalTime.of(10, 0))
                .durationMins(60)
                .projectLabel("WORK")
                .build());
        taskService.toggleTaskCompletion(userId, t1.getId());

        TaskResponse t2 = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Refactor Database Indexes")
                .dueDate(today)
                .dueTime(LocalTime.of(14, 0))
                .durationMins(90)
                .projectLabel("WORK")
                .build());
        taskService.toggleTaskCompletion(userId, t2.getId());

        // 2. Create Schedule Block
        planService.createScheduleBlock(userId, CreateScheduleBlockRequest.builder()
                .title("Architecture Sprint")
                .blockDate(today)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .category("WORK")
                .build());

        // 3. Create Calendar Event
        eventService.createEvent(userId, CreateEventRequest.builder()
                .title("Team Product Review")
                .startTime(today.atTime(15, 0).atZone(ZoneId.of("Asia/Kolkata")).toInstant())
                .endTime(today.atTime(16, 0).atZone(ZoneId.of("Asia/Kolkata")).toInstant())
                .category("WORK")
                .build());

        // 4. Create Habit and Log Check-in
        HabitResponse habit = habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Morning Run")
                .frequencyType(HabitFrequency.DAILY)
                .build());
        habitService.logHabit(userId, habit.getId(), LogHabitRequest.builder()
                .logDate(today)
                .countCompleted(1)
                .build());

        // 5. Create Goal
        goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Learn System Design")
                .targetDate(today.plusMonths(2))
                .targetValue(new BigDecimal("100"))
                .unit("%")
                .build());

        // 6. Create Expense
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Cloud Hosting Invoice")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1200.00"))
                .currency("INR")
                .category("Bills")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(today)
                .build());

        // 7. Create Learning Item and Log Session
        var learnItem = learningService.createLearningItem(userId, CreateLearningItemRequest.builder()
                .title("Designing Data-Intensive Applications")
                .category("TECH")
                .learningType(LearningType.BOOK)
                .difficultyLevel(DifficultyLevel.ADVANCED)
                .targetStudyTimeMinutes(60)
                .build());
        learningService.logSession(userId, LogLearningSessionRequest.builder()
                .learningItemId(learnItem.getId())
                .sessionDate(today)
                .durationMinutes(45)
                .notes("Read Chapter 3 on Storage Engines")
                .build());

        // Execute GET /api/v1/insights
        mockMvc.perform(get("/api/v1/insights")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timeDistribution.categories.length()").isNumber())
                .andExpect(jsonPath("$.data.timeDistribution.totalTrackedMinutes").isNumber())
                .andExpect(jsonPath("$.data.productivityHistogram.hourlyDistribution.length()").value(24))
                .andExpect(jsonPath("$.data.productivityHistogram.peakHourWindow").isNotEmpty())
                .andExpect(jsonPath("$.data.weeklyComparison.metrics.length()").value(4))
                .andExpect(jsonPath("$.data.habitCorrelations.correlations.length()").value(1))
                .andExpect(jsonPath("$.data.habitCorrelations.correlations[0].habitTitle").value("Morning Run"))
                .andExpect(jsonPath("$.data.userStats.tasksCompletedTotal").value(2))
                .andExpect(jsonPath("$.data.userStats.activeGoalsTotal").value(1))
                .andExpect(jsonPath("$.data.userStats.learningTimeMinutesTotal").value(45))
                .andExpect(jsonPath("$.data.totalExpensesThisWeek").value(1200.00));
    }

    @Test
    @DisplayName("Should query individual insights endpoints successfully")
    void testIndividualInsightEndpoints() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Time Distribution endpoint
        mockMvc.perform(get("/api/v1/insights/time-distribution")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories").isArray());

        // 2. Productivity Histogram endpoint
        mockMvc.perform(get("/api/v1/insights/productivity-histogram")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hourlyDistribution.length()").value(24));

        // 3. Weekly Comparison endpoint
        mockMvc.perform(get("/api/v1/insights/week-comparison")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metrics.length()").value(4));

        // 4. Correlations endpoint
        mockMvc.perform(get("/api/v1/insights/correlations")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("days", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.evaluatedDays").value(14));

        // 5. User Stats endpoint
        mockMvc.perform(get("/api/v1/insights/user-stats")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tasksCompletedTotal").isNumber());
    }

    @Test
    @DisplayName("Should enforce strict multi-tenant isolation across users for insights")
    void testMultiTenantInsightsIsolation() throws Exception {
        // User A creates 1 completed task
        TaskResponse t = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("User A Secret Project")
                .dueDate(LocalDate.now())
                .build());
        taskService.toggleTaskCompletion(userId, t.getId());

        // User B registers
        String uidB = "insights_b_" + UUID.randomUUID();
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email("insights_b_" + UUID.randomUUID() + "@livo.test")
                .fullName("User B Empty Profile")
                .timezone("UTC")
                .build());

        // User B queries insights
        mockMvc.perform(get("/api/v1/insights")
                        .header("Authorization", "Bearer " + authB.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userStats.tasksCompletedTotal").value(0))
                .andExpect(jsonPath("$.data.userStats.activeGoalsTotal").value(0))
                .andExpect(jsonPath("$.data.productivityHistogram.totalCompletedTasks").value(0))
                .andExpect(jsonPath("$.data.totalExpensesThisWeek").value(0.0));
    }

    @Test
    @DisplayName("Should pass caller custom date range through to habit correlations in comprehensive insights")
    void testComprehensiveInsightsCustomDateRangeForHabitCorrelations() throws Exception {
        LocalDate start = LocalDate.now().minusDays(13);
        LocalDate end = LocalDate.now();

        mockMvc.perform(get("/api/v1/insights")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.startDate").value(start.toString()))
                .andExpect(jsonPath("$.data.endDate").value(end.toString()))
                .andExpect(jsonPath("$.data.habitCorrelations.evaluatedDays").value(14));

        mockMvc.perform(get("/api/v1/insights/correlations")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.evaluatedDays").value(14));
    }
}
