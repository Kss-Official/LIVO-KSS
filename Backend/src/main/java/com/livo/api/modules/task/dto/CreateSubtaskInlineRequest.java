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
public class CreateSubtaskInlineRequest {

    @NotBlank(message = "Subtask title cannot be blank")
    @Size(max = 150, message = "Subtask title cannot exceed 150 characters")
    private String title;

    @Builder.Default
    private int sortOrder = 1;
}
