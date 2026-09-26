package com.livo.api.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private UUID conversationId;
    private UUID messageId;
    private String content;
    private String operatingMode;
    private Map<String, Object> cards;
    private List<String> suggestedReplies;
    private UUID proposalId;
    private int dailyUsageRemaining;
    private Instant createdAt;
}
