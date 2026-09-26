package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to confirm and persist a parsed draft into the respective domain module")
public class UniversalAddConfirmRequest {

    @Valid
    @NotNull(message = "Draft cannot be null")
    @Schema(description = "The parsed and optionally user-adjusted draft")
    private ParsedEntityDraft draft;
}
