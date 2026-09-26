package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing the parsed draft ready for 1-tap confirmation")
public class UniversalAddResponse {

    @Schema(description = "Original input string")
    private String rawInput;

    @Schema(description = "Detected domain type")
    private UniversalAddDomain detectedDomain;

    @Schema(description = "Confidence score between 0.0 and 1.0")
    private double confidence;

    @Schema(description = "Extracted entity draft")
    private ParsedEntityDraft parsedDraft;

    @Schema(description = "Explanation of how the text was parsed")
    private String explanation;
}
