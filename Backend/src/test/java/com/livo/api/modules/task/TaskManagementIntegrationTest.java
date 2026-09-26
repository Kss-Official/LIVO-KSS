package com.livo.api.modules.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.category.dto.CreateCategoryRequest;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import com.livo.api.modules.tag.dto.CreateTagRequest;
import com.livo.api.modules.task.dto.CreateSubtaskInlineRequest;
import com.livo.api.modules.task.dto.CreateSubtaskRequest;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.RescheduleTaskRequest;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
import com.livo.api.modules.trip.entity.TripEntity;
import com.livo.api.modules.trip.repository.TripRepository;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.SubtaskRepository;
import com.livo.api.modules.task.repository.TaskRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private TripRepository tripRepository;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "task_test_user_" + UUID.randomUUID();
        String email = "task_user_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Task Manager")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should create a task with inline subtasks and tags")
    void testCreateTaskWithInlineSubtasksAndTags() throws Exception {
        // 1. Create Tag
        CreateTagRequest tagReq = CreateTagRequest.builder()
                .name("UrgentTag")
                .colorHex("#EF4444")
                .build();

        MvcResult tagResult = mockMvc.perform(post("/api/v1/tags")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID tagId = UUID.fromString(objectMapper.readTree(tagResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Create Task
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Complete Q3 Financial Review")
                .description("Review balance sheet and tax deductions")
                .projectLabel("Finance 2026")
                .dueDate(LocalDate.now())
                .dueTime(LocalTime.of(15, 0))
                .durationMins(45)
                .priority(TaskPriority.HIGH)
                .category("FINANCE")
                .subtasks(List.of(
                        CreateSubtaskInlineRequest.builder().title("Gather bank statements").sortOrder(1).build(),
                        CreateSubtaskInlineRequest.builder().title("Calculate net profit").sortOrder(2).build()
                ))
                .tagIds(List.of(tagId))
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Complete Q3 Financial Review"))
                .andExpect(jsonPath("$.data.durationMins").value(45))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.subtasks.length()").value(2))
                .andExpect(jsonPath("$.data.tags.length()").value(1))
                .andExpect(jsonPath("$.data.tags[0].name").value("UrgentTag"))
                .andReturn();

        JsonNode taskJson = objectMapper.readTree(taskResult.getResponse().getContentAsString());
        UUID taskId = UUID.fromString(taskJson.path("data").path("id").asText());

        TaskEntity persisted = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(taskId, userId).orElseThrow();
        assertThat(persisted.getTitle()).isEqualTo("Complete Q3 Financial Review");
        assertThat(persisted.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Should start, complete, and un-complete a task")
    void testToggleTaskCompletionAndStatusTransitions() throws Exception {
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Draft Architecture Plan")
                .durationMins(60)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 1. Start Task -> IN_PROGRESS
        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/start")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.startedAt").isNotEmpty());

        // 2. Complete Task -> COMPLETED
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());

        // 3. Toggle back -> TODO
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.completedAt").isEmpty());
    }

    @Test
    @DisplayName("Should add, toggle, and delete subtasks")
    void testSubtaskCrudAndToggle() throws Exception {
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Deploy Backend Release")
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(taskResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 1. Add Subtask
        CreateSubtaskRequest subtaskReq = CreateSubtaskRequest.builder()
                .title("Run smoke test")
                .sortOrder(1)
                .build();

        MvcResult subResult = mockMvc.perform(post("/api/v1/tasks/" + taskId + "/subtasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subtaskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Run smoke test"))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andReturn();

        UUID subtaskId = UUID.fromString(objectMapper.readTree(subResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Toggle Subtask -> isCompleted = true
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/subtasks/" + subtaskId + "/toggle")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        // 3. Delete Subtask
        mockMvc.perform(delete("/api/v1/tasks/" + taskId + "/subtasks/" + subtaskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(subtaskRepository.findByIdAndUserIdAndDeletedAtIsNull(subtaskId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should reschedule task to tomorrow or drop")
    void testRescheduleTaskActions() throws Exception {
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Daily Exercise")
                .dueDate(LocalDate.now())
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(taskResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Reschedule to TOMORROW
        RescheduleTaskRequest tomorrowReq = RescheduleTaskRequest.builder()
                .action("TOMORROW")
                .build();

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/reschedule")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tomorrowReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dueDate").value(LocalDate.now().plusDays(1).toString()));

        // Reschedule action DROP -> CANCELLED
        RescheduleTaskRequest dropReq = RescheduleTaskRequest.builder()
                .action("DROP")
                .build();

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/reschedule")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dropReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should create and retrieve custom categories")
    void testCategoriesEndpoints() throws Exception {
        CreateCategoryRequest categoryReq = CreateCategoryRequest.builder()
                .name("Health & Fitness")
                .iconKey("heart")
                .colorHex("#10B981")
                .domainType(CategoryDomainType.TASK)
                .build();

        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Health & Fitness"))
                .andExpect(jsonPath("$.data.domainType").value("TASK"))
                .andReturn();

        UUID catId = UUID.fromString(objectMapper.readTree(catResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        mockMvc.perform(get("/api/v1/categories?domainType=TASK")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(delete("/api/v1/categories/" + catId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should soft delete a task")
    void testDeleteTaskSoftDeletesTaskAndSubtasks() throws Exception {
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Task to Delete")
                .subtasks(List.of(CreateSubtaskInlineRequest.builder().title("Sub 1").build()))
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(taskResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        mockMvc.perform(delete("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        TaskEntity deleted = taskRepository.findById(taskId).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should not re-fire TaskCompletedEvent or corrupt completedAt when editing an already completed task")
    void testEditingCompletedTaskDoesNotRecompleteOrRefire() throws Exception {
        // 1. Create a task
        CreateTaskRequest taskReq = CreateTaskRequest.builder()
                .title("Initial Task")
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(taskResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Complete the task
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                                .status(TaskStatus.COMPLETED)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        TaskEntity completedTask = taskRepository.findById(taskId).orElseThrow();
        java.time.Instant initialCompletedAt = completedTask.getCompletedAt();
        assertThat(initialCompletedAt).isNotNull();

        Thread.sleep(50);

        // 3. Edit title and description on the ALREADY completed task without changing status
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                                .title("Updated Title on Completed Task")
                                .description("Added extra details after completion")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title on Completed Task"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        TaskEntity editedTask = taskRepository.findById(taskId).orElseThrow();
        // completedAt must be preserved and not modified
        assertThat(editedTask.getCompletedAt()).isEqualTo(initialCompletedAt);
    }

    @Test
    @DisplayName("Should return 404 when creating task with non-existent goalId")
    void testCreateTaskWithNonExistentGoalFailsNotFound() throws Exception {
        UUID nonExistentGoalId = UUID.randomUUID();
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task with Bad Goal")
                .goalId(nonExistentGoalId)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Goal not found")));
    }

    @Test
    @DisplayName("Should return 404 when creating task with soft-deleted goalId")
    void testCreateTaskWithSoftDeletedGoalFailsNotFound() throws Exception {
        GoalEntity goal = GoalEntity.builder()
                .title("Deleted Goal")
                .build();
        goal.setUserId(userId);
        goal.setVersion(1L);
        goal = goalRepository.saveAndFlush(goal);
        goal.markDeleted();
        goal = goalRepository.saveAndFlush(goal);

        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task with Deleted Goal")
                .goalId(goal.getId())
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Goal not found")));
    }

    @Test
    @DisplayName("Should return 404 when creating task with non-existent tripId")
    void testCreateTaskWithNonExistentTripFailsNotFound() throws Exception {
        UUID nonExistentTripId = UUID.randomUUID();
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task with Bad Trip")
                .tripId(nonExistentTripId)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));
    }

    @Test
    @DisplayName("Should return 400 when milestone does not belong to specified goalId")
    void testCreateTaskWithMismatchedMilestoneAndGoalFailsBadRequest() throws Exception {
        GoalEntity goal1 = GoalEntity.builder().title("Goal 1").build();
        goal1.setUserId(userId);
        goal1.setVersion(1L);
        goal1 = goalRepository.saveAndFlush(goal1);

        GoalEntity goal2 = GoalEntity.builder().title("Goal 2").build();
        goal2.setUserId(userId);
        goal2.setVersion(1L);
        goal2 = goalRepository.saveAndFlush(goal2);

        MilestoneEntity milestone = MilestoneEntity.builder()
                .goalId(goal1.getId())
                .title("Milestone for Goal 1")
                .build();
        milestone.setUserId(userId);
        milestone.setVersion(1L);
        milestone = milestoneRepository.saveAndFlush(milestone);

        // Request with milestone belonging to Goal 1, but goalId = Goal 2
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Mismatched Task")
                .goalId(goal2.getId())
                .milestoneId(milestone.getId())
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("belongs to goal")));
    }

    @Test
    @DisplayName("Should auto-populate goalId when milestoneId is provided without goalId")
    void testCreateTaskWithMilestoneAutoResolvesGoalId() throws Exception {
        GoalEntity goal = GoalEntity.builder().title("Parent Goal").build();
        goal.setUserId(userId);
        goal.setVersion(1L);
        goal = goalRepository.saveAndFlush(goal);

        MilestoneEntity milestone = MilestoneEntity.builder()
                .goalId(goal.getId())
                .title("Milestone")
                .build();
        milestone.setUserId(userId);
        milestone.setVersion(1L);
        milestone = milestoneRepository.saveAndFlush(milestone);

        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Auto Linked Task")
                .milestoneId(milestone.getId())
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.milestoneId").value(milestone.getId().toString()))
                .andExpect(jsonPath("$.data.goalId").value(goal.getId().toString()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID createdTaskId = UUID.fromString(json.path("data").path("id").asText());

        TaskEntity persisted = taskRepository.findById(createdTaskId).orElseThrow();
        assertThat(persisted.getGoalId()).isEqualTo(goal.getId());
        assertThat(persisted.getMilestoneId()).isEqualTo(milestone.getId());
    }

    @Test
    @DisplayName("Should validate foreign keys when updating an existing task")
    void testUpdateTaskWithInvalidForeignKeysFails() throws Exception {
        CreateTaskRequest createReq = CreateTaskRequest.builder()
                .title("Original Task")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Update with non-existent goalId
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                                .goalId(UUID.randomUUID())
                                .build())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Goal not found")));

        // Update with non-existent tripId
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(com.livo.api.modules.task.dto.UpdateTaskRequest.builder()
                                .tripId(UUID.randomUUID())
                                .build())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trip not found")));
    }
}
