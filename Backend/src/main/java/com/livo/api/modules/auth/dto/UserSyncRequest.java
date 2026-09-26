package com.livo.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncRequest {

    @NotBlank(message = "Firebase UID is required")
    private String firebaseUid;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String avatarUrl;

    private String timezone;

    private String fcmToken;

    private String devicePlatform;
}
