package com.livo.api.modules.ai.entity;

import com.livo.api.common.entity.BaseEntity;
import com.livo.api.modules.ai.entity.enums.AiFeedbackType;
import com.livo.api.modules.ai.entity.enums.AiPermissionLevel;
import com.livo.api.modules.ai.entity.enums.AiRecommendationPriority;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import com.livo.api.modules.ai.entity.enums.AiRelatedEntityType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing proactive AI suggestions, optimization alerts, and behavioral insights.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_recommendations")
public class AiRecommendationEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private AiRecommendationType type;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_priority", nullable = false, length = 10)
    private AiRecommendationPriority recommendationPriority = AiRecommendationPriority.MEDIUM;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false, length = 10)
    private AiPermissionLevel permissionLevel = AiPermissionLevel.SUGGEST;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type", length = 20)
    private AiRelatedEntityType relatedEntityType;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "action_type", length = 50)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_payload", columnDefinition = "jsonb")
    private Map<String, Object> actionPayload;

    @Column(name = "dedupe_key", length = 100)
    private String dedupeKey;

    @Column(name = "snooze_until")
    private Instant snoozeUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", length = 20)
    private AiFeedbackType feedbackType;

    @Column(name = "user_note", length = 500)
    private String userNote;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
