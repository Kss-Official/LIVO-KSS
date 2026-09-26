package com.livo.api.modules.user.service;

import com.livo.api.modules.user.dto.RegisterDeviceTokenRequest;
import com.livo.api.modules.user.dto.ReorderLifeAreasRequest;
import com.livo.api.modules.user.dto.UpdateLifeAreaRequest;
import com.livo.api.modules.user.dto.UpdateOnboardingRequest;
import com.livo.api.modules.user.dto.UpdateUserProfileRequest;
import com.livo.api.modules.user.dto.UpdateUserPreferenceRequest;
import com.livo.api.modules.user.dto.UserLifeAreaResponse;
import com.livo.api.modules.user.dto.UserPreferenceResponse;
import com.livo.api.modules.user.dto.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request);

    UserPreferenceResponse getPreferences(UUID userId);

    UserPreferenceResponse updatePreferences(UUID userId, UpdateUserPreferenceRequest request);

    List<UserLifeAreaResponse> getLifeAreas(UUID userId, boolean activeOnly);

    UserLifeAreaResponse updateLifeAreaStatus(UUID userId, UUID areaId, UpdateLifeAreaRequest request);

    List<UserLifeAreaResponse> reorderLifeAreas(UUID userId, ReorderLifeAreasRequest request);

    UserProfileResponse updateOnboarding(UUID userId, UpdateOnboardingRequest request);

    void registerDeviceToken(UUID userId, RegisterDeviceTokenRequest request);

    void unregisterDeviceToken(UUID userId, String deviceToken);

    void deleteAccount(UUID userId);
}
