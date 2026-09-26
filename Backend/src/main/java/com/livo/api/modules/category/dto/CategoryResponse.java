package com.livo.api.modules.category.dto;

import com.livo.api.modules.category.entity.CategoryEntity;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
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
public class CategoryResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String iconKey;
    private String colorHex;
    private CategoryDomainType domainType;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static CategoryResponse fromEntity(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .iconKey(entity.getIconKey())
                .colorHex(entity.getColorHex())
                .domainType(entity.getDomainType())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
