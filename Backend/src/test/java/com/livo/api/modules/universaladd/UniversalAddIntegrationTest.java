package com.livo.api.modules.universaladd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.service.FinanceService;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.universaladd.dto.*;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UniversalAddIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FinanceService financeService;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "universal_user_" + UUID.randomUUID();
        String email = "universal_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Universal Tester")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should parse task with due date, time, and hashtags")
    void parseTaskWithDueDateTimeAndTags_returnsStructuredTaskDraft() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Buy groceries tomorrow at 5pm #errands")
                .build();

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("TASK"))
                .andExpect(jsonPath("$.data.parsedDraft.title", containsString("Buy groceries")))
                .andExpect(jsonPath("$.data.parsedDraft.date").value(tomorrow.toString()))
                .andExpect(jsonPath("$.data.parsedDraft.time").value("17:00:00"))
                .andExpect(jsonPath("$.data.parsedDraft.tags[0]").value("errands"))
                .andExpect(jsonPath("$.data.confidence").value(greaterThanOrEqualTo(0.8)));
    }

    @Test
    @DisplayName("Should parse expense with amount, currency, and category")
    void parseExpenseWithAmountAndCurrency_returnsStructuredExpenseDraft() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Spent 450 INR on lunch at Subway")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("EXPENSE"))
                .andExpect(jsonPath("$.data.parsedDraft.amount").value(450.0))
                .andExpect(jsonPath("$.data.parsedDraft.currency").value("INR"))
                .andExpect(jsonPath("$.data.parsedDraft.category").value("FOOD"))
                .andExpect(jsonPath("$.data.confidence").value(greaterThanOrEqualTo(0.9)));
    }

    @Test
    @DisplayName("Should parse habit with frequency and category")
    void parseHabitWithFrequency_returnsStructuredHabitDraft() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Drink 3 liters of water daily every morning")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("HABIT"))
                .andExpect(jsonPath("$.data.parsedDraft.frequency").value("DAILY"))
                .andExpect(jsonPath("$.data.parsedDraft.category").value("HEALTH"));
    }

    @Test
    @DisplayName("Should parse event with time and location")
    void parseEventWithTimeAndLocation_returnsStructuredEventDraft() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Team Sync meeting on Friday at 3pm at Room 4")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("EVENT"))
                .andExpect(jsonPath("$.data.parsedDraft.time").value("15:00:00"))
                .andExpect(jsonPath("$.data.parsedDraft.locationOrDestination").value("Room 4"));
    }

    @Test
    @DisplayName("Should parse trip with destination and date range")
    void parseTripWithDestination_returnsStructuredTripDraft() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Trip to Goa from 2026-10-10 to 2026-10-15")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("TRIP"))
                .andExpect(jsonPath("$.data.parsedDraft.locationOrDestination").value("Goa"))
                .andExpect(jsonPath("$.data.parsedDraft.date").value("2026-10-10"))
                .andExpect(jsonPath("$.data.parsedDraft.endDate").value("2026-10-15"));
    }

    @Test
    @DisplayName("Should confirm and persist task in database with 1-tap")
    void confirmAndCreateTask_persistsEntityInDatabase() throws Exception {
        ParsedEntityDraft draft = ParsedEntityDraft.builder()
                .domain(UniversalAddDomain.TASK)
                .title("Complete client proposal")
                .description("Prepare 10-slide pitch deck for prospective client")
                .category("WORK")
                .date(LocalDate.now().plusDays(2))
                .priority("HIGH")
                .build();

        UniversalAddConfirmRequest confirmReq = UniversalAddConfirmRequest.builder()
                .draft(draft)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/universal-add/confirm")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.domain").value("TASK"))
                .andExpect(jsonPath("$.data.title").value("Complete client proposal"))
                .andExpect(jsonPath("$.data.entityId").isNotEmpty())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseJson);
        UUID createdId = UUID.fromString(jsonNode.get("data").get("entityId").asText());

        // Verify entity exists via TaskService
        TaskResponse createdTask = taskService.getTaskById(userId, createdId);
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("Complete client proposal");
        assertThat(createdTask.getCategory()).isEqualTo("WORK");
    }

    @Test
    @DisplayName("Should confirm and persist expense in database with 1-tap")
    void confirmAndCreateExpense_persistsEntityInDatabase() throws Exception {
        ParsedEntityDraft draft = ParsedEntityDraft.builder()
                .domain(UniversalAddDomain.EXPENSE)
                .title("Team Dinner Celebration")
                .description("Celebratory dinner for milestone release")
                .amount(new BigDecimal("1850.00"))
                .currency("INR")
                .category("FOOD")
                .date(LocalDate.now())
                .build();

        UniversalAddConfirmRequest confirmReq = UniversalAddConfirmRequest.builder()
                .draft(draft)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/universal-add/confirm")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.domain").value("EXPENSE"))
                .andExpect(jsonPath("$.data.title").value("Team Dinner Celebration"))
                .andExpect(jsonPath("$.data.entityId").isNotEmpty())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseJson);
        UUID createdId = UUID.fromString(jsonNode.get("data").get("entityId").asText());

        // Verify entity exists via FinanceService
        TransactionResponse createdTx = financeService.getTransactionById(userId, createdId);
        assertThat(createdTx).isNotNull();
        assertThat(createdTx.getTitle()).isEqualTo("Team Dinner Celebration");
        assertThat(createdTx.getAmount()).isEqualByComparingTo("1850.00");
    }

    @Test
    @DisplayName("Should preserve TRIP domain when input mentions trip with budget amount")
    void parseTripWithBudget_preservesTripDomainOverAmount() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Trip to Goa from 2026-10-10 to 2026-10-15 with Rs 5000 budget")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("TRIP"))
                .andExpect(jsonPath("$.data.parsedDraft.locationOrDestination").value("Goa"))
                .andExpect(jsonPath("$.data.parsedDraft.date").value("2026-10-10"))
                .andExpect(jsonPath("$.data.parsedDraft.endDate").value("2026-10-15"))
                .andExpect(jsonPath("$.data.parsedDraft.amount").value(5000.0));
    }

    @Test
    @DisplayName("Should preserve EVENT domain when input mentions meeting/conference with ticket cost")
    void parseEventWithCost_preservesEventDomainOverAmount() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Conference webinar on Friday at 3pm ticket $50")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("EVENT"))
                .andExpect(jsonPath("$.data.parsedDraft.time").value("15:00:00"))
                .andExpect(jsonPath("$.data.parsedDraft.amount").value(50.0))
                .andExpect(jsonPath("$.data.parsedDraft.currency").value("USD"));
    }

    @Test
    @DisplayName("Should extract lowercase destination and format location cleanly")
    void parseTripWithLowercaseDestination_extractsFormattedLocation() throws Exception {
        UniversalAddRequest req = UniversalAddRequest.builder()
                .input("Trip to goa with Rs 3000 budget")
                .build();

        mockMvc.perform(post("/api/v1/universal-add/parse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("TRIP"))
                .andExpect(jsonPath("$.data.parsedDraft.locationOrDestination").value("Goa"))
                .andExpect(jsonPath("$.data.parsedDraft.amount").value(3000.0));
    }

    @Test
    @DisplayName("Should never create trip titled 'Trip to null' when destination is absent")
    void confirmTripWithNullDestination_createsTripWithCleanTitleAndDestination() throws Exception {
        ParsedEntityDraft draft = ParsedEntityDraft.builder()
                .domain(UniversalAddDomain.TRIP)
                .title(null)
                .locationOrDestination(null)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .date(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        UniversalAddConfirmRequest confirmReq = UniversalAddConfirmRequest.builder()
                .draft(draft)
                .build();

        mockMvc.perform(post("/api/v1/universal-add/confirm")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.domain").value("TRIP"))
                .andExpect(jsonPath("$.data.title").value("Trip"))
                .andExpect(jsonPath("$.data.entityId").isNotEmpty());
    }
}
