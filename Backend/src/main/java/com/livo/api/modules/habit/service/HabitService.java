package com.livo.api.modules.habit.service;

import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitHistoryResponse;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.dto.LogHabitRequest;
import com.livo.api.modules.habit.dto.UpdateHabitRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HabitService {

    HabitResponse createHabit(UUID userId, CreateHabitRequest request);

    HabitResponse getHabitById(UUID userId, UUID habitId);

    List<HabitResponse> getHabits(UUID userId, Boolean archived, UUID goalId);

    HabitResponse updateHabit(UUID userId, UUID habitId, UpdateHabitRequest request);

    void deleteHabit(UUID userId, UUID habitId);

    HabitResponse toggleArchive(UUID userId, UUID habitId);

    HabitResponse logHabit(UUID userId, UUID habitId, LogHabitRequest request);

    HabitResponse unlogHabit(UUID userId, UUID habitId, LocalDate logDate);

    HabitHistoryResponse getHabitHistory(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate);
}
