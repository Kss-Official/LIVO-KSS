package com.livo.api.modules.auth.service;

import com.livo.api.common.exception.ForbiddenException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.common.exception.UnauthorizedException;
import com.livo.api.common.security.JwtTokenProvider;
import com.livo.api.modules.auth.dto.AuthResponse;
import com.livo.api.modules.auth.dto.LogoutRequest;
import com.livo.api.modules.auth.dto.RefreshTokenRequest;
import com.livo.api.modules.auth.dto.UserSummaryResponse;
import com.livo.api.modules.auth.dto.UserSyncRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserLifeAreaRepository userLifeAreaRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.livo.api.common.security.UserSecurityStatusCache userSecurityStatusCache;

    @Value("${livo.jwt.expiration-ms:86400000}")
    private long expirationMs;

    @Override
    @Transactional
    public AuthResponse syncUser(UserSyncRequest request) {
        String firebaseUid = request.getFirebaseUid().trim();
        String email = request.getEmail().trim().toLowerCase();

        Optional<UserEntity> existingUserOpt = userRepository.findByFirebaseUidAndDeletedAtIsNull(firebaseUid);
        UserEntity user;
        boolean isNewUser = false;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            if (!user.isActive() || user.isDeleted()) {
                throw new ForbiddenException("User account has been deactivated or disabled");
            }

            boolean updated = false;
            if (request.getFullName() != null && !request.getFullName().isBlank() && !request.getFullName().equals(user.getFullName())) {
                user.setFullName(request.getFullName().trim());
                updated = true;
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(user.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl());
                updated = true;
            }
            if (!email.equalsIgnoreCase(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
        } else {
            // Check if user already exists by email
            Optional<UserEntity> emailUserOpt = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email);
            if (emailUserOpt.isPresent()) {
                user = emailUserOpt.get();
                if (!user.isActive() || user.isDeleted()) {
                    throw new ForbiddenException("User account has been deactivated or disabled");
                }
                user.setFirebaseUid(firebaseUid);
                if (request.getFullName() != null && !request.getFullName().isBlank()) {
                    user.setFullName(request.getFullName().trim());
                }
                if (request.getAvatarUrl() != null) {
                    user.setAvatarUrl(request.getAvatarUrl());
                }
                user = userRepository.save(user);
            } else {
                // Provision a brand new user
                isNewUser = true;
                UserEntity newUser = UserEntity.builder()
                        .firebaseUid(firebaseUid)
                        .email(email)
                        .fullName(request.getFullName().trim())
                        .avatarUrl(request.getAvatarUrl())
                        .timezone(request.getTimezone() != null && !request.getTimezone().isBlank() ? request.getTimezone().trim() : "Asia/Kolkata")
                        .language("en")
                        .currency("INR")
                        .dateFormat("DD/MM/YYYY")
                        .weekStart(WeekStart.MON)
                        .theme(Theme.LIGHT)
                        .isOnboarded(false)
                        .onboardingStepReached((short) 1)
                        .isActive(true)
                        .version(1L)
                        .build();
                user = userRepository.save(newUser);

                // 1. Provision default user preferences
                UserPreferenceEntity defaultPreferences = UserPreferenceEntity.builder()
                        .userId(user.getId())
                        .dailyWakeTime(LocalTime.of(7, 0))
                        .dailySleepTime(LocalTime.of(23, 0))
                        .maxPlannedHoursPerDay(new BigDecimal("8.00"))
                        .aiPreferenceLevel(AiPreferenceLevel.BALANCED)
                        .aiProactiveSuggestions(true)
                        .aiAllowFinance(false)
                        .aiAllowHealth(false)
                        .notificationsEnabled(true)
                        .notificationQuietStart(LocalTime.of(22, 0))
                        .notificationQuietEnd(LocalTime.of(7, 0))
                        .version(1L)
                        .build();
                userPreferenceRepository.save(defaultPreferences);

                // 2. Provision default 9 user life areas
                LifeAreaName[] defaultAreas = {
                        LifeAreaName.TASKS,
                        LifeAreaName.CALENDAR,
                        LifeAreaName.GOALS,
                        LifeAreaName.HABITS,
                        LifeAreaName.FINANCE,
                        LifeAreaName.LEARNING,
                        LifeAreaName.TRAVEL,
                        LifeAreaName.HEALTH,
                        LifeAreaName.INSIGHTS
                };
                List<UserLifeAreaEntity> lifeAreas = new ArrayList<>();
                short order = 1;
                for (LifeAreaName area : defaultAreas) {
                    UserLifeAreaEntity lifeArea = UserLifeAreaEntity.builder()
                            .areaName(area)
                            .isActive(true)
                            .displayOrder(order++)
                            .build();
                    lifeArea.setUserId(user.getId());
                    lifeArea.setVersion(1L);
                    lifeAreas.add(lifeArea);
                }
                userLifeAreaRepository.saveAll(lifeAreas);
                log.info("Provisioned new user {} with default preferences and 9 life areas", user.getId());
            }
        }

        // Register / Update FCM Device Token if provided
        if (request.getFcmToken() != null && !request.getFcmToken().isBlank()) {
            registerDeviceToken(user.getId(), request.getFcmToken().trim(), request.getDevicePlatform());
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(expirationMs)
                .isNewUser(isNewUser)
                .user(UserSummaryResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid token type: Expected a refresh token but received an access token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found or account deactivated"));

        if (!user.isActive() || user.isDeleted()) {
            throw new ForbiddenException("User account has been deactivated or disabled");
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getFirebaseUid());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(expirationMs)
                .isNewUser(false)
                .user(UserSummaryResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserSummaryResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void logout(UUID userId, LogoutRequest request) {
        if (request != null && request.getDeviceToken() != null && !request.getDeviceToken().isBlank()) {
            userDeviceTokenRepository.deactivateToken(request.getDeviceToken().trim());
        }
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.revokeTokens();
                userRepository.save(user);
                userSecurityStatusCache.evict(userId);
            });
        }
        SecurityContextHolder.clearContext();
    }

    private void registerDeviceToken(UUID userId, String token, String platformString) {
        DevicePlatform platform = DevicePlatform.ANDROID;
        if (platformString != null && !platformString.isBlank()) {
            try {
                platform = DevicePlatform.valueOf(platformString.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                log.warn("Unknown device platform: {}, defaulting to ANDROID", platformString);
            }
        }
        final DevicePlatform finalPlatform = platform;

        userDeviceTokenRepository.findByDeviceToken(token)
                .ifPresentOrElse(
                        existing -> {
                            if (existing.getUserId() != null && !existing.getUserId().equals(userId)) {
                                log.warn("Device token ownership transfer during auth sync: reassigning token [{}] from previous user {} to new user {}",
                                        maskToken(token), existing.getUserId(), userId);
                                existing.setUserId(userId);
                            } else {
                                log.info("Refreshed device token [{}] for user {} during auth sync", maskToken(token), userId);
                            }
                            existing.setPlatform(finalPlatform);
                            existing.setActive(true);
                            userDeviceTokenRepository.save(existing);
                        },
                        () -> {
                            UserDeviceTokenEntity deviceTokenEntity = UserDeviceTokenEntity.builder()
                                    .userId(userId)
                                    .deviceToken(token)
                                    .platform(finalPlatform)
                                    .isActive(true)
                                    .build();
                            userDeviceTokenRepository.save(deviceTokenEntity);
                            log.info("Registered new device token [{}] for user {} on platform {} during auth sync", maskToken(token), userId, finalPlatform);
                        }
                );
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
