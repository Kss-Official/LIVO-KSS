package com.livo.api.modules.user.dto;

import com.livo.api.modules.user.entity.UserLifeAreaEntity;
import com.livo.api.modules.user.entity.enums.LifeAreaName;
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
public class UserLifeAreaResponse {

    private UUID id;
    private UUID userId;
    private LifeAreaName areaName;
    private boolean isActive;
    private short displayOrder;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserLifeAreaResponse fromEntity(UserLifeAreaEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserLifeAreaResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .areaName(entity.getAreaName())
                .isActive(entity.isActive())
                .displayOrder(entity.getDisplayOrder())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
