package com.livo.api.modules.user.entity;

import com.livo.api.common.entity.BaseEntity;
import com.livo.api.modules.user.entity.enums.Theme;
import com.livo.api.modules.user.entity.enums.WeekStart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Core User entity authenticated via Firebase Auth.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "bio", length = 300)
    private String bio;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    @Builder.Default
    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Builder.Default
    @Column(name = "date_format", nullable = false, length = 20)
    private String dateFormat = "DD/MM/YYYY";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "week_start", nullable = false, length = 3)
    private WeekStart weekStart = WeekStart.MON;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    private Theme theme = Theme.LIGHT;

    @Builder.Default
    @Column(name = "is_onboarded", nullable = false)
    private boolean isOnboarded = false;

    @Builder.Default
    @Column(name = "onboarding_step_reached", nullable = false)
    private short onboardingStepReached = 1;

    @Column(name = "terms_accepted_version", length = 20)
    private String termsAcceptedVersion;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 1L;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "tokens_revoked_before")
    private Instant tokensRevokedBefore;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
        this.isActive = false;
        this.tokensRevokedBefore = Instant.now();
    }

    public void revokeTokens() {
        this.tokensRevokedBefore = Instant.now();
    }
}
