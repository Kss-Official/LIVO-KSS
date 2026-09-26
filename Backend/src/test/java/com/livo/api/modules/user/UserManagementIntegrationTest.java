package com.livo.api.modules.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.common.security.JwtTokenProvider;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.user.dto.RegisterDeviceTokenRequest;
import com.livo.api.modules.user.dto.ReorderLifeAreasRequest;
import com.livo.api.modules.user.dto.UpdateLifeAreaRequest;
import com.livo.api.modules.user.dto.UpdateOnboardingRequest;
import com.livo.api.modules.user.dto.UpdateUserProfileRequest;
import com.livo.api.modules.user.dto.UpdateUserPreferenceRequest;
import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import com.livo.api.modules.user.entity.enums.DevicePlatform;
import com.livo.api.modules.user.entity.enums.Theme;
import com.livo.api.modules.user.entity.enums.WeekStart;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserLifeAreaRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserLifeAreaRepository userLifeAreaRepository;

    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "user_mgmt_" + UUID.randomUUID();
        String email = "usermgmt_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Management User")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should retrieve authenticated user profile")
    void testGetProfileReturnsCurrentUserData() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.fullName").value("Management User"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Kolkata"));
    }

    @Test
    @DisplayName("Should update user profile fields and persist changes")
    void testUpdateProfileModifiesAllowedFields() throws Exception {
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .fullName("Updated Manager")
                .bio("Life OS Power User")
                .timezone("America/New_York")
                .currency("USD")
                .theme(Theme.DARK)
                .weekStart(WeekStart.SUN)
                .build();

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Manager"))
                .andExpect(jsonPath("$.data.bio").value("Life OS Power User"))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.theme").value("DARK"))
                .andExpect(jsonPath("$.data.weekStart").value("SUN"));

        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow();
        assertThat(user.getFullName()).isEqualTo("Updated Manager");
        assertThat(user.getBio()).isEqualTo("Life OS Power User");
        assertThat(user.getCurrency()).isEqualTo("USD");
        assertThat(user.getTheme()).isEqualTo(Theme.DARK);
    }

    @Test
    @DisplayName("Should get and update user preferences")
    void testGetAndModifyPreferences() throws Exception {
        mockMvc.perform(get("/api/v1/users/preferences")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyWakeTime").value("07:00:00"));

        UpdateUserPreferenceRequest updateRequest = UpdateUserPreferenceRequest.builder()
                .dailyWakeTime(LocalTime.of(6, 30))
                .dailySleepTime(LocalTime.of(22, 30))
                .maxPlannedHoursPerDay(new BigDecimal("7.50"))
                .aiPreferenceLevel(AiPreferenceLevel.PROACTIVE)
                .aiAllowFinance(true)
                .aiAllowHealth(true)
                .build();

        mockMvc.perform(put("/api/v1/users/preferences")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyWakeTime").value("06:30:00"))
                .andExpect(jsonPath("$.data.dailySleepTime").value("22:30:00"))
                .andExpect(jsonPath("$.data.aiPreferenceLevel").value("PROACTIVE"))
                .andExpect(jsonPath("$.data.aiAllowFinance").value(true));

        UserPreferenceEntity entity = userPreferenceRepository.findByUserId(userId).orElseThrow();
        assertThat(entity.getDailyWakeTime()).isEqualTo(LocalTime.of(6, 30));
        assertThat(entity.getDailySleepTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(entity.getMaxPlannedHoursPerDay()).isEqualByComparingTo(new BigDecimal("7.50"));
        assertThat(entity.getAiPreferenceLevel()).isEqualTo(AiPreferenceLevel.PROACTIVE);
    }

    @Test
    @DisplayName("Should reject invalid max planned hours with 400 Bad Request")
    void testUpdatePreferencesValidationRejectsInvalidHours() throws Exception {
        UpdateUserPreferenceRequest updateRequest = UpdateUserPreferenceRequest.builder()
                .maxPlannedHoursPerDay(new BigDecimal("28.00")) // Exceeds 24
                .build();

        mockMvc.perform(put("/api/v1/users/preferences")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should retrieve life areas and filter by active status")
    void testGetLifeAreasAndFilterActiveOnly() throws Exception {
        MvcResult allResult = mockMvc.perform(get("/api/v1/users/life-areas")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(9))
                .andReturn();

        JsonNode allJson = objectMapper.readTree(allResult.getResponse().getContentAsString());
        UUID firstAreaId = UUID.fromString(allJson.path("data").get(0).path("id").asText());

        // Deactivate first area
        UpdateLifeAreaRequest deactivateReq = UpdateLifeAreaRequest.builder()
                .isActive(false)
                .build();

        mockMvc.perform(patch("/api/v1/users/life-areas/" + firstAreaId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        // Filter activeOnly
        mockMvc.perform(get("/api/v1/users/life-areas?activeOnly=true")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(8));
    }

    @Test
    @DisplayName("Should reorder life areas according to client sequence")
    void testReorderLifeAreasUpdatesDisplayOrder() throws Exception {
        MvcResult allResult = mockMvc.perform(get("/api/v1/users/life-areas")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode allJson = objectMapper.readTree(allResult.getResponse().getContentAsString());
        List<UUID> areaIds = new ArrayList<>();
        allJson.path("data").forEach(node -> areaIds.add(UUID.fromString(node.path("id").asText())));

        // Reverse order
        Collections.reverse(areaIds);

        ReorderLifeAreasRequest reorderRequest = ReorderLifeAreasRequest.builder()
                .orderedAreaIds(areaIds)
                .build();

        mockMvc.perform(put("/api/v1/users/life-areas/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(areaIds.get(0).toString()))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1));
    }

    @Test
    @DisplayName("Should reject partial or duplicate list in reorderLifeAreas with 400")
    void testReorderLifeAreasRejectsPartialOrDuplicateList() throws Exception {
        MvcResult allResult = mockMvc.perform(get("/api/v1/users/life-areas")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode allJson = objectMapper.readTree(allResult.getResponse().getContentAsString());
        List<UUID> areaIds = new ArrayList<>();
        allJson.path("data").forEach(node -> areaIds.add(UUID.fromString(node.path("id").asText())));
        assertThat(areaIds.size()).isGreaterThan(1);

        // 1. Partial list (only first 2 items)
        List<UUID> partialList = areaIds.subList(0, 2);
        mockMvc.perform(put("/api/v1/users/life-areas/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderLifeAreasRequest.builder()
                                .orderedAreaIds(partialList)
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Reorder list must contain all")));

        // 2. Duplicate ID list
        List<UUID> duplicateList = new ArrayList<>(areaIds);
        duplicateList.set(1, duplicateList.get(0));
        mockMvc.perform(put("/api/v1/users/life-areas/reorder")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReorderLifeAreasRequest.builder()
                                .orderedAreaIds(duplicateList)
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("duplicate")));
    }

    @Test
    @DisplayName("Should update onboarding progress and terms acceptance")
    void testUpdateOnboardingProgress() throws Exception {
        UpdateOnboardingRequest request = UpdateOnboardingRequest.builder()
                .onboardingStepReached((short) 4)
                .isOnboarded(true)
                .acceptTerms(true)
                .termsAcceptedVersion("1.0.0")
                .build();

        mockMvc.perform(patch("/api/v1/users/onboarding")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStepReached").value(4))
                .andExpect(jsonPath("$.data.onboarded").value(true))
                .andExpect(jsonPath("$.data.termsAcceptedVersion").value("1.0.0"))
                .andExpect(jsonPath("$.data.termsAcceptedAt").isNotEmpty());

        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow();
        assertThat(user.isOnboarded()).isTrue();
        assertThat(user.getTermsAcceptedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should register and delete FCM device token")
    void testDeviceTokenRegistrationAndRemoval() throws Exception {
        String token = "fcm_user_mgmt_token_" + UUID.randomUUID();

        RegisterDeviceTokenRequest regRequest = RegisterDeviceTokenRequest.builder()
                .deviceToken(token)
                .platform(DevicePlatform.IOS)
                .build();

        mockMvc.perform(post("/api/v1/users/device-tokens")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        UserDeviceTokenEntity deviceToken = userDeviceTokenRepository.findByDeviceToken(token).orElseThrow();
        assertThat(deviceToken.getUserId()).isEqualTo(userId);
        assertThat(deviceToken.getPlatform()).isEqualTo(DevicePlatform.IOS);

        mockMvc.perform(delete("/api/v1/users/device-tokens/" + token)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(userDeviceTokenRepository.findByDeviceToken(token)).isEmpty();
    }

    @Test
    @DisplayName("Should transfer device token ownership and audit when a new user registers an existing token")
    void testDeviceTokenOwnershipTransfer() throws Exception {
        String token = "fcm_transfer_token_" + UUID.randomUUID();

        // 1. User 1 registers device token
        RegisterDeviceTokenRequest regRequest1 = RegisterDeviceTokenRequest.builder()
                .deviceToken(token)
                .platform(DevicePlatform.ANDROID)
                .build();

        mockMvc.perform(post("/api/v1/users/device-tokens")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        UserDeviceTokenEntity initialToken = userDeviceTokenRepository.findByDeviceToken(token).orElseThrow();
        assertThat(initialToken.getUserId()).isEqualTo(userId);

        // 2. User 2 registers the exact same device token (e.g. shared device / re-login)
        var user2Auth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid("fcm_user2_" + UUID.randomUUID())
                .email("user2_" + UUID.randomUUID() + "@livo.test")
                .fullName("User Two")
                .build());
        UUID user2Id = user2Auth.getUser().getId();
        String user2Token = user2Auth.getAccessToken();

        RegisterDeviceTokenRequest regRequest2 = RegisterDeviceTokenRequest.builder()
                .deviceToken(token)
                .platform(DevicePlatform.IOS)
                .build();

        mockMvc.perform(post("/api/v1/users/device-tokens")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Verify device token ownership was transferred to User 2
        UserDeviceTokenEntity transferredToken = userDeviceTokenRepository.findByDeviceToken(token).orElseThrow();
        assertThat(transferredToken.getUserId()).isEqualTo(user2Id);
        assertThat(transferredToken.getPlatform()).isEqualTo(DevicePlatform.IOS);

        // 4. User 1 attempting to unregister this token should NOT delete it (as it now belongs to User 2)
        mockMvc.perform(delete("/api/v1/users/device-tokens/" + token)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        assertThat(userDeviceTokenRepository.findByDeviceToken(token)).isPresent();
        assertThat(userDeviceTokenRepository.findByDeviceToken(token).get().getUserId()).isEqualTo(user2Id);

        // 5. User 2 unregistering the token succeeds and removes it
        mockMvc.perform(delete("/api/v1/users/device-tokens/" + token)
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        assertThat(userDeviceTokenRepository.findByDeviceToken(token)).isEmpty();
    }

    @Test
    @DisplayName("Should soft delete user account")
    void testSoftDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Subsequent call to /me should return 401 Unauthorized since deleted user's token is rejected by security cache
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        UserEntity deletedUser = userRepository.findById(userId).orElseThrow();
        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.isActive()).isFalse();
    }
}
