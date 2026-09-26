package com.livo.api.security;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.JwtTokenProvider;
import com.livo.api.common.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityAndAuthenticationIntegrationTest.SecurityTestEndpoints.class)
class SecurityAndAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.livo.api.modules.user.repository.UserRepository userRepository;

    @Autowired
    private com.livo.api.modules.auth.service.AuthService authService;

    @Autowired
    private com.livo.api.common.security.UserSecurityStatusCache userSecurityStatusCache;

    @RestController
    static class SecurityTestEndpoints {

        @GetMapping("/api/v1/auth/ping")
        public ApiResponse<String> publicPing() {
            return ApiResponse.success("PONG", "Public auth endpoint is reachable");
        }

        @GetMapping("/api/v1/protected/me")
        public ApiResponse<String> protectedEndpoint(@CurrentUser UserPrincipal principal, @CurrentUser UUID userId) {
            return ApiResponse.success("Hello " + principal.getEmail() + " (" + userId + ")", "Authenticated successfully");
        }
    }

    @Test
    @DisplayName("Test 1: Public endpoint /api/v1/auth/** permits unauthenticated requests")
    void testPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("PONG"));
    }

    @Test
    @DisplayName("Test 2: Protected endpoint returns 401 Unauthorized when Bearer token is missing")
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/protected/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Authentication token is missing, invalid, or expired"));
    }

    @Test
    @DisplayName("Test 3: Protected endpoint returns 401 Unauthorized when invalid token is provided")
    void testProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Test 4: Protected endpoint succeeds with valid Bearer token and resolves @CurrentUser")
    void testProtectedEndpointWithValidToken() throws Exception {
        com.livo.api.modules.user.entity.UserEntity user = userRepository.save(com.livo.api.modules.user.entity.UserEntity.builder()
                .email("sec.test." + UUID.randomUUID() + "@livo.app")
                .fullName("Security Tester")
                .firebaseUid("fb_" + UUID.randomUUID())
                .isActive(true)
                .build());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());

        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Hello " + user.getEmail() + " (" + user.getId() + ")"));
    }

    @Test
    @DisplayName("Test 5: Protected endpoint rejects Refresh Token when presented as Bearer access token")
    void testProtectedEndpointRejectsRefreshToken() throws Exception {
        UUID testUserId = UUID.randomUUID();
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUserId);

        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Authentication token is missing, invalid, or expired"));
    }

    @Test
    @DisplayName("Test 6: Protected endpoint rejects token of deactivated user")
    void testProtectedEndpointRejectsDeactivatedUser() throws Exception {
        com.livo.api.modules.user.entity.UserEntity user = userRepository.save(com.livo.api.modules.user.entity.UserEntity.builder()
                .email("deact.test." + UUID.randomUUID() + "@livo.app")
                .fullName("Deactivated Tester")
                .firebaseUid("fb_" + UUID.randomUUID())
                .isActive(true)
                .build());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());

        // Deactivate user and evict cache
        user.setActive(false);
        userRepository.save(user);
        userSecurityStatusCache.evict(user.getId());

        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Test 7: Protected endpoint rejects token of soft-deleted user")
    void testProtectedEndpointRejectsSoftDeletedUser() throws Exception {
        com.livo.api.modules.user.entity.UserEntity user = userRepository.save(com.livo.api.modules.user.entity.UserEntity.builder()
                .email("del.test." + UUID.randomUUID() + "@livo.app")
                .fullName("Deleted Tester")
                .firebaseUid("fb_" + UUID.randomUUID())
                .isActive(true)
                .build());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());

        // Soft-delete user and evict cache
        user.markDeleted();
        userRepository.save(user);
        userSecurityStatusCache.evict(user.getId());

        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Test 8: Logout revokes existing token, and newly issued token succeeds")
    void testProtectedEndpointRevokesTokenOnLogout() throws Exception {
        com.livo.api.modules.user.entity.UserEntity user = userRepository.save(com.livo.api.modules.user.entity.UserEntity.builder()
                .email("logout.test." + UUID.randomUUID() + "@livo.app")
                .fullName("Logout Tester")
                .firebaseUid("fb_" + UUID.randomUUID())
                .isActive(true)
                .build());

        String tokenBeforeLogout = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());

        // Initial request succeeds
        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBeforeLogout))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Call logout
        authService.logout(user.getId(), null);

        // Previous token must now be REJECTED
        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBeforeLogout))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // Wait a tiny moment to ensure new token has issuedAt after revocation
        Thread.sleep(10);

        // Newly issued token must SUCCEED
        String tokenAfterLogin = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());
        mockMvc.perform(get("/api/v1/protected/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAfterLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Test 9: CORS preflight allows trusted origin")
    void testCorsAllowsTrustedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/sync")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Test 10: CORS preflight rejects untrusted origin")
    void testCorsRejectsUntrustedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/sync")
                        .header("Origin", "https://untrusted-malicious-site.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
