package com.livo.api.modules.user.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.user.dto.RegisterDeviceTokenRequest;
import com.livo.api.modules.user.dto.ReorderLifeAreasRequest;
import com.livo.api.modules.user.dto.UpdateLifeAreaRequest;
import com.livo.api.modules.user.dto.UpdateOnboardingRequest;
import com.livo.api.modules.user.dto.UpdateUserProfileRequest;
import com.livo.api.modules.user.dto.UpdateUserPreferenceRequest;
import com.livo.api.modules.user.dto.UserLifeAreaResponse;
import com.livo.api.modules.user.dto.UserPreferenceResponse;
import com.livo.api.modules.user.dto.UserProfileResponse;
import com.livo.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.livo.api.modules.insights.dto.UserStatsResponse;
import com.livo.api.modules.insights.service.InsightsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/users", "/api/v1/user"})
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile, preferences, life areas, stats, and FCM device tokens")
public class UserController {

    private final UserService userService;
    private final InsightsService insightsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@CurrentUser UUID userId) {
        UserStatsResponse response = insightsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "User productivity stats retrieved successfully"));
    }

    @GetMapping({"/me", "/profile"})
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@CurrentUser UUID userId) {
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "User profile retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @CurrentUser UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User profile updated successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser UUID userId) {
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User account deleted successfully"));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferences(@CurrentUser UUID userId) {
        UserPreferenceResponse response = userService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "User preferences retrieved successfully"));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreferences(
            @CurrentUser UUID userId,
            @Valid @RequestBody UpdateUserPreferenceRequest request
    ) {
        UserPreferenceResponse response = userService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User preferences updated successfully"));
    }

    @GetMapping("/life-areas")
    public ResponseEntity<ApiResponse<List<UserLifeAreaResponse>>> getLifeAreas(
            @CurrentUser UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<UserLifeAreaResponse> response = userService.getLifeAreas(userId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(response, "User life areas retrieved successfully"));
    }

    @PatchMapping("/life-areas/{id}")
    public ResponseEntity<ApiResponse<UserLifeAreaResponse>> updateLifeAreaStatus(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLifeAreaRequest request
    ) {
        UserLifeAreaResponse response = userService.updateLifeAreaStatus(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Life area status updated successfully"));
    }

    @PutMapping("/life-areas/reorder")
    public ResponseEntity<ApiResponse<List<UserLifeAreaResponse>>> reorderLifeAreas(
            @CurrentUser UUID userId,
            @Valid @RequestBody ReorderLifeAreasRequest request
    ) {
        List<UserLifeAreaResponse> response = userService.reorderLifeAreas(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Life areas reordered successfully"));
    }

    @PatchMapping("/onboarding")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateOnboarding(
            @CurrentUser UUID userId,
            @Valid @RequestBody UpdateOnboardingRequest request
    ) {
        UserProfileResponse response = userService.updateOnboarding(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Onboarding progress updated successfully"));
    }

    @PostMapping("/device-tokens")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @CurrentUser UUID userId,
            @Valid @RequestBody RegisterDeviceTokenRequest request
    ) {
        userService.registerDeviceToken(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Device token registered successfully"));
    }

    @DeleteMapping("/device-tokens/{token}")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @CurrentUser UUID userId,
            @PathVariable String token
    ) {
        userService.unregisterDeviceToken(userId, token);
        return ResponseEntity.ok(ApiResponse.success(null, "Device token unregistered successfully"));
    }
}
