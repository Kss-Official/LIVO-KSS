package com.livo.api.modules.user.dto;

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
public class ReorderLifeAreasRequest {

    @NotEmpty(message = "Ordered area IDs list cannot be empty")
    private List<UUID> orderedAreaIds;
}
