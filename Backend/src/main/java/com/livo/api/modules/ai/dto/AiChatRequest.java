package com.livo.api.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    private UUID conversationId;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    @Builder.Default
    private String operatingMode = "GENERAL";

    private List<UUID> attachmentIds;
}
