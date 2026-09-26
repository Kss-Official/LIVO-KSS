package com.livo.api.modules.tag.dto;

import com.livo.api.modules.tag.entity.TagEntity;
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
public class TagResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String colorHex;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static TagResponse fromEntity(TagEntity entity) {
        if (entity == null) {
            return null;
        }
        return TagResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .colorHex(entity.getColorHex())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
