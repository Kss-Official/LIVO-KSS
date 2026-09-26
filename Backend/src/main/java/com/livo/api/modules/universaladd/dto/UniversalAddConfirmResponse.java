package com.livo.api.modules.universaladd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response confirming creation of entity across the respective module")
public class UniversalAddConfirmResponse {

    @Schema(description = "ID of the newly created entity")
    private UUID entityId;

    @Schema(description = "Domain where entity was created")
    private UniversalAddDomain domain;

    @Schema(description = "Title of the created entity")
    private String title;

    @Schema(description = "User-facing confirmation message")
    private String message;

    @Schema(description = "Full entity response details")
    private Object entityDetails;
}
