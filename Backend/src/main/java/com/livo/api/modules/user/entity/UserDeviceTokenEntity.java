package com.livo.api.modules.user.entity;

import com.livo.api.common.entity.BaseEntity;
import com.livo.api.modules.user.entity.enums.DevicePlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Mobile and web push notification device tokens for Firebase Cloud Messaging (FCM).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_device_tokens")
public class UserDeviceTokenEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_token", nullable = false, unique = true, length = 500)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private DevicePlatform platform;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
