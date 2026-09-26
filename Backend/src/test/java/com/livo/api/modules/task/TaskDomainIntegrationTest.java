package com.livo.api.modules.task;

import com.livo.api.modules.tag.entity.TagEntity;
import com.livo.api.modules.tag.repository.TagRepository;
import com.livo.api.modules.task.entity.SubtaskEntity;
import com.livo.api.modules.task.entity.TaskEntity;
import com.livo.api.modules.task.entity.TaskTagEntity;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.repository.SubtaskRepository;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.task.repository.TaskTagRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TaskTagRepository taskTagRepository;

    @Autowired
    private com.livo.api.modules.task.service.TaskService taskService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part3_fb_" + UUID.randomUUID())
                .email("test.part3." + UUID.randomUUID() + "@example.com")
                .fullName("Part 3 Task User")
                .timezone("Asia/Kolkata")
                .language("en")
                .currency("INR")
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.findById(testUser.getId()).ifPresent(u -> {
                taskTagRepository.deleteAll(taskTagRepository.findAllByTagIdAndUserIdAndDeletedAtIsNull(UUID.randomUUID(), u.getId()));
                subtaskRepository.deleteAll(subtaskRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID.randomUUID(), u.getId()));
                taskRepository.deleteAll(taskRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                tagRepository.deleteAll(tagRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: TaskEntity persistence, repeating arrays, status and due-date queries")
    void testTaskPersistenceAndQueries() {
        LocalDate today = LocalDate.now();
        TaskEntity task = TaskEntity.builder()
                .title("Complete High Priority Architectural Review")
                .description("Detailed inspection of database schema & JPA entities")
                .projectLabel("LIVO Core")
                .dueDate(today)
                .dueTime(LocalTime.of(18, 0))
                .durationMins(45)
                .priority(TaskPriority.HIGH)
                .category("WORK")
                .status(TaskStatus.TODO)
                .completedDates(List.of(today.minusDays(1)))
                .skippedDates(List.of(today.minusDays(2)))
                .build();
        task.setUserId(testUser.getId());
        task = taskRepository.saveAndFlush(task);

        // Verify ID + User lookup
        Optional<TaskEntity> fetched = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getTitle()).isEqualTo("Complete High Priority Architectural Review");
        assertThat(fetched.get().getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(fetched.get().getCompletedDates()).contains(today.minusDays(1));
        assertThat(fetched.get().getSkippedDates()).contains(today.minusDays(2));

        // Query by status
        List<TaskEntity> todoTasks = taskRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(testUser.getId(), TaskStatus.TODO);
        assertThat(todoTasks).hasSize(1);

        // Query by due date
        List<TaskEntity> dueToday = taskRepository.findAllByUserIdAndDueDateAndDeletedAtIsNull(testUser.getId(), today);
        assertThat(dueToday).hasSize(1);

        // Soft delete
        task.markDeleted();
        taskRepository.saveAndFlush(task);
        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: Subtasks breakdown and ordered retrieval")
    void testSubtasks() {
        TaskEntity task = TaskEntity.builder()
                .title("Parent Task with Breakdown")
                .build();
        task.setUserId(testUser.getId());
        task = taskRepository.saveAndFlush(task);

        SubtaskEntity step1 = SubtaskEntity.builder()
                .taskId(task.getId())
                .title("Step 1: Write Entities")
                .isCompleted(true)
                .sortOrder(1)
                .build();
        step1.setUserId(testUser.getId());

        SubtaskEntity step2 = SubtaskEntity.builder()
                .taskId(task.getId())
                .title("Step 2: Write Repositories")
                .isCompleted(false)
                .sortOrder(2)
                .build();
        step2.setUserId(testUser.getId());

        subtaskRepository.saveAndFlush(step1);
        subtaskRepository.saveAndFlush(step2);

        List<SubtaskEntity> subtasks = subtaskRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(task.getId(), testUser.getId());
        assertThat(subtasks).hasSize(2);
        assertThat(subtasks.get(0).getTitle()).isEqualTo("Step 1: Write Entities");
        assertThat(subtasks.get(0).isCompleted()).isTrue();
        assertThat(subtasks.get(1).getTitle()).isEqualTo("Step 2: Write Repositories");
        assertThat(subtasks.get(1).isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Test 3: TaskTag associations with tenant isolation")
    void testTaskTagAssociation() {
        TaskEntity task = TaskEntity.builder()
                .title("Tagged Task")
                .build();
        task.setUserId(testUser.getId());
        task = taskRepository.saveAndFlush(task);

        TagEntity tag = TagEntity.builder()
                .name("Backend")
                .colorHex("#3B82F6")
                .build();
        tag.setUserId(testUser.getId());
        tag = tagRepository.saveAndFlush(tag);

        TaskTagEntity taskTag = TaskTagEntity.builder()
                .taskId(task.getId())
                .tagId(tag.getId())
                .build();
        taskTag.setUserId(testUser.getId());
        taskTagRepository.saveAndFlush(taskTag);

        List<TaskTagEntity> tagsForTask = taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId());
        assertThat(tagsForTask).hasSize(1);
        assertThat(tagsForTask.get(0).getTagId()).isEqualTo(tag.getId());

        assertThat(taskTagRepository.existsByTaskIdAndTagIdAndDeletedAtIsNull(task.getId(), tag.getId())).isTrue();
    }

    @Test
    @DisplayName("Test 4: deleteTask cascades soft-delete to task_tags")
    void testDeleteTaskCascadesToTaskTags() {
        TaskEntity task = TaskEntity.builder()
                .title("Task with Tags to Delete")
                .build();
        task.setUserId(testUser.getId());
        task = taskRepository.saveAndFlush(task);

        TagEntity tag = TagEntity.builder()
                .name("CleanupTag")
                .colorHex("#EF4444")
                .build();
        tag.setUserId(testUser.getId());
        tag = tagRepository.saveAndFlush(tag);

        TaskTagEntity taskTag = TaskTagEntity.builder()
                .taskId(task.getId())
                .tagId(tag.getId())
                .build();
        taskTag.setUserId(testUser.getId());
        taskTagRepository.saveAndFlush(taskTag);

        assertThat(taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId())).hasSize(1);

        taskService.deleteTask(testUser.getId(), task.getId());

        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId())).isEmpty();
        assertThat(taskTagRepository.findAllByTaskIdAndUserIdAndDeletedAtIsNull(task.getId(), testUser.getId())).isEmpty();
    }
}
