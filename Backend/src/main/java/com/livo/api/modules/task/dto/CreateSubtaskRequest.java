package com.livo.api.modules.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubtaskRequest {

    @NotBlank(message = "Subtask title is required")
    @Size(min = 1, max = 150, message = "Subtask title must be between 1 and 150 characters")
    private String title;

    @Builder.Default
    private int sortOrder = 1;
}
