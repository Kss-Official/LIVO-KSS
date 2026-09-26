package com.livo.api.modules.learning;

import com.livo.api.modules.learning.entity.LearningItemEntity;
import com.livo.api.modules.learning.entity.LearningResourceEntity;
import com.livo.api.modules.learning.entity.LearningSessionEntity;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningResourceType;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.entity.enums.StudyFrequency;
import com.livo.api.modules.learning.repository.LearningItemRepository;
import com.livo.api.modules.learning.repository.LearningResourceRepository;
import com.livo.api.modules.learning.repository.LearningSessionRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LearningDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private LearningSessionRepository learningSessionRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part8_fb_" + UUID.randomUUID())
                .email("test.part8." + UUID.randomUUID() + "@example.com")
                .fullName("Part 8 Learning User")
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
                learningSessionRepository.deleteAll(learningSessionRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                learningResourceRepository.deleteAll(learningResourceRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                learningItemRepository.deleteAll(learningItemRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: LearningItemEntity persistence, enums mapping, progress percentage, and status filtering")
    void testLearningItemPersistence() {
        LocalDate today = LocalDate.now();
        LearningItemEntity item = LearningItemEntity.builder()
                .title("Deep Learning Specialization")
                .description("Master Neural Networks and Deep Learning")
                .objective("Build deep learning architectures in PyTorch")
                .notes("Focus on convolution and attention mechanisms")
                .learningType(LearningType.COURSE)
                .category("AI/ML")
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .targetStudyTimeMinutes(60)
                .studyFrequency(StudyFrequency.DAILY)
                .progressPercentage((short) 40)
                .status(LearningStatus.IN_PROGRESS)
                .startDate(today)
                .targetCompletionDate(today.plusMonths(3))
                .build();
        item.setUserId(testUser.getId());
        item = learningItemRepository.saveAndFlush(item);

        Optional<LearningItemEntity> found = learningItemRepository.findByIdAndUserIdAndDeletedAtIsNull(item.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Deep Learning Specialization");
        assertThat(found.get().getLearningType()).isEqualTo(LearningType.COURSE);
        assertThat(found.get().getDifficultyLevel()).isEqualTo(DifficultyLevel.INTERMEDIATE);
        assertThat(found.get().getTargetStudyTimeMinutes()).isEqualTo(60);
        assertThat(found.get().getStudyFrequency()).isEqualTo(StudyFrequency.DAILY);
        assertThat(found.get().getProgressPercentage()).isEqualTo((short) 40);
        assertThat(found.get().getStatus()).isEqualTo(LearningStatus.IN_PROGRESS);

        // Status query
        List<LearningItemEntity> inProgress = learningItemRepository.findAllByUserIdAndStatusAndDeletedAtIsNull(
                testUser.getId(), LearningStatus.IN_PROGRESS
        );
        assertThat(inProgress).hasSize(1);

        // Category query
        List<LearningItemEntity> aiItems = learningItemRepository.findAllByUserIdAndCategoryAndDeletedAtIsNull(testUser.getId(), "AI/ML");
        assertThat(aiItems).hasSize(1);

        // Soft delete
        item.markDeleted();
        learningItemRepository.saveAndFlush(item);
        assertThat(learningItemRepository.findByIdAndUserIdAndDeletedAtIsNull(item.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: LearningResourceEntity persistence, resource type, sort order, and soft delete")
    void testLearningResourcePersistence() {
        LearningItemEntity item = LearningItemEntity.builder()
                .title("System Design Roadmap")
                .startDate(LocalDate.now())
                .build();
        item.setUserId(testUser.getId());
        item = learningItemRepository.saveAndFlush(item);

        LearningResourceEntity res1 = LearningResourceEntity.builder()
                .learningItemId(item.getId())
                .resourceType(LearningResourceType.WEBSITE)
                .title("ByteByteGo System Design Guide")
                .url("https://bytebytego.com")
                .notes("Read chapter 1 on rate limiters")
                .isCompleted(false)
                .sortOrder(1)
                .build();
        res1.setUserId(testUser.getId());
        res1 = learningResourceRepository.saveAndFlush(res1);

        LearningResourceEntity res2 = LearningResourceEntity.builder()
                .learningItemId(item.getId())
                .resourceType(LearningResourceType.VIDEO)
                .title("MIT Distributed Systems Lecture 1")
                .url("https://youtube.com/watch?v=example")
                .notes("Introduction to RPCs")
                .isCompleted(true)
                .sortOrder(2)
                .build();
        res2.setUserId(testUser.getId());
        res2 = learningResourceRepository.saveAndFlush(res2);

        List<LearningResourceEntity> resources = learningResourceRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(
                item.getId(), testUser.getId()
        );
        assertThat(resources).hasSize(2);
        assertThat(resources.get(0).getTitle()).isEqualTo("ByteByteGo System Design Guide");
        assertThat(resources.get(0).getResourceType()).isEqualTo(LearningResourceType.WEBSITE);
        assertThat(resources.get(1).getTitle()).isEqualTo("MIT Distributed Systems Lecture 1");
        assertThat(resources.get(1).isCompleted()).isTrue();

        // Soft delete res1
        res1.markDeleted();
        learningResourceRepository.saveAndFlush(res1);
        List<LearningResourceEntity> remaining = learningResourceRepository.findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(
                item.getId(), testUser.getId()
        );
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTitle()).isEqualTo("MIT Distributed Systems Lecture 1");
    }

    @Test
    @DisplayName("Test 3: LearningSessionEntity persistence, duration check, and date range query")
    void testLearningSessionPersistence() {
        LocalDate today = LocalDate.now();
        LearningItemEntity item = LearningItemEntity.builder()
                .title("Rust Programming Language")
                .startDate(today.minusDays(10))
                .build();
        item.setUserId(testUser.getId());
        item = learningItemRepository.saveAndFlush(item);

        LearningSessionEntity session = LearningSessionEntity.builder()
                .learningItemId(item.getId())
                .sessionDate(today)
                .durationMinutes(45)
                .notes("Completed chapter on lifetimes and borrowing")
                .build();
        session.setUserId(testUser.getId());
        session = learningSessionRepository.saveAndFlush(session);

        Optional<LearningSessionEntity> found = learningSessionRepository.findByIdAndUserIdAndDeletedAtIsNull(session.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDurationMinutes()).isEqualTo(45);
        assertThat(found.get().getSessionDate()).isEqualTo(today);

        // Date range query
        List<LearningSessionEntity> sessions = learningSessionRepository.findAllByUserIdAndSessionDateBetweenAndDeletedAtIsNullOrderBySessionDateDesc(
                testUser.getId(), today.minusDays(1), today.plusDays(1)
        );
        assertThat(sessions).hasSize(1);

        // Soft delete
        session.markDeleted();
        learningSessionRepository.saveAndFlush(session);
        assertThat(learningSessionRepository.findByIdAndUserIdAndDeletedAtIsNull(session.getId(), testUser.getId())).isEmpty();
    }
}
