package com.livo.api.modules.category.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
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
 * Custom user-created categories for Tasks, Calendar Events, or Expenses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseSyncEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Builder.Default
    @Column(name = "icon_key", nullable = false, length = 50)
    private String iconKey = "folder";

    @Builder.Default
    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex = "#3B82F6";

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type", nullable = false, length = 20)
    private CategoryDomainType domainType;
}
