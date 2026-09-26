package com.livo.api.modules.tag.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User-defined tags for grouping tasks and items with color coding.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
public class TagEntity extends BaseSyncEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Builder.Default
    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex = "#6B7280";
}
