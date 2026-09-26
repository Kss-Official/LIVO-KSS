package com.livo.api.modules.trip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.trip.dto.CreateItineraryItemRequest;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.dto.UpdateItineraryItemRequest;
import com.livo.api.modules.trip.dto.UpdateTripRequest;
import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import com.livo.api.modules.trip.repository.ItineraryItemRepository;
import com.livo.api.modules.trip.repository.TripRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
class TripManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ItineraryItemRepository itineraryItemRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "traveler_user_" + UUID.randomUUID();
        String email = "traveler_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Wanderlust Traveler")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should create trips, link to goals, and query with filters")
    void testCreateAndQueryTrips() throws Exception {
        // 1. Create Goal
        var goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Explore East Asia")
                .relatedArea("EXPERIENCES")
                .build());

        // 2. Create Leisure Trip: Tokyo & Kyoto
        CreateTripRequest trip1 = CreateTripRequest.builder()
                .goalId(goal.getId())
                .title("Tokyo & Kyoto Cherry Blossom")
                .destination("Japan")
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(1).plusDays(10))
                .tripType(TripType.LEISURE)
                .travelMode(TravelMode.FLIGHT)
                .accommodationType(AccommodationType.HOTEL)
                .travelWith(TravelWith.PARTNER)
                .budgetAmount(new BigDecimal("250000.00"))
                .currency("INR")
                .notes("Book JR Pass and Shinkansen tickets in advance")
                .build();

        mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trip1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Tokyo & Kyoto Cherry Blossom"))
                .andExpect(jsonPath("$.data.destination").value("Japan"))
                .andExpect(jsonPath("$.data.tripType").value("LEISURE"))
                .andExpect(jsonPath("$.data.travelMode").value("FLIGHT"))
                .andExpect(jsonPath("$.data.travelWith").value("PARTNER"))
                .andExpect(jsonPath("$.data.goalId").value(goal.getId().toString()));

        // 3. Create Business Trip: Bangalore DevConf
        CreateTripRequest trip2 = CreateTripRequest.builder()
                .title("Bangalore Tech Conference")
                .destination("Bangalore, India")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(7))
                .tripType(TripType.BUSINESS)
                .travelMode(TravelMode.FLIGHT)
                .accommodationType(AccommodationType.HOTEL)
                .travelWith(TravelWith.SOLO)
                .budgetAmount(new BigDecimal("20000.00"))
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trip2)))
                .andExpect(status().isCreated());

        // 4. Query all trips
        mockMvc.perform(get("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 5. Query by goalId
        mockMvc.perform(get("/api/v1/trips")
                        .param("goalId", goal.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Tokyo & Kyoto Cherry Blossom"));

        // 6. Query by tripType=BUSINESS
        mockMvc.perform(get("/api/v1/trips")
                        .param("tripType", "BUSINESS")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Bangalore Tech Conference"));

        // 7. Query upcoming trips
        mockMvc.perform(get("/api/v1/trips")
                        .param("upcoming", "true")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Should manage itinerary items and maintain chronological ordering")
    void testItineraryLifecycleAndChronologicalOrdering() throws Exception {
        LocalDate tripStart = LocalDate.now().plusWeeks(2);
        LocalDate tripEnd = tripStart.plusDays(5);

        CreateTripRequest tripReq = CreateTripRequest.builder()
                .title("Goa Weekend Retreat")
                .destination("Goa, India")
                .startDate(tripStart)
                .endDate(tripEnd)
                .build();

        MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID tripId = UUID.fromString(objectMapper.readTree(tripResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 1. Add Item 1: Day 1 Arrival at 14:00
        CreateItineraryItemRequest item1 = CreateItineraryItemRequest.builder()
                .itemDate(tripStart)
                .itemTime(LocalTime.of(14, 0))
                .title("Resort Check-in & Pool Relaxation")
                .location("Taj Fort Aguada")
                .notes("Check-in time 2 PM")
                .build();

        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Resort Check-in & Pool Relaxation"));

        // 2. Add Item 2: Day 2 Scuba Diving at 09:00
        CreateItineraryItemRequest item2 = CreateItineraryItemRequest.builder()
                .itemDate(tripStart.plusDays(1))
                .itemTime(LocalTime.of(9, 0))
                .title("Scuba Diving at Grande Island")
                .location("Grande Island Pier")
                .build();

        MvcResult item2Result = mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID item2Id = UUID.fromString(objectMapper.readTree(item2Result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 3. Add Item 3: Day 1 Sunset Dinner at 19:30 (should order between Day 1 14:00 and Day 2 09:00)
        CreateItineraryItemRequest item3 = CreateItineraryItemRequest.builder()
                .itemDate(tripStart)
                .itemTime(LocalTime.of(19, 30))
                .title("Sunset Dinner at Thalassa")
                .location("Vagator")
                .build();

        MvcResult item3Result = mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item3)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID item3Id = UUID.fromString(objectMapper.readTree(item3Result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 4. Verify Chronological Order: [Day 1 14:00, Day 1 19:30, Day 2 09:00]
        MvcResult listResult = mockMvc.perform(get("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn();

        JsonNode items = objectMapper.readTree(listResult.getResponse().getContentAsString()).path("data");
        assertThat(items.get(0).path("title").asText()).isEqualTo("Resort Check-in & Pool Relaxation");
        assertThat(items.get(1).path("title").asText()).isEqualTo("Sunset Dinner at Thalassa");
        assertThat(items.get(2).path("title").asText()).isEqualTo("Scuba Diving at Grande Island");

        // 5. Update Item 2
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/itinerary/" + item2Id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateItineraryItemRequest.builder()
                                .title("Private Yacht & Scuba")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Private Yacht & Scuba"));

        // 6. Delete Item 3
        mockMvc.perform(delete("/api/v1/trips/" + tripId + "/itinerary/" + item3Id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 7. Verify Trip reflects updated itinerary count
        mockMvc.perform(get("/api/v1/trips/" + tripId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itineraryCount").value(2));
    }

    @Test
    @DisplayName("Should aggregate trip summary with linked expenses, tasks, and events")
    void testTripSummaryWithExpensesAndTasks() throws Exception {
        LocalDate start = LocalDate.now().plusMonths(2);
        LocalDate end = start.plusDays(7);

        // 1. Create Trip with 80,000 INR budget
        CreateTripRequest tripReq = CreateTripRequest.builder()
                .title("Himachal Backpacking")
                .destination("Manali & Spiti")
                .startDate(start)
                .endDate(end)
                .budgetAmount(new BigDecimal("80000.00"))
                .currency("INR")
                .build();

        MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID tripId = UUID.fromString(objectMapper.readTree(tripResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Add Itinerary Item
        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateItineraryItemRequest.builder()
                                .itemDate(start)
                                .title("Overnight Volvo Bus to Manali")
                                .build())))
                .andExpect(status().isCreated());

        // 3. Log 2 Linked Transactions (Expenses)
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .tripId(tripId)
                                .title("Volvo Bus Booking")
                                .amount(new BigDecimal("4500.00"))
                                .type(TransactionType.EXPENSE)
                                .category("TRAVEL")
                                .paymentMethod(PaymentMethod.UPI)
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .tripId(tripId)
                                .title("Homestay Advance")
                                .amount(new BigDecimal("15500.00"))
                                .type(TransactionType.EXPENSE)
                                .category("ACCOMMODATION")
                                .paymentMethod(PaymentMethod.NET_BANKING)
                                .build())))
                .andExpect(status().isCreated());

        // 4. Create Linked Task
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTaskRequest.builder()
                                .tripId(tripId)
                                .title("Buy thermal wear and trekking boots")
                                .build())))
                .andExpect(status().isCreated());

        // 5. Create Linked Event
        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateEventRequest.builder()
                                .tripId(tripId)
                                .title("Bus Boarding at Kashmere Gate")
                                .startTime(Instant.now().plus(60, ChronoUnit.DAYS))
                                .endTime(Instant.now().plus(60, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                                .build())))
                .andExpect(status().isCreated());

        // 6. Fetch Trip Summary
        mockMvc.perform(get("/api/v1/trips/" + tripId + "/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trip.title").value("Himachal Backpacking"))
                .andExpect(jsonPath("$.data.totalExpenses").value(20000.00))
                .andExpect(jsonPath("$.data.trip.budgetRemaining").value(60000.00))
                .andExpect(jsonPath("$.data.expenseCount").value(2))
                .andExpect(jsonPath("$.data.linkedTaskCount").value(1))
                .andExpect(jsonPath("$.data.linkedEventCount").value(1))
                .andExpect(jsonPath("$.data.itinerary.length()").value(1))
                .andExpect(jsonPath("$.data.tripDurationDays").value(8));
    }

    @Test
    @DisplayName("Should update trip and cascade soft-delete to itinerary items")
    void testUpdateAndCascadeSoftDelete() throws Exception {
        LocalDate start = LocalDate.now().plusMonths(3);
        LocalDate end = start.plusDays(4);

        CreateTripRequest tripReq = CreateTripRequest.builder()
                .title("Kerala Backwaters")
                .destination("Alleppey")
                .startDate(start)
                .endDate(end)
                .budgetAmount(new BigDecimal("40000.00"))
                .build();

        MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID tripId = UUID.fromString(objectMapper.readTree(tripResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Add 2 itinerary items
        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateItineraryItemRequest.builder()
                                .itemDate(start)
                                .title("Houseboat check-in")
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateItineraryItemRequest.builder()
                                .itemDate(start.plusDays(1))
                                .title("Ayurvedic spa session")
                                .build())))
                .andExpect(status().isCreated());

        // Update Trip
        mockMvc.perform(put("/api/v1/trips/" + tripId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateTripRequest.builder()
                                .destination("Alleppey & Munnar")
                                .budgetAmount(new BigDecimal("55000.00"))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.destination").value("Alleppey & Munnar"))
                .andExpect(jsonPath("$.data.budgetAmount").value(55000.00));

        // Soft-delete Trip
        mockMvc.perform(delete("/api/v1/trips/" + tripId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Verify Trip is 404
        mockMvc.perform(get("/api/v1/trips/" + tripId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // Verify Itinerary items query is 404
        mockMvc.perform(get("/api/v1/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // Verify in DB that trip and its itinerary items have deletedAt set
        assertThat(tripRepository.findByIdAndUserIdAndDeletedAtIsNull(tripId, userId)).isEmpty();
        assertThat(itineraryItemRepository.findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(tripId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should validate constraints and ensure multi-tenant user isolation")
    void testValidationAndUserIsolation() throws Exception {
        // 1. Validation: endDate before startDate
        CreateTripRequest invalidDates = CreateTripRequest.builder()
                .title("Inverted Dates Trip")
                .destination("Nowhere")
                .startDate(LocalDate.now().plusWeeks(2))
                .endDate(LocalDate.now().plusWeeks(1))
                .build();

        mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDates)))
                .andExpect(status().isBadRequest());

        // 2. Create valid trip
        CreateTripRequest validTrip = CreateTripRequest.builder()
                .title("Valid Paris Getaway")
                .destination("Paris, France")
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(1).plusDays(5))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTrip)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID validTripId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 3. Validation: Itinerary item date outside trip dates
        CreateItineraryItemRequest outOfBoundsItem = CreateItineraryItemRequest.builder()
                .itemDate(LocalDate.now()) // Way before trip start
                .title("Early item")
                .build();

        mockMvc.perform(post("/api/v1/trips/" + validTripId + "/itinerary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outOfBoundsItem)))
                .andExpect(status().isBadRequest());

        // 4. User isolation
        String otherUid = "other_traveler_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_traveler_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Traveler")
                .build());
        String otherToken = otherAuth.getAccessToken();

        mockMvc.perform(get("/api/v1/trips/" + validTripId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/trips/" + validTripId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
