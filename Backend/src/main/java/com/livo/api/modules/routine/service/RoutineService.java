package com.livo.api.modules.routine.service;

import com.livo.api.modules.routine.dto.CreateRoutineRequest;
import com.livo.api.modules.routine.dto.RoutineResponse;
import com.livo.api.modules.routine.dto.UpdateRoutineRequest;

import java.util.List;
import java.util.UUID;

public interface RoutineService {

    List<RoutineResponse> getRoutines(UUID userId, boolean activeOnly);

    RoutineResponse getRoutineById(UUID userId, UUID routineId);

    RoutineResponse createRoutine(UUID userId, CreateRoutineRequest request);

    RoutineResponse updateRoutine(UUID userId, UUID routineId, UpdateRoutineRequest request);

    void deleteRoutine(UUID userId, UUID routineId);
}
