package com.livo.api.modules.category.dto;

import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    private String name;

    @Builder.Default
    @Size(max = 50, message = "Icon key cannot exceed 50 characters")
    private String iconKey = "folder";

    @Builder.Default
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color hex must be a valid 6-digit hex color (e.g. #3B82F6)")
    private String colorHex = "#3B82F6";

    @NotNull(message = "Domain type is required (TASK, EVENT, EXPENSE)")
    private CategoryDomainType domainType;
}
