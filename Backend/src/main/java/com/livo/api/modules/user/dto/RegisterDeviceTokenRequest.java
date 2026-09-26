package com.livo.api.modules.user.dto;

import com.livo.api.modules.user.entity.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String deviceToken;

    @Builder.Default
    private DevicePlatform platform = DevicePlatform.ANDROID;
}
