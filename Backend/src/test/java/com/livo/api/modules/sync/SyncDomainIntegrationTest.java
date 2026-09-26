package com.livo.api.modules.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.sync.dto.OfflineMutationItemRequest;
import com.livo.api.modules.sync.dto.ResolveConflictRequest;
import com.livo.api.modules.sync.dto.SyncPushRequest;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
import com.livo.api.modules.sync.repository.OfflineMutationRepository;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.entity.TaskEntity;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SyncDomainIntegrationTest {

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
    private com.livo.api.modules.habit.repository.HabitRepository habitRepository;

    @Autowired
    private com.livo.api.modules.goal.repository.GoalRepository goalRepository;

    @Autowired
    private OfflineMutationRepository offlineMutationRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "sync_user_" + UUID.randomUUID();
        String email = "sync_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Jordan Sync")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should batch push mutations, create entities, and enforce per-user idempotency")
    void testBatchPushAndIdempotency() throws Exception {
        String mutKey1 = "idemp_task_" + UUID.randomUUID();
        String mutKey2 = "idemp_expense_" + UUID.randomUUID();

        OfflineMutationItemRequest mut1 = OfflineMutationItemRequest.builder()
                .idempotencyKey(mutKey1)
                .domainEntity("TASK")
                .action("CREATE")
                .clientTimestamp(Instant.now().minusSeconds(120))
                .payload(Map.of("title", "Offline Created Task", "priority", "HIGH"))
                .build();

        OfflineMutationItemRequest mut2 = OfflineMutationItemRequest.builder()
                .idempotencyKey(mutKey2)
                .domainEntity("EXPENSE")
                .action("CREATE")
                .clientTimestamp(Instant.now().minusSeconds(110))
                .payload(Map.of("title", "Team Lunch", "amount", "450.00", "currency", "INR"))
                .build();

        SyncPushRequest pushRequest = SyncPushRequest.builder()
                .mutations(List.of(mut1, mut2))
                .build();

        // First push: both should be applied
        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appliedCount").value(2))
                .andExpect(jsonPath("$.data.conflictCount").value(0))
                .andExpect(jsonPath("$.data.results[0].status").value("APPLIED"))
                .andExpect(jsonPath("$.data.results[1].status").value("APPLIED"));

        // Second push with same idempotency keys: should return cached responses without duplicating
        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appliedCount").value(2))
                .andExpect(jsonPath("$.data.results[0].message").value("Idempotent response: mutation was previously processed"))
                .andExpect(jsonPath("$.data.results[1].message").value("Idempotent response: mutation was previously processed"));

        // Verify task exists in DB
        List<TaskEntity> tasks = taskRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        assertThat(tasks.stream().anyMatch(t -> "Offline Created Task".equals(t.getTitle()))).isTrue();
    }

    @Test
    @DisplayName("Should detect optimistic concurrency conflict when server version is newer")
    void testOptimisticConcurrencyConflictDetection() throws Exception {
        // 1. Create task on server (version = 1)
        var createdTask = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Initial Task Title")
                .build());

        // 2. Simulate server-side concurrent update (version becomes 2)
        taskService.updateTask(userId, createdTask.getId(), com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                .title("Updated Title On Web App")
                .build());
        TaskEntity serverTask = taskRepository.findByIdAndUserId(createdTask.getId(), userId).orElseThrow();

        // 3. Client comes online and pushes mutation based on stale version 1
        String mutKey = "stale_mut_" + UUID.randomUUID();
        OfflineMutationItemRequest staleMut = OfflineMutationItemRequest.builder()
                .idempotencyKey(mutKey)
                .domainEntity("TASK")
                .action("UPDATE")
                .entityId(serverTask.getId())
                .baseVersion(1L) // Stale base version!
                .clientTimestamp(Instant.now().minusSeconds(60))
                .payload(Map.of("title", "Stale Mobile Offline Edit"))
                .build();

        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SyncPushRequest.builder().mutations(List.of(staleMut)).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conflictCount").value(1))
                .andExpect(jsonPath("$.data.results[0].status").value("CONFLICT"))
                .andExpect(jsonPath("$.data.results[0].conflictDetails.serverVersion").value(2))
                .andExpect(jsonPath("$.data.results[0].conflictDetails.clientBaseVersion").value(1));

        // 4. Verify server title was NOT overwritten
        TaskEntity unchanged = taskRepository.findByIdAndUserId(serverTask.getId(), userId).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Updated Title On Web App");
    }

    @Test
    @DisplayName("Should reconcile conflict using SERVER_WINS and CLIENT_WINS strategies")
    void testConflictReconciliationStrategies() throws Exception {
        // 1. Setup a conflict mutation
        var task = taskService.createTask(userId, CreateTaskRequest.builder().title("Original Subject").build());
        taskService.updateTask(userId, task.getId(), com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                .title("Server Modified Title")
                .build());
        TaskEntity entity = taskRepository.findByIdAndUserId(task.getId(), userId).orElseThrow();

        String mutKey = "conflict_resolve_" + UUID.randomUUID();
        OfflineMutationItemRequest conflictMut = OfflineMutationItemRequest.builder()
                .idempotencyKey(mutKey)
                .domainEntity("TASK")
                .action("UPDATE")
                .entityId(entity.getId())
                .baseVersion(1L)
                .clientTimestamp(Instant.now())
                .payload(Map.of("title", "Client Preferred Title"))
                .build();

        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SyncPushRequest.builder().mutations(List.of(conflictMut)).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conflictCount").value(1));

        // 2. Resolve with CLIENT_WINS strategy
        ResolveConflictRequest clientWinsReq = ResolveConflictRequest.builder()
                .idempotencyKey(mutKey)
                .strategy("CLIENT_WINS")
                .build();

        mockMvc.perform(post("/api/v1/sync/resolve-conflict")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientWinsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.message").value("Resolved: Client/merged version force-applied"));

        // Verify entity updated to client title
        TaskEntity resolved = taskRepository.findByIdAndUserId(entity.getId(), userId).orElseThrow();
        assertThat(resolved.getTitle()).isEqualTo("Client Preferred Title");
        assertThat(resolved.getVersion()).isGreaterThan(2L);
    }

    @Test
    @DisplayName("Should pull delta changes since timestamp including soft-deleted entities")
    void testDeltaSyncPull() throws Exception {
        Instant before = Instant.now().minusSeconds(5);

        // 1. Create a task and soft-delete another
        var task1 = taskService.createTask(userId, CreateTaskRequest.builder().title("Delta Task 1").build());
        var task2 = taskService.createTask(userId, CreateTaskRequest.builder().title("Task to Delete").build());
        taskService.deleteTask(userId, task2.getId());

        // 2. Delta pull
        MvcResult pullResult = mockMvc.perform(get("/api/v1/sync/pull")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("since", before.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tasks").isNotEmpty())
                .andReturn();

        int totalChanges = objectMapper.readTree(pullResult.getResponse().getContentAsString())
                .path("data").path("totalChanges").asInt();
        assertThat(totalChanges).isGreaterThanOrEqualTo(2);

        // 3. Verify audit log retrieval
        mockMvc.perform(get("/api/v1/sync/mutations")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should enforce strict multi-tenant isolation on offline mutations")
    void testMultiTenantSyncIsolation() throws Exception {
        String mutKeyA = "user_a_mut_" + UUID.randomUUID();
        OfflineMutationItemRequest mutA = OfflineMutationItemRequest.builder()
                .idempotencyKey(mutKeyA)
                .domainEntity("TASK")
                .action("CREATE")
                .clientTimestamp(Instant.now())
                .payload(Map.of("title", "User A Private Task"))
                .build();

        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SyncPushRequest.builder().mutations(List.of(mutA)).build())))
                .andExpect(status().isOk());

        // User B
        String uidB = "sync_b_" + UUID.randomUUID();
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email("b_" + UUID.randomUUID() + "@livo.test")
                .fullName("User B")
                .timezone("UTC")
                .build());

        // User B cannot see User A's mutations
        mockMvc.perform(get("/api/v1/sync/mutations")
                        .header("Authorization", "Bearer " + authB.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should explicitly REJECT unsupported domain mutations without silent data loss")
    void testUnsupportedDomainIsExplicitlyRejectedWithoutSilentLoss() throws Exception {
        String idempotencyKey = "unsupported_domain_" + UUID.randomUUID();
        OfflineMutationItemRequest mutation = OfflineMutationItemRequest.builder()
                .idempotencyKey(idempotencyKey)
                .domainEntity("NOTE")
                .action("CREATE")
                .clientTimestamp(Instant.now())
                .payload(Map.of("content", "Secret offline note"))
                .build();

        SyncPushRequest request = SyncPushRequest.builder()
                .mutations(List.of(mutation))
                .build();

        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rejectedCount").value(1))
                .andExpect(jsonPath("$.data.appliedCount").value(0))
                .andExpect(jsonPath("$.data.results[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.data.results[0].message").value("Domain 'NOTE' is not supported for offline synchronization"));
    }

    @Test
    @DisplayName("Should update Habit entity in database when conflict is resolved with CLIENT_WINS")
    void testHabitConflictResolutionClientWinsUpdatesDatabase() throws Exception {
        // 1. Create a habit in DB
        com.livo.api.modules.habit.entity.HabitEntity habit = com.livo.api.modules.habit.entity.HabitEntity.builder()
                .title("Initial Habit Title")
                .frequencyType(com.livo.api.modules.habit.entity.enums.HabitFrequency.DAILY)
                .startDate(java.time.LocalDate.now())
                .build();
        habit.setUserId(userId);
        habit.setVersion(2L);
        habit = habitRepository.save(habit);

        // 2. Push a conflicting mutation (baseVersion 1L while server is at 2L)
        String idempotencyKey = "habit_conf_" + UUID.randomUUID();
        OfflineMutationItemRequest mutation = OfflineMutationItemRequest.builder()
                .idempotencyKey(idempotencyKey)
                .domainEntity("HABIT")
                .action("UPDATE")
                .entityId(habit.getId())
                .baseVersion(1L)
                .clientTimestamp(Instant.now())
                .payload(Map.of("title", "Client Won Habit Title"))
                .build();

        mockMvc.perform(post("/api/v1/sync/push")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SyncPushRequest.builder().mutations(List.of(mutation)).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conflictCount").value(1));

        // 3. Resolve conflict with CLIENT_WINS
        ResolveConflictRequest resolveReq = ResolveConflictRequest.builder()
                .idempotencyKey(idempotencyKey)
                .strategy("CLIENT_WINS")
                .build();

        mockMvc.perform(post("/api/v1/sync/resolve-conflict")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"));

        // 4. Verify the habit entity in DB was actually updated!
        com.livo.api.modules.habit.entity.HabitEntity updated = habitRepository.findById(habit.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Client Won Habit Title");
    }
}
