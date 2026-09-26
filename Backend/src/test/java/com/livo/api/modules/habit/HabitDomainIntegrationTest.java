package com.livo.api.modules.habit;

import com.livo.api.modules.habit.entity.HabitEntity;
import com.livo.api.modules.habit.entity.HabitLogEntity;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.entity.enums.HabitPreferredTime;
import com.livo.api.modules.habit.repository.HabitLogRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
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
class HabitDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part6_fb_" + UUID.randomUUID())
                .email("test.part6." + UUID.randomUUID() + "@example.com")
                .fullName("Part 6 Habit User")
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
                habitLogRepository.deleteAll(habitLogRepository.findAllByUserIdAndLogDateAndDeletedAtIsNull(u.getId(), LocalDate.now()));
                habitRepository.deleteAll(habitRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: HabitEntity persistence, custom days array, and query by active status")
    void testHabitPersistence() {
        LocalDate today = LocalDate.now();
        HabitEntity habit = HabitEntity.builder()
                .title("Drink 3L Water Daily")
                .description("Stay hydrated for peak cognitive performance")
                .motivationNote("A hydrated mind is a sharp mind")
                .iconKey("WATER")
                .colorHex("#3B82F6")
                .frequencyType(HabitFrequency.DAILY)
                .customDays(List.of(1, 2, 3, 4, 5, 6, 7))
                .targetCount(3)
                .targetUnit("liters")
                .preferredTime(HabitPreferredTime.MORNING)
                .preferredClockTime(LocalTime.of(8, 0))
                .reminderEnabled(true)
                .reminderTime(LocalTime.of(8, 0))
                .startDate(today)
                .isArchived(false)
                .build();
        habit.setUserId(testUser.getId());
        habit = habitRepository.saveAndFlush(habit);

        Optional<HabitEntity> found = habitRepository.findByIdAndUserIdAndDeletedAtIsNull(habit.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Drink 3L Water Daily");
        assertThat(found.get().getTargetCount()).isEqualTo(3);
        assertThat(found.get().getTargetUnit()).isEqualTo("liters");
        assertThat(found.get().getCustomDays()).containsExactly(1, 2, 3, 4, 5, 6, 7);

        // Query active habits
        List<HabitEntity> activeHabits = habitRepository.findAllByUserIdAndIsArchivedFalseAndDeletedAtIsNull(testUser.getId());
        assertThat(activeHabits).hasSize(1);

        // Soft delete
        habit.markDeleted();
        habitRepository.saveAndFlush(habit);
        assertThat(habitRepository.findByIdAndUserIdAndDeletedAtIsNull(habit.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: HabitLogEntity check-in persistence and date-range queries")
    void testHabitLogs() {
        LocalDate today = LocalDate.now();
        HabitEntity habit = HabitEntity.builder()
                .title("Daily Meditation")
                .startDate(today.minusDays(5))
                .build();
        habit.setUserId(testUser.getId());
        habit = habitRepository.saveAndFlush(habit);

        HabitLogEntity log = HabitLogEntity.builder()
                .habitId(habit.getId())
                .logDate(today)
                .countCompleted(1)
                .build();
        log.setUserId(testUser.getId());
        log = habitLogRepository.saveAndFlush(log);

        Optional<HabitLogEntity> found = habitLogRepository.findByHabitIdAndLogDateAndDeletedAtIsNull(habit.getId(), today);
        assertThat(found).isPresent();
        assertThat(found.get().getCountCompleted()).isEqualTo(1);
        assertThat(found.get().getLoggedAt()).isNotNull();

        // Existence check
        assertThat(habitLogRepository.existsByHabitIdAndLogDateAndDeletedAtIsNull(habit.getId(), today)).isTrue();
        assertThat(habitLogRepository.existsByHabitIdAndLogDateAndDeletedAtIsNull(habit.getId(), today.minusDays(1))).isFalse();

        // Date range query
        List<HabitLogEntity> rangeLogs = habitLogRepository.findAllByHabitIdAndLogDateBetweenAndDeletedAtIsNullOrderByLogDateAsc(
                habit.getId(), today.minusDays(7), today.plusDays(1)
        );
        assertThat(rangeLogs).hasSize(1);
    }
}
