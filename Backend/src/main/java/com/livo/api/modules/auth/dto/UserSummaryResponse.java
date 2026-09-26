package com.livo.api.modules.auth.dto;

import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.entity.enums.Theme;
import com.livo.api.modules.user.entity.enums.WeekStart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private UUID id;
    private String firebaseUid;
    private String email;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private String timezone;
    private String language;
    private String currency;
    private String dateFormat;
    private WeekStart weekStart;
    private Theme theme;
    private boolean isOnboarded;
    private short onboardingStepReached;
    private Instant createdAt;

    public static UserSummaryResponse fromEntity(UserEntity user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .currency(user.getCurrency())
                .dateFormat(user.getDateFormat())
                .weekStart(user.getWeekStart())
                .theme(user.getTheme())
                .isOnboarded(user.isOnboarded())
                .onboardingStepReached(user.getOnboardingStepReached())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
