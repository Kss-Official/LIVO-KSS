package com.livo.api.modules.goal.service;

import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.GoalProgressHistoryResponse;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.dto.UpdateGoalProgressRequest;
import com.livo.api.modules.goal.dto.UpdateGoalRequest;
import com.livo.api.modules.goal.entity.enums.GoalStatus;

import java.util.List;
import java.util.UUID;

public interface GoalService {

    GoalResponse createGoal(UUID userId, CreateGoalRequest request);

    GoalResponse getGoalById(UUID userId, UUID goalId);

    List<GoalResponse> getAllGoals(UUID userId, GoalStatus status, String relatedArea);

    GoalResponse updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request);

    GoalResponse updateGoalProgress(UUID userId, UUID goalId, UpdateGoalProgressRequest request);

    void deleteGoal(UUID userId, UUID goalId);

    List<GoalProgressHistoryResponse> getGoalProgressHistory(UUID userId, UUID goalId);
}
