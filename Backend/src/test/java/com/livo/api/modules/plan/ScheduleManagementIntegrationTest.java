package com.livo.api.modules.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.dto.UpdateEventRequest;
import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.plan.dto.ConflictCheckRequest;
import com.livo.api.modules.plan.dto.CreateScheduleBlockRequest;
import com.livo.api.modules.plan.dto.UpdateScheduleBlockRequest;
import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import com.livo.api.modules.plan.repository.ScheduleBlockRepository;
import com.livo.api.modules.routine.dto.CreateRoutineRequest;
import com.livo.api.modules.routine.dto.UpdateRoutineRequest;
import com.livo.api.modules.routine.entity.RoutineEntity;
import com.livo.api.modules.routine.repository.RoutineRepository;
import com.livo.api.modules.task.dto.CreateTaskRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
class ScheduleManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ScheduleBlockRepository scheduleBlockRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "sched_user_" + UUID.randomUUID();
        String email = "sched_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Scheduler User")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should perform full CRUD on calendar events")
    void testCreateAndQueryEvents() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Quarterly Board Meeting")
                .description("Discussion on annual targets")
                .location("Zoom Room 1")
                .format(EventFormat.ONLINE)
                .priority(EventPriority.HIGH)
                .category("WORK")
                .startTime(start)
                .endTime(end)
                .reminderMinutes(15)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Quarterly Board Meeting"))
                .andExpect(jsonPath("$.data.format").value("ONLINE"))
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Get by ID
        mockMvc.perform(get("/api/v1/events/" + eventId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.location").value("Zoom Room 1"));

        // Update
        UpdateEventRequest updateReq = UpdateEventRequest.builder()
                .location("In-Person Conference Room A")
                .format(EventFormat.IN_PERSON)
                .build();

        mockMvc.perform(put("/api/v1/events/" + eventId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.location").value("In-Person Conference Room A"))
                .andExpect(jsonPath("$.data.format").value("IN_PERSON"));

        // Delete
        mockMvc.perform(delete("/api/v1/events/" + eventId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        EventEntity deleted = eventRepository.findById(eventId).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should reject event when end time is before start time with 400 Bad Request")
    void testEventValidationRejectsEndTimeBeforeStartTime() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant end = start.minus(30, ChronoUnit.MINUTES);

        CreateEventRequest invalidReq = CreateEventRequest.builder()
                .title("Impossible Meeting")
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should perform full CRUD on routines")
    void testCreateAndQueryRoutines() throws Exception {
        CreateRoutineRequest routineReq = CreateRoutineRequest.builder()
                .title("Morning Sunlight & Meditation")
                .startTime(LocalTime.of(6, 30))
                .endTime(LocalTime.of(7, 15))
                .daysOfWeek(List.of(1, 2, 3, 4, 5, 6, 7))
                .isActive(true)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routineReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Morning Sunlight & Meditation"))
                .andReturn();

        UUID routineId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Update Routine
        UpdateRoutineRequest updateReq = UpdateRoutineRequest.builder()
                .endTime(LocalTime.of(7, 30))
                .build();

        mockMvc.perform(put("/api/v1/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endTime").value("07:30:00"));

        RoutineEntity routine = routineRepository.findByIdAndUserIdAndDeletedAtIsNull(routineId, userId).orElseThrow();
        assertThat(routine.getEndTime()).isEqualTo(LocalTime.of(7, 30));

        // Delete Routine
        mockMvc.perform(delete("/api/v1/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        assertThat(routineRepository.findByIdAndUserIdAndDeletedAtIsNull(routineId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should reject routine creation and update with identical or inverted times")
    void testRoutineRejectsInvertedTimes() throws Exception {
        // 1. Equal times: 08:00 to 08:00 -> 400 Bad Request
        CreateRoutineRequest equalReq = CreateRoutineRequest.builder()
                .title("Equal Times Routine")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 0))
                .build();

        mockMvc.perform(post("/api/v1/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equalReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Routine end time must be after start time")));

        // 2. Inverted times: 14:00 to 09:00 -> 400 Bad Request
        CreateRoutineRequest invertedReq = CreateRoutineRequest.builder()
                .title("Inverted Times Routine")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        mockMvc.perform(post("/api/v1/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invertedReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Routine end time must be after start time")));

        // 3. Create valid routine, then update with inverted times -> 400 Bad Request
        CreateRoutineRequest validReq = CreateRoutineRequest.builder()
                .title("Valid Routine")
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(8, 0))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID routineId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Update with inverted time: start=07:00, new end=06:00
        UpdateRoutineRequest invalidUpdate = UpdateRoutineRequest.builder()
                .endTime(LocalTime.of(6, 0))
                .build();

        mockMvc.perform(put("/api/v1/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Routine end time must be after start time")));

        // Cleanup
        routineRepository.deleteById(routineId);
    }

    @Test
    @DisplayName("Should create, retrieve, update, and delete schedule blocks")
    void testScheduleBlockCrud() throws Exception {
        LocalDate today = LocalDate.now();

        CreateScheduleBlockRequest blockReq = CreateScheduleBlockRequest.builder()
                .blockDate(today)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .title("Deep Focus Coding")
                .category("DEVELOPMENT")
                .isLocked(true)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Deep Focus Coding"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andReturn();

        UUID blockId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Query by date
        mockMvc.perform(get("/api/v1/schedule-blocks?date=" + today)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(blockId.toString()));

        // Update block
        UpdateScheduleBlockRequest updateReq = UpdateScheduleBlockRequest.builder()
                .title("Deep Focus: API Refactoring")
                .build();

        mockMvc.perform(put("/api/v1/schedule-blocks/" + blockId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Deep Focus: API Refactoring"));

        // Delete block
        mockMvc.perform(delete("/api/v1/schedule-blocks/" + blockId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        assertThat(scheduleBlockRepository.findByIdAndUserIdAndDeletedAtIsNull(blockId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should aggregate daily plan timeline and calculate workload correctly")
    void testDailyPlanAggregationAndWorkloadCalculation() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Add Task (duration 60 mins)
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Unit Test Review")
                .dueDate(today)
                .durationMins(60)
                .build());

        // 2. Add Schedule Block (120 mins: 14:00 to 16:00)
        ScheduleBlockEntity block = ScheduleBlockEntity.builder()
                .blockDate(today)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .title("Sprint Retrospective Block")
                .build();
        block.setUserId(userId);
        block.setVersion(1L);
        scheduleBlockRepository.save(block);

        // 3. Add Calendar Event
        Instant eventStart = today.atTime(11, 0).atZone(java.time.ZoneId.of("UTC")).toInstant();
        Instant eventEnd = today.atTime(12, 0).atZone(java.time.ZoneId.of("UTC")).toInstant();
        EventEntity event = EventEntity.builder()
                .title("Team Standup")
                .startTime(eventStart)
                .endTime(eventEnd)
                .build();
        event.setUserId(userId);
        event.setVersion(1L);
        eventRepository.save(event);

        // 4. Add Routine running today
        RoutineEntity routine = RoutineEntity.builder()
                .title("Evening Wind-down")
                .startTime(LocalTime.of(21, 30))
                .endTime(LocalTime.of(22, 0))
                .daysOfWeek(List.of(today.getDayOfWeek().getValue()))
                .isActive(true)
                .build();
        routine.setUserId(userId);
        routine.setVersion(1L);
        routineRepository.save(routine);

        // 5. Call GET /api/v1/plan/daily
        MvcResult planResult = mockMvc.perform(get("/api/v1/plan/daily?date=" + today)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value(today.toString()))
                .andExpect(jsonPath("$.data.tasks.length()").value(1))
                .andExpect(jsonPath("$.data.scheduleBlocks.length()").value(1))
                .andExpect(jsonPath("$.data.events.length()").value(1))
                .andExpect(jsonPath("$.data.routines.length()").value(1))
                .andReturn();

        JsonNode planJson = objectMapper.readTree(planResult.getResponse().getContentAsString()).path("data");

        // Workload = (60 task mins + 120 block mins) / 60 = 3.00 hours
        BigDecimal workload = new BigDecimal(planJson.path("plannedWorkloadHours").asText());
        assertThat(workload).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(planJson.path("overloaded").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("Should reject schedule block with inverted times (end before or equal start) with HTTP 400")
    void testScheduleBlockRejectsInvertedTimes() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Equal times
        CreateScheduleBlockRequest equalReq = CreateScheduleBlockRequest.builder()
                .blockDate(today)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 0))
                .title("Zero Duration Block")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equalReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("after start time")));

        // 2. Inverted times
        CreateScheduleBlockRequest invertedReq = CreateScheduleBlockRequest.builder()
                .blockDate(today)
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(13, 0))
                .title("Inverted Block")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invertedReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("after start time")));
    }

    @Test
    @DisplayName("Should detect schedule block collisions on same date and throw HTTP 409 Conflict")
    void testScheduleBlockConflictDetection() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 1. Create Base Block (10:00 - 12:00)
        CreateScheduleBlockRequest baseReq = CreateScheduleBlockRequest.builder()
                .blockDate(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .title("Deep Work: Core Engine")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Deep Work: Core Engine"));

        // 2. Back-to-back block (12:00 - 13:00) -> MUST SUCCEED (no overlap)
        CreateScheduleBlockRequest backToBackReq = CreateScheduleBlockRequest.builder()
                .blockDate(tomorrow)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .title("Lunch Block")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backToBackReq)))
                .andExpect(status().isCreated());

        // 3. Overlapping Block (11:00 - 12:30) -> MUST FAIL with 409 Conflict
        CreateScheduleBlockRequest overlappingReq = CreateScheduleBlockRequest.builder()
                .blockDate(tomorrow)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 30))
                .title("Conflicting Team Meeting")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Deep Work: Core Engine")));

        // 4. Overlapping Block on DIFFERENT date (10:30 - 11:30 yesterday) -> MUST SUCCEED
        CreateScheduleBlockRequest diffDateReq = CreateScheduleBlockRequest.builder()
                .blockDate(tomorrow.plusDays(1))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .title("Next Day Block")
                .build();

        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diffDateReq)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should detect conflict on updating schedule block to overlap another block")
    void testScheduleBlockUpdateConflictDetection() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(2);

        // Block A: 09:00 - 11:00
        MvcResult blockAResult = mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .blockDate(targetDate)
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(11, 0))
                                .title("Block A")
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID blockAId = UUID.fromString(objectMapper.readTree(blockAResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Block B: 14:00 - 16:00
        MvcResult blockBResult = mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .blockDate(targetDate)
                                .startTime(LocalTime.of(14, 0))
                                .endTime(LocalTime.of(16, 0))
                                .title("Block B")
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        UUID blockBId = UUID.fromString(objectMapper.readTree(blockBResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Update Block B to overlap Block A (move to 10:00 - 12:00) -> MUST FAIL with 409
        mockMvc.perform(put("/api/v1/schedule-blocks/" + blockBId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateScheduleBlockRequest.builder()
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(12, 0))
                                .build())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Block A")));

        // Update Block B title without changing time (self-overlap) -> MUST SUCCEED
        mockMvc.perform(put("/api/v1/schedule-blocks/" + blockBId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateScheduleBlockRequest.builder()
                                .title("Block B Renamed")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Block B Renamed"));
    }

    @Test
    @DisplayName("Should return 7-day week summary strip with workload capacity labels")
    void testWeekSummaryEndpoint() throws Exception {
        LocalDate start = LocalDate.now();
        mockMvc.perform(get("/api/v1/plan/week-summary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", start.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startDate").value(start.toString()))
                .andExpect(jsonPath("$.data.days").isArray())
                .andExpect(jsonPath("$.data.days.length()").value(7))
                .andExpect(jsonPath("$.data.days[0].capacityLabel").exists())
                .andExpect(jsonPath("$.data.averageCapacityPercentage").exists());
    }

    @Test
    @DisplayName("Should detect conflicts via check endpoint and allow keep-both override")
    void testConflictCheckAndKeepBoth() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);

        // 1. Create a block 10:00 - 11:30
        mockMvc.perform(post("/api/v1/schedule-blocks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .blockDate(date)
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(11, 30))
                                .title("Morning Focus")
                                .build())))
                .andExpect(status().isCreated());

        // 2. Conflict check for overlapping time 10:30 - 11:00
        mockMvc.perform(post("/api/v1/plan/conflicts/check")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ConflictCheckRequest.builder()
                                .blockDate(date)
                                .startTime(LocalTime.of(10, 30))
                                .endTime(LocalTime.of(11, 0))
                                .requestedDurationMinutes(30)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(true))
                .andExpect(jsonPath("$.data.conflictDetails[0]").value(org.hamcrest.Matchers.containsString("Morning Focus")))
                .andExpect(jsonPath("$.data.suggestedSlots").isArray());

        // 3. Keep-both override allows creation without error
        mockMvc.perform(post("/api/v1/plan/conflicts/keep-both")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateScheduleBlockRequest.builder()
                                .blockDate(date)
                                .startTime(LocalTime.of(10, 30))
                                .endTime(LocalTime.of(11, 0))
                                .title("Overlapping Quick Sync")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Overlapping Quick Sync"));
    }

    @Test
    @DisplayName("Should auto plan my day and rebalance overloaded day")
    void testAutoPlanMyDayAndRebalance() throws Exception {
        LocalDate date = LocalDate.now().plusDays(4);

        // Auto plan my day
        mockMvc.perform(post("/api/v1/plan/my-day")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.date").value(date.toString()));

        // Day rebalance
        mockMvc.perform(post("/api/v1/plan/rebalance")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDate").value(date.toString()))
                .andExpect(jsonPath("$.data.explanation").exists());
    }

    @Test
    @DisplayName("Should get user profile productivity stats via /api/v1/user/stats and /api/v1/users/stats")
    void testUserStatsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/user/stats")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasksCompletedTotal").exists())
                .andExpect(jsonPath("$.data.activeGoalsTotal").exists())
                .andExpect(jsonPath("$.data.currentDayStreak").exists());

        mockMvc.perform(get("/api/v1/users/stats")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasksCompletedTotal").exists());
    }
}

