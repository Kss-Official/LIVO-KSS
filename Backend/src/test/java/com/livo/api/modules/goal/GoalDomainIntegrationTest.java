package com.livo.api.modules.goal;

import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.GoalProgressHistoryEntity;
import com.livo.api.modules.goal.entity.MilestoneEntity;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import com.livo.api.modules.goal.entity.enums.GoalTrackingType;
import com.livo.api.modules.goal.repository.GoalProgressHistoryRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.goal.repository.MilestoneRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GoalDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private GoalProgressHistoryRepository goalProgressHistoryRepository;

    @Autowired
    private com.livo.api.modules.goal.service.GoalService goalService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part5_fb_" + UUID.randomUUID())
                .email("test.part5." + UUID.randomUUID() + "@example.com")
                .fullName("Part 5 Goal User")
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
                goalProgressHistoryRepository.deleteAll(goalProgressHistoryRepository.findAllByUserIdAndRecordedDateOrderByCreatedAtDesc(u.getId(), LocalDate.now()));
                milestoneRepository.deleteAll(milestoneRepository.findAllByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID.randomUUID()));
                goalRepository.deleteAll(goalRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: GoalEntity persistence, numerical tracking, and soft-delete")
    void testGoalPersistence() {
        GoalEntity goal = GoalEntity.builder()
                .title("Master Spring Boot & System Architecture")
                .description("Build the production-grade LIVO backend")
                .relatedArea("LEARNING")
                .targetDescription("Complete 100 hours of architecture and backend coding")
                .category("LEARNING")
                .priority(GoalPriority.HIGH)
                .targetDate(LocalDate.now().plusMonths(3))
                .progressTrackingType(GoalTrackingType.NUMERICAL)
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("25.00"))
                .unit("hours")
                .reminderFrequency("WEEKLY")
                .status(GoalStatus.IN_PROGRESS)
                .build();
        goal.setUserId(testUser.getId());
        goal = goalRepository.saveAndFlush(goal);

        Optional<GoalEntity> found = goalRepository.findByIdAndUserIdAndDeletedAtIsNull(goal.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Master Spring Boot & System Architecture");
        assertThat(found.get().getTargetValue()).isEqualByComparingTo("100.00");
        assertThat(found.get().getCurrentValue()).isEqualByComparingTo("25.00");
        assertThat(found.get().getUnit()).isEqualTo("hours");

        // Query by status
        List<GoalEntity> activeGoals = goalRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(testUser.getId(), GoalStatus.IN_PROGRESS);
        assertThat(activeGoals).hasSize(1);

        // Soft delete
        goal.markDeleted();
        goalRepository.saveAndFlush(goal);
        assertThat(goalRepository.findByIdAndUserIdAndDeletedAtIsNull(goal.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: MilestoneEntity checkpoints with ordered retrieval")
    void testMilestones() {
        GoalEntity goal = GoalEntity.builder()
                .title("Fitness Target 2026")
                .build();
        goal.setUserId(testUser.getId());
        goal = goalRepository.saveAndFlush(goal);

        MilestoneEntity m1 = MilestoneEntity.builder()
                .goalId(goal.getId())
                .title("Run 5k without stopping")
                .sortOrder(1)
                .isCompleted(true)
                .build();
        m1.setUserId(testUser.getId());

        MilestoneEntity m2 = MilestoneEntity.builder()
                .goalId(goal.getId())
                .title("Run 10k in under 55 mins")
                .sortOrder(2)
                .isCompleted(false)
                .build();
        m2.setUserId(testUser.getId());

        milestoneRepository.saveAndFlush(m1);
        milestoneRepository.saveAndFlush(m2);

        List<MilestoneEntity> milestones = milestoneRepository.findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(goal.getId(), testUser.getId());
        assertThat(milestones).hasSize(2);
        assertThat(milestones.get(0).getTitle()).isEqualTo("Run 5k without stopping");
        assertThat(milestones.get(0).isCompleted()).isTrue();
        assertThat(milestones.get(1).getTitle()).isEqualTo("Run 10k in under 55 mins");
        assertThat(milestones.get(1).isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Test 3: GoalProgressHistoryEntity tracking value transitions over time")
    void testProgressHistory() {
        GoalEntity goal = GoalEntity.builder()
                .title("Savings Goal")
                .build();
        goal.setUserId(testUser.getId());
        goal = goalRepository.saveAndFlush(goal);

        LocalDate today = LocalDate.now();

        GoalProgressHistoryEntity progress = GoalProgressHistoryEntity.builder()
                .goalId(goal.getId())
                .userId(testUser.getId())
                .recordedDate(today)
                .previousValue(BigDecimal.ZERO)
                .newValue(new BigDecimal("15000.00"))
                .notes("Added monthly salary savings")
                .build();

        goalProgressHistoryRepository.saveAndFlush(progress);

        List<GoalProgressHistoryEntity> history = goalProgressHistoryRepository.findAllByGoalIdAndUserIdOrderByRecordedDateDescCreatedAtDesc(
                goal.getId(), testUser.getId()
        );
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getNewValue()).isEqualByComparingTo("15000.00");
        assertThat(history.get(0).getNotes()).isEqualTo("Added monthly salary savings");
        assertThat(history.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Test 4: Goal validation rejects negative currentValue and non-positive targetValue")
    void testGoalBoundsValidation() {
        // 1. Negative target value rejected on create
        com.livo.api.modules.goal.dto.CreateGoalRequest reqBadTarget = com.livo.api.modules.goal.dto.CreateGoalRequest.builder()
                .title("Invalid Target Goal")
                .targetValue(new BigDecimal("-100.00"))
                .build();
        org.junit.jupiter.api.Assertions.assertThrows(com.livo.api.common.exception.BadRequestException.class, () ->
                goalService.createGoal(testUser.getId(), reqBadTarget)
        );

        // 2. Negative current value rejected on create
        com.livo.api.modules.goal.dto.CreateGoalRequest reqBadCurrent = com.livo.api.modules.goal.dto.CreateGoalRequest.builder()
                .title("Invalid Current Goal")
                .currentValue(new BigDecimal("-10.00"))
                .build();
        org.junit.jupiter.api.Assertions.assertThrows(com.livo.api.common.exception.BadRequestException.class, () ->
                goalService.createGoal(testUser.getId(), reqBadCurrent)
        );

        // 3. Create valid goal
        com.livo.api.modules.goal.dto.CreateGoalRequest validGoal = com.livo.api.modules.goal.dto.CreateGoalRequest.builder()
                .title("Valid Progress Goal")
                .targetValue(new BigDecimal("100.00"))
                .build();
        com.livo.api.modules.goal.dto.GoalResponse created = goalService.createGoal(testUser.getId(), validGoal);

        // 4. Negative update progress rejected
        com.livo.api.modules.goal.dto.UpdateGoalProgressRequest badProgress = com.livo.api.modules.goal.dto.UpdateGoalProgressRequest.builder()
                .currentValue(new BigDecimal("-5.00"))
                .build();
        org.junit.jupiter.api.Assertions.assertThrows(com.livo.api.common.exception.BadRequestException.class, () ->
                goalService.updateGoalProgress(testUser.getId(), created.getId(), badProgress)
        );

        // 5. Negative target update rejected
        com.livo.api.modules.goal.dto.UpdateGoalRequest badTargetUpdate = com.livo.api.modules.goal.dto.UpdateGoalRequest.builder()
                .targetValue(BigDecimal.ZERO)
                .build();
        org.junit.jupiter.api.Assertions.assertThrows(com.livo.api.common.exception.BadRequestException.class, () ->
                goalService.updateGoal(testUser.getId(), created.getId(), badTargetUpdate)
        );
    }
}
