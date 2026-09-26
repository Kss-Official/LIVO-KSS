package com.livo.api.modules.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.LogoutRequest;
import com.livo.api.modules.auth.dto.RefreshTokenRequest;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserLifeAreaEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.LifeAreaName;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserLifeAreaRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserLifeAreaRepository userLifeAreaRepository;

    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    @Test
    @DisplayName("Should provision user, default preferences, and 9 life areas on first sync")
    void testNewUserSyncProvisionsUserPreferencesAndLifeAreas() throws Exception {
        String uid = "firebase_new_" + UUID.randomUUID();
        String email = "sync_new_" + UUID.randomUUID() + "@livo.test";

        UserSyncRequest request = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("New Livo User")
                .avatarUrl("https://cloudinary.com/test_avatar.png")
                .timezone("Asia/Kolkata")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newUser").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.data.user.fullName").value("New Livo User"))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID userId = UUID.fromString(responseJson.path("data").path("user").path("id").asText());

        // Verify user persistence
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow();
        assertThat(user.getFirebaseUid()).isEqualTo(uid);
        assertThat(user.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(user.isActive()).isTrue();

        // Verify default preferences provisioned
        UserPreferenceEntity preferences = userPreferenceRepository.findByUserId(userId).orElseThrow();
        assertThat(preferences.getDailyWakeTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(preferences.getDailySleepTime()).isEqualTo(LocalTime.of(23, 0));
        assertThat(preferences.getMaxPlannedHoursPerDay()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(preferences.isAiProactiveSuggestions()).isTrue();

        // Verify default 9 life areas provisioned in sequence
        List<UserLifeAreaEntity> lifeAreas = userLifeAreaRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId);
        assertThat(lifeAreas).hasSize(9);
        assertThat(lifeAreas.get(0).getAreaName()).isEqualTo(LifeAreaName.TASKS);
        assertThat(lifeAreas.get(1).getAreaName()).isEqualTo(LifeAreaName.CALENDAR);
        assertThat(lifeAreas.get(2).getAreaName()).isEqualTo(LifeAreaName.GOALS);
        assertThat(lifeAreas.get(3).getAreaName()).isEqualTo(LifeAreaName.HABITS);
        assertThat(lifeAreas.get(4).getAreaName()).isEqualTo(LifeAreaName.FINANCE);
        assertThat(lifeAreas.get(5).getAreaName()).isEqualTo(LifeAreaName.LEARNING);
        assertThat(lifeAreas.get(6).getAreaName()).isEqualTo(LifeAreaName.TRAVEL);
        assertThat(lifeAreas.get(7).getAreaName()).isEqualTo(LifeAreaName.HEALTH);
        assertThat(lifeAreas.get(8).getAreaName()).isEqualTo(LifeAreaName.INSIGHTS);
    }

    @Test
    @DisplayName("Should update profile fields and return 200 OK for existing user on sync")
    void testExistingUserSyncUpdatesProfileAndReturnsTokens() throws Exception {
        String uid = "firebase_existing_" + UUID.randomUUID();
        String email = "sync_existing_" + UUID.randomUUID() + "@livo.test";

        UserSyncRequest initialRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Initial Name")
                .avatarUrl("https://cloudinary.com/avatar1.png")
                .build();

        mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated());

        // Second sync with updated name and avatar
        UserSyncRequest updateRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Updated Name")
                .avatarUrl("https://cloudinary.com/avatar2.png")
                .build();

        mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newUser").value(false))
                .andExpect(jsonPath("$.data.user.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.user.avatarUrl").value("https://cloudinary.com/avatar2.png"));

        UserEntity user = userRepository.findByFirebaseUidAndDeletedAtIsNull(uid).orElseThrow();
        assertThat(user.getFullName()).isEqualTo("Updated Name");
        assertThat(user.getAvatarUrl()).isEqualTo("https://cloudinary.com/avatar2.png");
    }

    @Test
    @DisplayName("Should register and associate FCM device token during sync")
    void testSyncWithDeviceTokenRegistersFcmToken() throws Exception {
        String uid = "firebase_fcm_" + UUID.randomUUID();
        String email = "sync_fcm_" + UUID.randomUUID() + "@livo.test";
        String fcmToken = "fcm_token_" + UUID.randomUUID();

        UserSyncRequest request = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("FCM User")
                .fcmToken(fcmToken)
                .devicePlatform("ANDROID")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID userId = UUID.fromString(responseJson.path("data").path("user").path("id").asText());

        UserDeviceTokenEntity deviceTokenEntity = userDeviceTokenRepository.findByDeviceToken(fcmToken).orElseThrow();
        assertThat(deviceTokenEntity.getUserId()).isEqualTo(userId);
        assertThat(deviceTokenEntity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should rotate and return new tokens via refresh endpoint")
    void testRefreshTokenSuccess() throws Exception {
        String uid = "firebase_refresh_" + UUID.randomUUID();
        String email = "sync_refresh_" + UUID.randomUUID() + "@livo.test";

        UserSyncRequest syncRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Refresh Tester")
                .build();

        MvcResult syncResult = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode syncJson = objectMapper.readTree(syncResult.getResponse().getContentAsString());
        String refreshToken = syncJson.path("data").path("refreshToken").asText();

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.email").value(email.toLowerCase()));
    }

    @Test
    @DisplayName("Should reject invalid or malformed refresh token with 401")
    void testRefreshTokenWithInvalidTokenFails() throws Exception {
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("invalid.malformed.refresh-token")
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should reject access token presented to refresh endpoint with 401 Unauthorized")
    void testRefreshTokenWithAccessTokenFails() throws Exception {
        String uid = "firebase_access_ref_" + UUID.randomUUID();
        String email = "access_ref_" + UUID.randomUUID() + "@livo.test";

        UserSyncRequest syncRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Token Confusion Tester")
                .build();

        MvcResult syncResult = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode syncJson = objectMapper.readTree(syncResult.getResponse().getContentAsString());
        String accessToken = syncJson.path("data").path("accessToken").asText();

        // Attempting to refresh using an access token MUST fail!
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(accessToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Expected a refresh token")));
    }

    @Test
    @DisplayName("Should get current user profile with valid Bearer token")
    void testGetCurrentUserWithTokenReturnsProfile() throws Exception {
        String uid = "firebase_me_" + UUID.randomUUID();
        String email = "sync_me_" + UUID.randomUUID() + "@livo.test";

        UserSyncRequest syncRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Profile User")
                .build();

        MvcResult syncResult = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode syncJson = objectMapper.readTree(syncResult.getResponse().getContentAsString());
        String accessToken = syncJson.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.data.fullName").value("Profile User"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when calling /api/v1/auth/me without token")
    void testGetCurrentUserWithoutTokenFails() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should logout and deactivate user device token")
    void testLogoutDeactivatesDeviceToken() throws Exception {
        String uid = "firebase_logout_" + UUID.randomUUID();
        String email = "sync_logout_" + UUID.randomUUID() + "@livo.test";
        String fcmToken = "fcm_logout_" + UUID.randomUUID();

        UserSyncRequest syncRequest = UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Logout User")
                .fcmToken(fcmToken)
                .devicePlatform("ANDROID")
                .build();

        MvcResult syncResult = mockMvc.perform(post("/api/v1/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode syncJson = objectMapper.readTree(syncResult.getResponse().getContentAsString());
        String accessToken = syncJson.path("data").path("accessToken").asText();

        LogoutRequest logoutRequest = LogoutRequest.builder()
                .deviceToken(fcmToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        UserDeviceTokenEntity deviceToken = userDeviceTokenRepository.findByDeviceToken(fcmToken).orElseThrow();
        assertThat(deviceToken.isActive()).isFalse();
    }
}
