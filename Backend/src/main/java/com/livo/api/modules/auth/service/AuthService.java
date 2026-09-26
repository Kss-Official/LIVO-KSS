package com.livo.api.modules.auth.service;

import com.livo.api.modules.auth.dto.AuthResponse;
import com.livo.api.modules.auth.dto.LogoutRequest;
import com.livo.api.modules.auth.dto.RefreshTokenRequest;
import com.livo.api.modules.auth.dto.UserSummaryResponse;
import com.livo.api.modules.auth.dto.UserSyncRequest;

import java.util.UUID;

public interface AuthService {

    /**
     * Synchronizes a user authenticated via Firebase.
     * If the user doesn't exist, provisions user, default preferences, and default life areas.
     * If the user already exists, updates basic profile fields.
     * Issues JWT access and refresh tokens.
     */
    AuthResponse syncUser(UserSyncRequest request);

    /**
     * Refreshes JWT access token using a valid refresh token.
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Retrieves the current authenticated user profile summary.
     */
    UserSummaryResponse getCurrentUser(UUID userId);

    /**
     * Logs out the user, optionally unregistering the provided FCM device token.
     */
    void logout(UUID userId, LogoutRequest request);
}
