package com.livo.api.modules.user.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.user.dto.RegisterDeviceTokenRequest;
import com.livo.api.modules.user.dto.ReorderLifeAreasRequest;
import com.livo.api.modules.user.dto.UpdateLifeAreaRequest;
import com.livo.api.modules.user.dto.UpdateOnboardingRequest;
import com.livo.api.modules.user.dto.UpdateUserProfileRequest;
import com.livo.api.modules.user.dto.UpdateUserPreferenceRequest;
import com.livo.api.modules.user.dto.UserLifeAreaResponse;
import com.livo.api.modules.user.dto.UserPreferenceResponse;
import com.livo.api.modules.user.dto.UserProfileResponse;
import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.UserLifeAreaEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.AiPreferenceLevel;
import com.livo.api.modules.user.entity.enums.DevicePlatform;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserLifeAreaRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserLifeAreaRepository userLifeAreaRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final com.livo.api.common.security.UserSecurityStatusCache userSecurityStatusCache;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserEntity user = findActiveUserOrThrow(userId);
        return UserProfileResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        UserEntity user = findActiveUserOrThrow(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            user.setTimezone(request.getTimezone().trim());
        }
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            user.setLanguage(request.getLanguage().trim());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            user.setCurrency(request.getCurrency().trim().toUpperCase());
        }
        if (request.getDateFormat() != null && !request.getDateFormat().isBlank()) {
            user.setDateFormat(request.getDateFormat().trim());
        }
        if (request.getWeekStart() != null) {
            user.setWeekStart(request.getWeekStart());
        }
        if (request.getTheme() != null) {
            user.setTheme(request.getTheme());
        }

        return UserProfileResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserPreferenceResponse getPreferences(UUID userId) {
        findActiveUserOrThrow(userId);

        UserPreferenceEntity preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return UserPreferenceResponse.fromEntity(preferences);
    }

    @Override
    @Transactional
    public UserPreferenceResponse updatePreferences(UUID userId, UpdateUserPreferenceRequest request) {
        findActiveUserOrThrow(userId);

        UserPreferenceEntity pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (request.getDailyWakeTime() != null) {
            pref.setDailyWakeTime(request.getDailyWakeTime());
        }
        if (request.getDailySleepTime() != null) {
            pref.setDailySleepTime(request.getDailySleepTime());
        }
        if (request.getMaxPlannedHoursPerDay() != null) {
            pref.setMaxPlannedHoursPerDay(request.getMaxPlannedHoursPerDay());
        }
        if (request.getPreferredDeepWorkStart() != null) {
            pref.setPreferredDeepWorkStart(request.getPreferredDeepWorkStart());
        }
        if (request.getPreferredDeepWorkEnd() != null) {
            pref.setPreferredDeepWorkEnd(request.getPreferredDeepWorkEnd());
        }
        if (request.getAiProactiveSuggestions() != null) {
            pref.setAiProactiveSuggestions(request.getAiProactiveSuggestions());
        }
        if (request.getAiPreferenceLevel() != null) {
            pref.setAiPreferenceLevel(request.getAiPreferenceLevel());
        }
        if (request.getAiAllowFinance() != null) {
            pref.setAiAllowFinance(request.getAiAllowFinance());
        }
        if (request.getAiAllowHealth() != null) {
            pref.setAiAllowHealth(request.getAiAllowHealth());
        }
        if (request.getNotificationsEnabled() != null) {
            pref.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getNotificationQuietStart() != null) {
            pref.setNotificationQuietStart(request.getNotificationQuietStart());
        }
        if (request.getNotificationQuietEnd() != null) {
            pref.setNotificationQuietEnd(request.getNotificationQuietEnd());
        }
        if (request.getDismissedOverloadDates() != null) {
            pref.setDismissedOverloadDates(request.getDismissedOverloadDates());
        }

        return UserPreferenceResponse.fromEntity(userPreferenceRepository.save(pref));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLifeAreaResponse> getLifeAreas(UUID userId, boolean activeOnly) {
        findActiveUserOrThrow(userId);

        List<UserLifeAreaEntity> areas = activeOnly
                ? userLifeAreaRepository.findAllByUserIdAndIsActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAsc(userId)
                : userLifeAreaRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId);

        return areas.stream()
                .map(UserLifeAreaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserLifeAreaResponse updateLifeAreaStatus(UUID userId, UUID areaId, UpdateLifeAreaRequest request) {
        findActiveUserOrThrow(userId);

        UserLifeAreaEntity area = userLifeAreaRepository.findByIdAndUserIdAndDeletedAtIsNull(areaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserLifeArea", "id", areaId));

        area.setActive(request.getIsActive());
        return UserLifeAreaResponse.fromEntity(userLifeAreaRepository.save(area));
    }

    @Override
    @Transactional
    public List<UserLifeAreaResponse> reorderLifeAreas(UUID userId, ReorderLifeAreasRequest request) {
        findActiveUserOrThrow(userId);

        List<UUID> orderedIds = request.getOrderedAreaIds();
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BadRequestException("Ordered area IDs list cannot be empty");
        }

        List<UserLifeAreaEntity> existingAreas = userLifeAreaRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId);

        if (orderedIds.size() != existingAreas.size()) {
            throw new BadRequestException(String.format(
                    "Reorder list must contain all %d life areas for user, but received %d",
                    existingAreas.size(), orderedIds.size()
            ));
        }

        Set<UUID> uniqueOrderedIds = new HashSet<>(orderedIds);
        if (uniqueOrderedIds.size() != orderedIds.size()) {
            throw new BadRequestException("Reorder list contains duplicate life area IDs");
        }

        Set<UUID> existingIds = existingAreas.stream().map(UserLifeAreaEntity::getId).collect(Collectors.toSet());
        if (!uniqueOrderedIds.equals(existingIds)) {
            throw new BadRequestException("Reorder list must contain a complete permutation of all existing active life areas");
        }

        Map<UUID, UserLifeAreaEntity> areaMap = existingAreas.stream()
                .collect(Collectors.toMap(UserLifeAreaEntity::getId, Function.identity()));

        short currentOrder = 1;
        for (UUID areaId : orderedIds) {
            UserLifeAreaEntity area = areaMap.get(areaId);
            area.setDisplayOrder(currentOrder++);
        }

        List<UserLifeAreaEntity> updatedAreas = userLifeAreaRepository.saveAll(existingAreas);
        return updatedAreas.stream()
                .sorted(Comparator.comparingInt(UserLifeAreaEntity::getDisplayOrder))
                .map(UserLifeAreaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserProfileResponse updateOnboarding(UUID userId, UpdateOnboardingRequest request) {
        UserEntity user = findActiveUserOrThrow(userId);

        if (request.getOnboardingStepReached() != null) {
            user.setOnboardingStepReached(request.getOnboardingStepReached());
        }
        if (request.getIsOnboarded() != null) {
            user.setOnboarded(request.getIsOnboarded());
        }
        if (request.getTermsAcceptedVersion() != null) {
            user.setTermsAcceptedVersion(request.getTermsAcceptedVersion());
        }
        if (Boolean.TRUE.equals(request.getAcceptTerms())) {
            user.setTermsAcceptedAt(Instant.now());
        }

        return UserProfileResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void registerDeviceToken(UUID userId, RegisterDeviceTokenRequest request) {
        findActiveUserOrThrow(userId);
        String token = request.getDeviceToken().trim();
        DevicePlatform platform = request.getPlatform() != null ? request.getPlatform() : DevicePlatform.ANDROID;

        userDeviceTokenRepository.findByDeviceToken(token)
                .ifPresentOrElse(
                        existing -> {
                            if (existing.getUserId() != null && !existing.getUserId().equals(userId)) {
                                log.warn("Device token ownership transfer: reassigning token [{}] from previous user {} to new user {}",
                                        maskToken(token), existing.getUserId(), userId);
                                existing.setUserId(userId);
                            } else {
                                log.info("Refreshed device token [{}] for user {}", maskToken(token), userId);
                            }
                            existing.setPlatform(platform);
                            existing.setActive(true);
                            userDeviceTokenRepository.save(existing);
                        },
                        () -> {
                            UserDeviceTokenEntity deviceToken = UserDeviceTokenEntity.builder()
                                    .userId(userId)
                                    .deviceToken(token)
                                    .platform(platform)
                                    .isActive(true)
                                    .build();
                            userDeviceTokenRepository.save(deviceToken);
                            log.info("Registered new device token [{}] for user {} on platform {}", maskToken(token), userId, platform);
                        }
                );
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    @Override
    @Transactional
    public void unregisterDeviceToken(UUID userId, String deviceToken) {
        findActiveUserOrThrow(userId);
        userDeviceTokenRepository.deleteAllByUserIdAndDeviceToken(userId, deviceToken.trim());
    }

    @Override
    @Transactional
    public void deleteAccount(UUID userId) {
        UserEntity user = findActiveUserOrThrow(userId);
        user.markDeleted();
        userRepository.save(user);
        userSecurityStatusCache.evict(userId);
        log.info("User account {} soft deleted", userId);
    }

    private UserEntity findActiveUserOrThrow(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserPreferenceEntity createDefaultPreferences(UUID userId) {
        UserPreferenceEntity defaultPreferences = UserPreferenceEntity.builder()
                .userId(userId)
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
        return userPreferenceRepository.save(defaultPreferences);
    }
}
