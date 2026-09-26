package com.livo.api.modules.health.service;

import com.livo.api.modules.health.dto.CreateHealthEntryRequest;
import com.livo.api.modules.health.dto.DailyHealthSummaryResponse;
import com.livo.api.modules.health.dto.HealthEntryResponse;
import com.livo.api.modules.health.dto.HealthStatsResponse;
import com.livo.api.modules.health.dto.UpdateHealthEntryRequest;
import com.livo.api.modules.health.entity.enums.HealthType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HealthService {

    HealthEntryResponse createHealthEntry(UUID userId, CreateHealthEntryRequest request);

    List<HealthEntryResponse> getHealthEntries(UUID userId, HealthType healthType, UUID goalId, LocalDate startDate, LocalDate endDate);

    HealthEntryResponse getHealthEntry(UUID userId, UUID id);

    HealthEntryResponse updateHealthEntry(UUID userId, UUID id, UpdateHealthEntryRequest request);

    void deleteHealthEntry(UUID userId, UUID id);

    DailyHealthSummaryResponse getDailySummary(UUID userId, LocalDate date);

    HealthStatsResponse getHealthStats(UUID userId, LocalDate startDate, LocalDate endDate);
}
