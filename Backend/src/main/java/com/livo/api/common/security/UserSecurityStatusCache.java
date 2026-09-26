package com.livo.api.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Ultra-fast local L1 in-memory cache for user authorization and revocation checks.
 * Prevents hammering remote PostgreSQL pool on every authenticated HTTP request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSecurityStatusCache {

    private final UserRepository userRepository;

    private final Cache<UUID, UserSecurityStatus> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofSeconds(60))
            .build();

    public UserSecurityStatus getSecurityStatus(UUID userId) {
        return cache.get(userId, id -> {
            Optional<UserEntity> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return UserSecurityStatus.builder()
                        .userId(id)
                        .exists(false)
                        .active(false)
                        .deleted(true)
                        .tokensRevokedBefore(null)
                        .build();
            }
            UserEntity user = userOpt.get();
            return UserSecurityStatus.builder()
                    .userId(id)
                    .exists(true)
                    .active(user.isActive())
                    .deleted(user.isDeleted())
                    .tokensRevokedBefore(user.getTokensRevokedBefore())
                    .build();
        });
    }

    public void evict(UUID userId) {
        if (userId != null) {
            cache.invalidate(userId);
        }
    }

    public void clear() {
        cache.invalidateAll();
    }
}
