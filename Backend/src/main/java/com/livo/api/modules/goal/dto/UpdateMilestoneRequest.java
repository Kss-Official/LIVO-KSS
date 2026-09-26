package com.livo.api.modules.goal.dto;

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
public class UpdateMilestoneRequest {

    @Size(max = 150, message = "Milestone title cannot exceed 150 characters")
    private String title;

    private LocalDate targetDate;

    private Boolean isCompleted;

    private Integer sortOrder;
}
