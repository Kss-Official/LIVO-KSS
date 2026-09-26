package com.livo.api.modules.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncPushRequest {

    @NotEmpty(message = "At least one mutation is required for batch sync push")
    @Valid
    @Builder.Default
    private List<OfflineMutationItemRequest> mutations = new ArrayList<>();
}
