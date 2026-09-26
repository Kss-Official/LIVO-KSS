package com.livo.api.modules.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import com.livo.api.modules.ai.repository.AiRecommendationRepository;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.service.EventService;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.service.FinanceService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.goal.service.MilestoneService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.service.PlanService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
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

import com.livo.api.modules.home.dto.NextUpCard;
import com.livo.api.modules.home.service.HomeServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeDomainIntegrationTest {

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
    private GoalService goalService;

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private PlanService planService;

    @Autowired
    private EventService eventService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private AiRecommendationRepository aiRecommendationRepository;

    @Autowired
    private HomeServiceImpl homeServiceImpl;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "home_user_" + UUID.randomUUID();
        String email = "home_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Taylor Home Pilot")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should return complete aggregated Home feed with 12 unified modules")
    void testAggregatedHomeDashboardFeed() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Create tasks (1 in-progress, 1 urgent due today, 1 normal due today)
        TaskResponse inProgressTask = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Architecture Review Session")
                .dueDate(today)
                .priority(TaskPriority.HIGH)
                .durationMins(60)
                .projectLabel("LIVO Core")
                .build());

        // Mark as in-progress with startedAt
        TaskEntity inProgressEntity = taskRepository.findByIdAndUserId(inProgressTask.getId(), userId).orElseThrow();
        inProgressEntity.setStatus(TaskStatus.IN_PROGRESS);
        inProgressEntity.setStartedAt(Instant.now().minusSeconds(1200)); // 20 mins ago
        taskRepository.save(inProgressEntity);

        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Submit Client Proposal")
                .dueDate(today)
                .dueTime(LocalTime.of(15, 0))
                .priority(TaskPriority.URGENT)
                .durationMins(45)
                .build());

        // 2. Create Habit & Log Check-in
        HabitResponse habit = habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Hydration 2L")
                .frequencyType(HabitFrequency.DAILY)
                .build());

        habitService.logHabit(userId, habit.getId(), LogHabitRequest.builder()
                .logDate(today)
                .countCompleted(1)
                .build());

        // 3. Create Goal with Milestones
        GoalResponse goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Achieve AWS Solutions Architect")
                .targetDate(today.plusMonths(3))
                .targetValue(new BigDecimal("100"))
                .unit("%")
                .build());

        var m1 = milestoneService.addMilestone(userId, goal.getId(), CreateMilestoneRequest.builder()
                .title("Pass Practice Exam 1")
                .build());
        milestoneService.toggleMilestoneCompletion(userId, goal.getId(), m1.getId());

        milestoneService.addMilestone(userId, goal.getId(), CreateMilestoneRequest.builder()
                .title("Pass Practice Exam 2")
                .build());

        // 4. Create Schedule Block
        planService.createScheduleBlock(userId, CreateScheduleBlockRequest.builder()
                .title("Deep Focus Block")
                .blockDate(today)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .category("WORK")
                .build());

        // 5. Create Calendar Event
        eventService.createEvent(userId, CreateEventRequest.builder()
                .title("Team Standup")
                .startTime(today.atTime(10, 0).atZone(java.time.ZoneId.of("Asia/Kolkata")).toInstant())
                .endTime(today.atTime(10, 30).atZone(java.time.ZoneId.of("Asia/Kolkata")).toInstant())
                .category("WORK")
                .build());

        // 6. Create Finance Budget & Expense
        financeService.createBudget(userId, CreateBudgetRequest.builder()
                .category("Food")
                .monthlyLimit(new BigDecimal("10000.00"))
                .alertThresholdPercent((short) 80)
                .monthStart(today.withDayOfMonth(1))
                .build());

        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Weekly Groceries")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .category("Food")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(today)
                .build());

        // 7. Seed AI recommendation
        AiRecommendationEntity rec = AiRecommendationEntity.builder()
                .userId(userId)
                .type(AiRecommendationType.HIGH_PRIORITY_TASK)
                .title("Focus Sprint: Submit Client Proposal")
                .reason("Urgent priority task due today. Recommend 45 minutes of deep focus.")
                .actionType("SCHEDULE_TASK")
                .isActive(true)
                .build();
        aiRecommendationRepository.save(rec);

        // Execute GET /api/v1/home
        mockMvc.perform(get("/api/v1/home")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.greeting").isNotEmpty())
                .andExpect(jsonPath("$.data.dailyQuote.quote").isNotEmpty())
                .andExpect(jsonPath("$.data.dailyQuote.author").isNotEmpty())
                .andExpect(jsonPath("$.data.dailyLifeScore.overallScore").isNumber())
                .andExpect(jsonPath("$.data.dailyLifeScore.ratingLabel").isNotEmpty())
                .andExpect(jsonPath("$.data.activeTask.title").value("Architecture Review Session"))
                .andExpect(jsonPath("$.data.activeTask.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.priorityCarousel.length()").value(2))
                .andExpect(jsonPath("$.data.priorityCarousel[0].rankLabel").value("1 of 2"))
                .andExpect(jsonPath("$.data.priorityCarousel[1].rankLabel").value("2 of 2"))
                .andExpect(jsonPath("$.data.nextUpSchedule").isNotEmpty())
                .andExpect(jsonPath("$.data.habitsProgress.totalScheduledToday").value(1))
                .andExpect(jsonPath("$.data.habitsProgress.completedToday").value(1))
                .andExpect(jsonPath("$.data.habitsProgress.completionRate").value(100.0))
                .andExpect(jsonPath("$.data.goalsProgress.activeGoalsCount").value(1))
                .andExpect(jsonPath("$.data.goalsProgress.completedMilestones").value(1))
                .andExpect(jsonPath("$.data.goalsProgress.totalMilestones").value(2))
                .andExpect(jsonPath("$.data.goalsProgress.label").value("Goals 1/2"))
                .andExpect(jsonPath("$.data.workloadSummary.workloadStatus").isNotEmpty())
                .andExpect(jsonPath("$.data.financeSnapshot.monthTotalExpense").value(2500.00))
                .andExpect(jsonPath("$.data.financeSnapshot.monthlyBudgetLimit").value(10000.00))
                .andExpect(jsonPath("$.data.proactiveRecommendation.title").value("Focus Sprint: Submit Client Proposal"));
    }

    @Test
    @DisplayName("Should verify daily-overview alias and quote of the day endpoint")
    void testDailyOverviewAliasAndQuoteEndpoint() throws Exception {
        // 1. Daily overview alias
        mockMvc.perform(get("/api/v1/home/daily-overview")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.greeting").isNotEmpty())
                .andExpect(jsonPath("$.data.dailyLifeScore").isNotEmpty());

        // 2. Standalone quote endpoint
        mockMvc.perform(get("/api/v1/home/quote")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quote").isNotEmpty())
                .andExpect(jsonPath("$.data.author").isNotEmpty());
    }

    @Test
    @DisplayName("Should enforce strict multi-tenant isolation on home dashboard")
    void testMultiTenantHomeIsolation() throws Exception {
        // User A creates 1 task
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("User A Exclusive Secret Task")
                .dueDate(LocalDate.now())
                .priority(TaskPriority.HIGH)
                .build());

        // User B registers
        String uidB = "home_b_" + UUID.randomUUID();
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email("home_b_" + UUID.randomUUID() + "@livo.test")
                .fullName("User B Empty Profile")
                .timezone("UTC")
                .build());

        // User B queries home
        mockMvc.perform(get("/api/v1/home")
                        .header("Authorization", "Bearer " + authB.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.greeting").isNotEmpty())
                .andExpect(jsonPath("$.data.activeTask").doesNotExist())
                .andExpect(jsonPath("$.data.priorityCarousel.length()").value(0))
                .andExpect(jsonPath("$.data.habitsProgress.totalScheduledToday").value(0))
                .andExpect(jsonPath("$.data.financeSnapshot.monthTotalExpense").value(0.0));
    }

    @Test
    @DisplayName("Should correctly mark late-night event crossing midnight as live now")
    void testResolveNextUpScheduleMidnightRolloverForLateNightEvents() {
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");

        // Event starting at 23:30 with no explicit endTime (defaults to 00:30 next day)
        Instant eventStart = today.atTime(23, 30).atZone(zoneId).toInstant();
        eventService.createEvent(userId, CreateEventRequest.builder()
                .title("Late Night Hackathon Session")
                .startTime(eventStart)
                .category("WORK")
                .build());

        // 1. At 23:45 (within the 23:30 - 00:30 window) -> Should be live now!
        LocalTime testTimeDuringEvent = LocalTime.of(23, 45);
        NextUpCard nextUpLive = homeServiceImpl.resolveNextUpSchedule(userId, today, testTimeDuringEvent, zoneId);
        assertThat(nextUpLive).isNotNull();
        assertThat(nextUpLive.getTitle()).isEqualTo("Late Night Hackathon Session");
        assertThat(nextUpLive.isLiveNow()).isTrue();
        assertThat(nextUpLive.getStartTime()).isEqualTo(LocalTime.of(23, 30));
        assertThat(nextUpLive.getEndTime()).isEqualTo(LocalTime.of(0, 30));

        // 2. At 23:15 (before the event) -> Should NOT be live now, but upcoming
        LocalTime testTimeBeforeEvent = LocalTime.of(23, 15);
        NextUpCard nextUpUpcoming = homeServiceImpl.resolveNextUpSchedule(userId, today, testTimeBeforeEvent, zoneId);
        assertThat(nextUpUpcoming).isNotNull();
        assertThat(nextUpUpcoming.isLiveNow()).isFalse();
    }
}
