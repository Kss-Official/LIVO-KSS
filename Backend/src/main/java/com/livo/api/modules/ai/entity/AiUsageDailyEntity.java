package com.livo.api.modules.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entity tracking daily AI chat and voice usage quotas for rate limiting.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_usage_daily")
public class AiUsageDailyEntity implements Serializable {

    @EmbeddedId
    private AiUsageDailyId id;

    @Builder.Default
    @Column(name = "chat_count", nullable = false)
    private int chatCount = 0;

    @Builder.Default
    @Column(name = "voice_count", nullable = false)
    private int voiceCount = 0;
}
