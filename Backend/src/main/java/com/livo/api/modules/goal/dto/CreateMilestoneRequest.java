package com.livo.api.modules.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMilestoneRequest {

    @NotBlank(message = "Milestone title cannot be blank")
    @Size(max = 150, message = "Milestone title cannot exceed 150 characters")
    private String title;

    private LocalDate targetDate;

    private Integer sortOrder;

    @Builder.Default
    private boolean isAiGenerated = false;
}
