package com.livo.api.modules.learning.dto;

import com.livo.api.modules.learning.entity.enums.LearningResourceType;
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
public class UpdateLearningResourceRequest {

    private LearningResourceType resourceType;

    @Size(max = 150, message = "Resource title cannot exceed 150 characters")
    private String title;

    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "URL must start with http:// or https://")
    private String url;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private Boolean isCompleted;

    private Integer sortOrder;
}
