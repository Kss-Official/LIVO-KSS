package com.livo.api.modules.goal.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderMilestonesRequest {

    @NotEmpty(message = "Milestone IDs cannot be empty")
    private List<UUID> milestoneIds;
}
