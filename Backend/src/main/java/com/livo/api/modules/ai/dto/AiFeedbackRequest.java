package com.livo.api.modules.ai.dto;

import com.livo.api.modules.ai.entity.enums.AiFeedbackType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackRequest {

    @NotNull(message = "Feedback type is required (ACCEPTED, DISMISSED, SNOOZED, MODIFIED, REJECTED)")
    private AiFeedbackType feedbackType;

    private String userNote;
}
