package com.livo.api.modules.learning.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import com.livo.api.modules.learning.entity.enums.LearningResourceType;
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
 * Entity representing an external resource or reference link attached to a learning item.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_resources")
public class LearningResourceEntity extends BaseSyncEntity {

    @Column(name = "learning_item_id", nullable = false)
    private UUID learningItemId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private LearningResourceType resourceType = LearningResourceType.WEBSITE;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "notes", length = 500)
    private String notes;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 1;
}
