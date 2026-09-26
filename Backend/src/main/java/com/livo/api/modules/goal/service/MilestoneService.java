package com.livo.api.modules.goal.service;

import com.livo.api.modules.goal.dto.CreateMilestoneRequest;
import com.livo.api.modules.goal.dto.MilestoneResponse;
import com.livo.api.modules.goal.dto.ReorderMilestonesRequest;
import com.livo.api.modules.goal.dto.UpdateMilestoneRequest;

import java.util.List;
import java.util.UUID;

public interface MilestoneService {

    MilestoneResponse addMilestone(UUID userId, UUID goalId, CreateMilestoneRequest request);

    List<MilestoneResponse> getMilestonesByGoal(UUID userId, UUID goalId);

    MilestoneResponse updateMilestone(UUID userId, UUID goalId, UUID milestoneId, UpdateMilestoneRequest request);

    MilestoneResponse toggleMilestoneCompletion(UUID userId, UUID goalId, UUID milestoneId);

    List<MilestoneResponse> reorderMilestones(UUID userId, UUID goalId, ReorderMilestonesRequest request);

    void deleteMilestone(UUID userId, UUID goalId, UUID milestoneId);
}
