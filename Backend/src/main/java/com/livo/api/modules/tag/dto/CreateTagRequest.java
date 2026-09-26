package com.livo.api.modules.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;

    @Builder.Default
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color hex must be a valid 6-digit hex color (e.g. #6B7280)")
    private String colorHex = "#6B7280";
}
