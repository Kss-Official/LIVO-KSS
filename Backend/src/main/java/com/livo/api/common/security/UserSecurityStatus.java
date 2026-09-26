package com.livo.api.common.security;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Value object representing cached security authorization status for a user.
 */
@Getter
@Builder
public class UserSecurityStatus {
    private final UUID userId;
    private final boolean exists;
    private final boolean active;
    private final boolean deleted;
    private final Instant tokensRevokedBefore;

    /**
     * Evaluates if the presented token is permitted to access the application.
     *
     * @param tokenIssuedAt the timestamp when the token was created
     * @return true if the user exists, is active, not deleted, and token was not issued before revocation
     */
    public boolean isAllowed(Instant tokenIssuedAt) {
        if (!exists || !active || deleted) {
            return false;
        }
        if (tokensRevokedBefore != null) {
            if (tokenIssuedAt == null) {
                return false;
            }
            return !tokenIssuedAt.isBefore(tokensRevokedBefore);
        }
        return true;
    }
}
