package com.livo.api.modules.auth.controller;

import com.livo.api.common.exception.UnauthorizedException;
import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.auth.dto.AuthResponse;
import com.livo.api.modules.auth.dto.LogoutRequest;
import com.livo.api.modules.auth.dto.RefreshTokenRequest;
import com.livo.api.modules.auth.dto.UserSummaryResponse;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication, user sync, and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<AuthResponse>> syncUser(@Valid @RequestBody UserSyncRequest request) {
        AuthResponse response = authService.syncUser(request);
        String message = response.isNewUser() ? "User provisioned and synced successfully" : "User synced successfully";
        return ResponseEntity.status(response.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ApiResponse.success(response, message));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody UserSyncRequest request) {
        AuthResponse response = authService.syncUser(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserSyncRequest request) {
        AuthResponse response = authService.syncUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getMe(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication token is required to view profile");
        }
        UserSummaryResponse response = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Current user retrieved successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CurrentUser UserPrincipal principal,
            @RequestBody(required = false) LogoutRequest request
    ) {
        UUID userId = principal != null ? principal.getId() : null;
        authService.logout(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
