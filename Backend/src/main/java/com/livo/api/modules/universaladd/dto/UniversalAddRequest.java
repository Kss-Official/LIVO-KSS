package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to parse natural language or voice transcript into structured LIVO data")
public class UniversalAddRequest {

    @NotBlank(message = "Input text cannot be blank")
    @Schema(description = "Free-form natural language text or voice transcript", example = "Buy groceries tomorrow at 5pm #errands")
    private String input;

    @Schema(description = "Optional preferred domain hint if user selected a specific tab")
    private UniversalAddDomain preferredDomain;

    @Builder.Default
    @Schema(description = "Whether to use Google Gemini AI parsing instead of local deterministic regex matcher")
    private Boolean useAi = false;
}
