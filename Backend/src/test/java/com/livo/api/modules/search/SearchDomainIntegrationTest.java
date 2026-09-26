package com.livo.api.modules.search;

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
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.service.LearningService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchDomainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private EventService eventService;

    @Autowired
    private HabitService habitService;

    @Autowired
    private LearningService learningService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TripService tripService;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "search_user_" + UUID.randomUUID();
        String email = "search_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Search Tester")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should return matching items across all 7 domains for unified query")
    void searchAllDomains_returnsMatchingEntitiesAcrossModules() throws Exception {
        String keyword = "Quantum" + UUID.randomUUID().toString().substring(0, 5);

        // 1. Task
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Complete " + keyword + " algorithm implementation")
                .description("Detailed notes on " + keyword + " computing state vectors")
                .dueDate(LocalDate.now().plusDays(2))
                .build());

        // 2. Goal
        goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Master " + keyword + " Mechanics")
                .description("Achieve deep conceptual clarity in " + keyword + " mechanics")
                .targetDescription("Read 3 comprehensive textbooks on " + keyword)
                .targetDate(LocalDate.now().plusMonths(6))
                .build());

        // 3. Event
        eventService.createEvent(userId, CreateEventRequest.builder()
                .title(keyword + " Conference Keynote")
                .description("Global summit on " + keyword + " developments")
                .location("Auditorium B - " + keyword + " Center")
                .startTime(Instant.now().plusSeconds(7200))
                .endTime(Instant.now().plusSeconds(10800))
                .build());

        // 4. Habit
        habitService.createHabit(userId, CreateHabitRequest.builder()
                .title("Daily " + keyword + " Meditation")
                .description("15 minutes focus on " + keyword + " mind clarity")
                .motivationNote("Keep the " + keyword + " flow alive every day")
                .frequencyType(HabitFrequency.DAILY)
                .build());

        // 5. Learning Item
        learningService.createLearningItem(userId, CreateLearningItemRequest.builder()
                .title("Advanced " + keyword + " Cryptography")
                .description("Symmetric and asymmetric primitives for " + keyword)
                .notes("Key exchange protocols resilient to " + keyword + " attacks")
                .learningType(LearningType.COURSE)
                .difficultyLevel(DifficultyLevel.ADVANCED)
                .build());

        // 6. Expense (Transaction)
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title(keyword + " Hardware Kit")
                .description("Purchase of experimental " + keyword + " sensors")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("3500.00"))
                .currency("INR")
                .category("EQUIPMENT")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(LocalDate.now())
                .build());

        // 7. Trip
        tripService.createTrip(userId, CreateTripRequest.builder()
                .title(keyword + " Expedition")
                .destination("Zurich - " + keyword + " Lab")
                .startDate(LocalDate.now().plusWeeks(2))
                .endDate(LocalDate.now().plusWeeks(3))
                .build());

        // Execute unified search with domain=ALL
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("q", keyword)
                        .param("domain", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.query").value(keyword))
                .andExpect(jsonPath("$.data.domainFilter").value("ALL"))
                .andExpect(jsonPath("$.data.totalResults").value(greaterThanOrEqualTo(7)))
                .andExpect(jsonPath("$.data.items[*].domainType", hasItems(
                        "TASK", "GOAL", "EVENT", "HABIT", "LEARNING", "EXPENSE", "TRIP"
                )))
                .andExpect(jsonPath("$.data.items[0].relevanceScore").value(greaterThan(0.0)));
    }

    @Test
    @DisplayName("Should return only target domain items when domain filter is specified")
    void searchWithDomainFilter_returnsOnlyTargetDomain() throws Exception {
        String keyword = "FilterTag" + UUID.randomUUID().toString().substring(0, 5);

        // Create Task
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Task about " + keyword)
                .build());

        // Create Expense
        financeService.createTransaction(userId, CreateTransactionRequest.builder()
                .title("Expense for " + keyword)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("499.00"))
                .currency("INR")
                .category("SHOPPING")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionDate(LocalDate.now())
                .build());

        // Search with domain=TASK
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("q", keyword)
                        .param("domain", "TASK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domainFilter").value("TASK"))
                .andExpect(jsonPath("$.data.totalResults").value(1))
                .andExpect(jsonPath("$.data.items[0].domainType").value("TASK"))
                .andExpect(jsonPath("$.data.items[0].title", containsString(keyword)));

        // Search with domain=EXPENSE
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("q", keyword)
                        .param("domain", "EXPENSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domainFilter").value("EXPENSE"))
                .andExpect(jsonPath("$.data.totalResults").value(1))
                .andExpect(jsonPath("$.data.items[0].domainType").value("EXPENSE"))
                .andExpect(jsonPath("$.data.items[0].title", containsString(keyword)));
    }

    @Test
    @DisplayName("Should maintain strict multi-tenant isolation across search results")
    void searchMultiTenantIsolation_doesNotExposeOtherUserData() throws Exception {
        String secretKeyword = "Confidential" + UUID.randomUUID().toString().substring(0, 5);

        // User A creates a task with secret keyword
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Top Secret " + secretKeyword)
                .build());

        // User B registers
        String uidB = "user_b_" + UUID.randomUUID();
        String emailB = "user_b_" + UUID.randomUUID() + "@livo.test";
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email(emailB)
                .fullName("User B Investigator")
                .build());

        // User B searches for User A's keyword -> must return 0 results
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + authB.getAccessToken())
                        .param("q", secretKeyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResults").value(0))
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    @DisplayName("Should gracefully handle blank or empty queries with zero results")
    void searchWithBlankQuery_returnsEmptyResultsGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("q", "   ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResults").value(0))
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResults").value(0))
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    @DisplayName("Should rank exact title matches higher than partial substring matches")
    void searchRelevanceRanking_ranksExactMatchesHigher() throws Exception {
        String token = "Solana" + UUID.randomUUID().toString().substring(0, 4);

        // Exact title match
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title(token)
                .description("Pure token exact name")
                .build());

        // Substring match in description
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("General Web3 Reading")
                .description("Includes a small segment on " + token + " tokenomics and staking")
                .build());

        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("q", token)
                        .param("domain", "TASK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResults").value(2))
                .andExpect(jsonPath("$.data.items[0].title").value(token))
                .andExpect(jsonPath("$.data.items[0].relevanceScore").value(1.0))
                .andExpect(jsonPath("$.data.items[1].relevanceScore").value(lessThan(1.0)));
    }
}
