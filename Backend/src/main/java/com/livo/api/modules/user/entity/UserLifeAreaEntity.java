package com.livo.api.modules.user.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.user.entity.enums.LifeAreaName;
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

/**
 * Enabled life domains / modules for a specific user (Tasks, Habits, Finance, etc.).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_life_areas")
public class UserLifeAreaEntity extends BaseSyncEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "area_name", nullable = false, length = 20)
    private LifeAreaName areaName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private short displayOrder = 1;
}
