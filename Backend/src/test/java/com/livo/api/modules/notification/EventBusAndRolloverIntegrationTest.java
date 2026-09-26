package com.livo.api.modules.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.finance.dto.CreateBudgetRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.service.FinanceService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.notification.dto.NotificationResponse;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.scheduler.MidnightRolloverScheduler;
import com.livo.api.modules.notification.service.NotificationService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventBusAndRolloverIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MidnightRolloverScheduler midnightRolloverScheduler;

    private UUID userId;

    @BeforeEach
    void setUp() {
        String uid = "event_user_" + UUID.randomUUID();
        String email = "event_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Event Bus Tester")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
    }

    @Test
    @DisplayName("Should publish TaskCompletedEvent and update linked goal progress asynchronously")
    void taskCompleted_publishesEventAndUpdatesLinkedGoalProgress() throws Exception {
        // 1. Create a goal
        GoalResponse goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Complete Fullstack Project")
                .targetDescription("100% of tasks done")
                .build());

        // 2. Create 2 tasks linked to this goal
        TaskResponse task1 = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Build backend APIs")
                .goalId(goal.getId())
                .dueDate(LocalDate.now())
                .build());

        TaskResponse task2 = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Build mobile UI")
                .goalId(goal.getId())
                .dueDate(LocalDate.now())
                .build());

        // 3. Complete task 1
        taskService.toggleTaskCompletion(userId, task1.getId());

        // 4. Await linked goal progress updated to 50.00%
        GoalEntity updatedGoal = null;
        for (int i = 0; i < 30; i++) {
            updatedGoal = goalRepository.findByIdAndUserIdAndDeletedAtIsNull(goal.getId(), userId).orElseThrow();
            if (updatedGoal.getCurrentValue() != null && updatedGoal.getCurrentValue().compareTo(BigDecimal.ZERO) > 0) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(updatedGoal).isNotNull();
        assertThat(updatedGoal.getCurrentValue()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Should publish HabitCheckedInEvent and create streak celebration notification at milestone")
    void habitStreakMilestone_publishesEventAndCreatesCelebrationNotification() throws Exception {
        // 1. Create a habit starting 10 days ago
        HabitResponse habit = habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Daily Coding Habit")
                .startDate(LocalDate.now().minusDays(10))
                .frequencyType(HabitFrequency.DAILY)
                .build());

        // 2. Insert logs for previous 6 consecutive days (today - 6 to today - 1)
        for (int i = 6; i >= 1; i--) {
            HabitLogEntity logEntry = HabitLogEntity.builder()
                    .habitId(habit.getId())
                    .logDate(LocalDate.now().minusDays(i))
                    .countCompleted(1)
                    .loggedAt(Instant.now())
                    .build();
            logEntry.setUserId(userId);
            logEntry.setVersion(1L);
            habitLogRepository.save(logEntry);
        }

        // 3. Log habit for today -> streak recalculates to 7 -> triggers milestone notification
        habitService.logHabit(userId, habit.getId(), LogHabitRequest.builder()
                .logDate(LocalDate.now())
                .countCompleted(1)
                .build());

        // 4. Await notification of type STREAK
        List<NotificationResponse> notifications = List.of();
        for (int i = 0; i < 30; i++) {
            notifications = notificationService.getNotifications(userId, false, NotificationType.STREAK, 10);
            if (!notifications.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).contains("Streak Milestone Reached");
        assertThat(notifications.get(0).getBody()).contains("7-day streak");

        // 5. Log habit again today (streak remains 7, count increments) -> should NOT trigger duplicate notification
        habitService.logHabit(userId, habit.getId(), LogHabitRequest.builder()
                .logDate(LocalDate.now())
                .countCompleted(1)
                .build());
        Thread.sleep(500);

        List<NotificationResponse> notificationsAfterSecondLog =
                notificationService.getNotifications(userId, false, NotificationType.STREAK, 10);
        assertThat(notificationsAfterSecondLog).hasSize(1);
    }

    @Test
    @DisplayName("Should publish ExpenseCreatedEvent, generate budget warning at threshold, and deduplicate subsequent expenses")
    void expenseExceedingBudgetThreshold_publishesEventAndGeneratesBudgetWarning() throws Exception {
        String category = "DINING_" + UUID.randomUUID().toString().substring(0, 4);

        // 1. Set a monthly budget of 1,000 INR
        financeService.createBudget(userId, CreateBudgetRequest.builder()
                .category(category)
                .monthlyLimit(new BigDecimal("1000.00"))
                .monthStart(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1))
                .build());

        // 2. Create an expense of 850 INR (85% of budget)
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Dinner with Friends")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("850.00"))
                .currency("INR")
                .category(category)
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        // 3. Await notification of type BUDGET
        List<NotificationResponse> notifications = List.of();
        for (int i = 0; i < 30; i++) {
            notifications = notificationService.getNotifications(userId, false, NotificationType.BUDGET, 10);
            if (!notifications.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).contains("Budget Alert (80%)");
        assertThat(notifications.get(0).getBody()).contains("85%");

        // 4. Create another expense of 50 INR (90% of budget) — MUST NOT re-send threshold alert
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Coffee after Dinner")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("INR")
                .category(category)
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        Thread.sleep(500); // Allow async listener to complete
        var notifsAfterSecondExpense = notificationService.getNotifications(userId, false, NotificationType.BUDGET, 10);
        assertThat(notifsAfterSecondExpense).hasSize(1); // Still exactly 1 alert!

        // 5. Create an expense of 200 INR (110% of budget) — MUST send "Budget Exceeded!" alert
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Late Night Snack")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("200.00"))
                .currency("INR")
                .category(category)
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        List<NotificationResponse> notifsAfterExceeded = List.of();
        for (int i = 0; i < 30; i++) {
            notifsAfterExceeded = notificationService.getNotifications(userId, false, NotificationType.BUDGET, 10);
            if (notifsAfterExceeded.size() >= 2) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(notifsAfterExceeded).hasSize(2);
        assertThat(notifsAfterExceeded.get(0).getTitle()).isEqualTo("Budget Exceeded!");

        // 6. Create another expense of 50 INR (115% of budget) — MUST NOT re-send exceeded alert
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Another Snack")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("INR")
                .category(category)
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        Thread.sleep(500);
        var notifsFinal = notificationService.getNotifications(userId, false, NotificationType.BUDGET, 10);
        assertThat(notifsFinal).hasSize(2); // Still exactly 2 alerts!
    }

    @Test
    @DisplayName("Should respect user configured alertThresholdPercent (e.g. 50%)")
    void customThresholdBudgetAlert_firesAtConfiguredThreshold() throws Exception {
        String category = "GROCERY_" + UUID.randomUUID().toString().substring(0, 4);

        // 1. Set a monthly budget of 2,000 INR with alertThresholdPercent = 50%
        financeService.createBudget(userId, CreateBudgetRequest.builder()
                .category(category)
                .monthlyLimit(new BigDecimal("2000.00"))
                .alertThresholdPercent((short) 50)
                .monthStart(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1))
                .build());

        // 2. Create an expense of 1,100 INR (55% of budget, crosses 50% threshold)
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Supermarket Shopping")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1100.00"))
                .currency("INR")
                .category(category)
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        // 3. Await notification of type BUDGET
        List<NotificationResponse> notifications = List.of();
        for (int i = 0; i < 30; i++) {
            notifications = notificationService.getNotifications(userId, false, NotificationType.BUDGET, 10);
            if (!notifications.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).contains("Budget Alert (50%)");
        assertThat(notifications.get(0).getBody()).contains("55%");
    }

    @Test
    @DisplayName("Should detect overdue tasks from previous days and dispatch rollover notifications")
    void midnightRollover_detectsOverdueTasksAndDispatchesAlerts() {
        // 1. Create an incomplete task with due date in the past
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Overdue Tax Filing")
                .dueDate(LocalDate.now().minusDays(2))
                .build());

        // 2. Execute midnight rollover
        MidnightRolloverScheduler.RolloverResult result = midnightRolloverScheduler.executeRollover(LocalDate.now());

        // 3. Verify result
        assertThat(result.getTotalOverdueFound()).isGreaterThanOrEqualTo(1);
        assertThat(result.getUsersNotified()).isGreaterThanOrEqualTo(1);

        // 4. Verify notification of type OVERDUE was generated for user
        List<NotificationResponse> notifications = notificationService.getNotifications(userId, false, NotificationType.OVERDUE, 10);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).isEqualTo("Overdue Tasks Reminder");
        assertThat(notifications.get(0).getBody()).contains("overdue task");
    }
}
