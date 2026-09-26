package com.livo.api.modules.ai.entity;

import com.livo.api.common.entity.BaseEntity;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an AI-generated actionable modification requiring user consent or review.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_proposals")
public class AiProposalEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposal_payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> proposalPayload = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "undo_payload", columnDefinition = "jsonb")
    private Map<String, Object> undoPayload;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiProposalStatus status = AiProposalStatus.PENDING;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "reverted_at")
    private Instant revertedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
