package com.livo.api.modules.plan.dto;

import com.livo.api.engines.conflict.SlotSuggestionEngine.FreeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckResponse {

    private boolean hasConflict;
    private List<String> conflictDetails;
    private List<FreeSlot> suggestedSlots;
}
