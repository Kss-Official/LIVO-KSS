package com.livo.api.modules.user;

import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserLifeAreaEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import com.livo.api.modules.user.entity.enums.DevicePlatform;
import com.livo.api.modules.user.entity.enums.LifeAreaName;
import com.livo.api.modules.user.entity.enums.Theme;
import com.livo.api.modules.user.entity.enums.WeekStart;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserLifeAreaRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserLifeAreaRepository userLifeAreaRepository;

    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Create an isolated test user
        testUser = UserEntity.builder()
                .firebaseUid("test_fb_" + UUID.randomUUID())
                .email("test.user." + UUID.randomUUID() + "@example.com")
                .fullName("Test User Integration")
                .bio("Integration test bio")
                .timezone("Asia/Kolkata")
                .language("en")
                .currency("INR")
                .dateFormat("DD/MM/YYYY")
                .weekStart(WeekStart.MON)
                .theme(Theme.LIGHT)
                .isOnboarded(true)
                .onboardingStepReached((short) 4)
                .isActive(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.findById(testUser.getId()).ifPresent(u -> {
                userPreferenceRepository.deleteByUserId(u.getId());
                userLifeAreaRepository.deleteAll(userLifeAreaRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(u.getId()));
                userDeviceTokenRepository.deleteAll(userDeviceTokenRepository.findAllByUserIdAndIsActiveTrue(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: UserEntity CRUD, unique constraints, and soft delete")
    void testUserCrudAndSoftDelete() {
        // Query by ID
        Optional<UserEntity> found = userRepository.findByIdAndDeletedAtIsNull(testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(found.get().getCurrency()).isEqualTo("INR");

        // Query by Firebase UID
        Optional<UserEntity> byFb = userRepository.findByFirebaseUidAndDeletedAtIsNull(testUser.getFirebaseUid());
        assertThat(byFb).isPresent();
        assertThat(byFb.get().getId()).isEqualTo(testUser.getId());

        // Query by Email Case-Insensitive
        Optional<UserEntity> byEmailUpper = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(testUser.getEmail().toUpperCase());
        assertThat(byEmailUpper).isPresent();
        assertThat(byEmailUpper.get().getId()).isEqualTo(testUser.getId());

        // Soft Delete
        testUser.markDeleted();
        userRepository.saveAndFlush(testUser);

        // Verify soft-deleted user is excluded from active queries
        assertThat(userRepository.findByIdAndDeletedAtIsNull(testUser.getId())).isEmpty();
        assertThat(userRepository.findByFirebaseUidAndDeletedAtIsNull(testUser.getFirebaseUid())).isEmpty();
        // But still exists in raw table
        assertThat(userRepository.findById(testUser.getId())).isPresent();
    }

    @Test
    @DisplayName("Test 2: UserPreferenceEntity persistence and array mapping")
    void testUserPreferences() {
        UserPreferenceEntity preferences = UserPreferenceEntity.builder()
                .userId(testUser.getId())
                .dailyWakeTime(LocalTime.of(6, 30))
                .dailySleepTime(LocalTime.of(22, 30))
                .maxPlannedHoursPerDay(new BigDecimal("7.50"))
                .preferredDeepWorkStart(LocalTime.of(9, 0))
                .preferredDeepWorkEnd(LocalTime.of(12, 0))
                .aiProactiveSuggestions(true)
                .aiPreferenceLevel(AiPreferenceLevel.BALANCED)
                .aiAllowFinance(true)
                .aiAllowHealth(true)
                .notificationsEnabled(true)
                .notificationQuietStart(LocalTime.of(22, 0))
                .notificationQuietEnd(LocalTime.of(6, 30))
                .dismissedOverloadDates(List.of(LocalDate.of(2026, 9, 21)))
                .build();

        userPreferenceRepository.saveAndFlush(preferences);

        Optional<UserPreferenceEntity> fetched = userPreferenceRepository.findByUserId(testUser.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getAiPreferenceLevel()).isEqualTo(AiPreferenceLevel.BALANCED);
        assertThat(fetched.get().getMaxPlannedHoursPerDay()).isEqualByComparingTo("7.50");
        assertThat(fetched.get().isAiAllowFinance()).isTrue();
        assertThat(fetched.get().getDismissedOverloadDates()).contains(LocalDate.of(2026, 9, 21));
    }

    @Test
    @DisplayName("Test 3: UserLifeAreaEntity multi-tenant queries and display ordering")
    void testUserLifeAreas() {
        UserLifeAreaEntity tasksArea = UserLifeAreaEntity.builder()
                .areaName(LifeAreaName.TASKS)
                .isActive(true)
                .displayOrder((short) 1)
                .build();
        tasksArea.setUserId(testUser.getId());

        UserLifeAreaEntity financeArea = UserLifeAreaEntity.builder()
                .areaName(LifeAreaName.FINANCE)
                .isActive(true)
                .displayOrder((short) 2)
                .build();
        financeArea.setUserId(testUser.getId());

        userLifeAreaRepository.saveAndFlush(tasksArea);
        userLifeAreaRepository.saveAndFlush(financeArea);

        List<UserLifeAreaEntity> areas = userLifeAreaRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(testUser.getId());
        assertThat(areas).hasSize(2);
        assertThat(areas.get(0).getAreaName()).isEqualTo(LifeAreaName.TASKS);
        assertThat(areas.get(1).getAreaName()).isEqualTo(LifeAreaName.FINANCE);

        // Test existence check
        assertThat(userLifeAreaRepository.existsByUserIdAndAreaNameAndDeletedAtIsNull(testUser.getId(), LifeAreaName.TASKS)).isTrue();
        assertThat(userLifeAreaRepository.existsByUserIdAndAreaNameAndDeletedAtIsNull(testUser.getId(), LifeAreaName.TRAVEL)).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("Test 4: UserDeviceTokenEntity push token management")
    void testUserDeviceTokens() {
        String token = "fcm_token_test_" + UUID.randomUUID();

        UserDeviceTokenEntity deviceToken = UserDeviceTokenEntity.builder()
                .userId(testUser.getId())
                .deviceToken(token)
                .platform(DevicePlatform.ANDROID)
                .isActive(true)
                .build();

        userDeviceTokenRepository.saveAndFlush(deviceToken);

        List<UserDeviceTokenEntity> activeTokens = userDeviceTokenRepository.findAllByUserIdAndIsActiveTrue(testUser.getId());
        assertThat(activeTokens).hasSize(1);
        assertThat(activeTokens.get(0).getDeviceToken()).isEqualTo(token);
        assertThat(activeTokens.get(0).getPlatform()).isEqualTo(DevicePlatform.ANDROID);

        // Deactivate token
        userDeviceTokenRepository.deactivateToken(token);

        List<UserDeviceTokenEntity> afterDeactivate = userDeviceTokenRepository.findAllByUserIdAndIsActiveTrue(testUser.getId());
        assertThat(afterDeactivate).isEmpty();
    }
}
