package com.livo.api.modules.health.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.health.dto.CreateHealthEntryRequest;
import com.livo.api.modules.health.dto.DailyHealthSummaryResponse;
import com.livo.api.modules.health.dto.HealthEntryResponse;
import com.livo.api.modules.health.dto.HealthStatsResponse;
import com.livo.api.modules.health.dto.UpdateHealthEntryRequest;
import com.livo.api.modules.health.entity.HealthEntryEntity;
import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
import com.livo.api.modules.health.repository.HealthEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private final HealthEntryRepository healthEntryRepository;
    private final GoalRepository goalRepository;

    @Override
    @Transactional
    public HealthEntryResponse createHealthEntry(UUID userId, CreateHealthEntryRequest request) {
        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }

        if (request.getDurationMins() != null && request.getDurationMins() <= 0) {
            throw new BadRequestException("Duration must be greater than 0");
        }

        HealthEntryEntity entity = HealthEntryEntity.builder()
                .goalId(request.getGoalId())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .healthType(request.getHealthType())
                .entryDate(request.getEntryDate())
                .entryTime(request.getEntryTime())
                .durationMins(request.getDurationMins())
                .intensity(request.getIntensity())
                .metricsJson(request.getMetricsJson() != null ? request.getMetricsJson() : new HashMap<>())
                .notes(request.getNotes())
                .build();

        entity.setUserId(userId);
        entity.setVersion(1L);

        HealthEntryEntity saved = healthEntryRepository.save(entity);
        log.info("Created health entry {} ({}) for user {}", saved.getId(), saved.getTitle(), userId);
        return HealthEntryResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthEntryResponse> getHealthEntries(UUID userId, HealthType healthType, UUID goalId, LocalDate startDate, LocalDate endDate) {
        List<HealthEntryEntity> entries = healthEntryRepository.findFilteredHealthEntries(
                userId, healthType, goalId, startDate, endDate
        );

        return entries.stream()
                .map(HealthEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthEntryResponse getHealthEntry(UUID userId, UUID id) {
        HealthEntryEntity entity = findEntryOrThrow(id, userId);
        return HealthEntryResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public HealthEntryResponse updateHealthEntry(UUID userId, UUID id, UpdateHealthEntryRequest request) {
        HealthEntryEntity entry = findEntryOrThrow(id, userId);

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserIdAndDeletedAtIsNull(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
            entry.setGoalId(request.getGoalId());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            entry.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            entry.setDescription(request.getDescription());
        }
        if (request.getHealthType() != null) {
            entry.setHealthType(request.getHealthType());
        }
        if (request.getEntryDate() != null) {
            entry.setEntryDate(request.getEntryDate());
        }
        if (request.getEntryTime() != null) {
            entry.setEntryTime(request.getEntryTime());
        }
        if (request.getDurationMins() != null) {
            if (request.getDurationMins() <= 0) {
                throw new BadRequestException("Duration must be greater than 0");
            }
            entry.setDurationMins(request.getDurationMins());
        }
        if (request.getIntensity() != null) {
            entry.setIntensity(request.getIntensity());
        }
        if (request.getMetricsJson() != null) {
            entry.setMetricsJson(request.getMetricsJson());
        }
        if (request.getNotes() != null) {
            entry.setNotes(request.getNotes());
        }

        entry.setVersion(entry.getVersion() != null ? entry.getVersion() + 1 : 1L);
        HealthEntryEntity updated = healthEntryRepository.save(entry);
        log.info("Updated health entry {} for user {}", id, userId);
        return HealthEntryResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteHealthEntry(UUID userId, UUID id) {
        HealthEntryEntity entry = findEntryOrThrow(id, userId);
        entry.setDeletedAt(Instant.now());
        entry.setVersion(entry.getVersion() != null ? entry.getVersion() + 1 : 1L);
        healthEntryRepository.save(entry);
        log.info("Soft-deleted health entry {} for user {}", id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyHealthSummaryResponse getDailySummary(UUID userId, LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<HealthEntryEntity> entities = healthEntryRepository
                .findAllByUserIdAndEntryDateAndDeletedAtIsNullOrderByEntryTimeAsc(userId, queryDate);

        List<HealthEntryResponse> responses = entities.stream()
                .map(HealthEntryResponse::fromEntity)
                .collect(Collectors.toList());

        int totalWorkoutMinutes = entities.stream()
                .filter(e -> e.getHealthType() == HealthType.WORKOUT && e.getDurationMins() != null)
                .mapToInt(HealthEntryEntity::getDurationMins)
                .sum();

        Double totalSleepHours = null;
        for (HealthEntryEntity entity : entities) {
            if (entity.getHealthType() == HealthType.SLEEP) {
                if (entity.getMetricsJson() != null && entity.getMetricsJson().containsKey("sleepHours")) {
                    Object val = entity.getMetricsJson().get("sleepHours");
                    if (val instanceof Number num) {
                        totalSleepHours = (totalSleepHours == null ? 0.0 : totalSleepHours) + num.doubleValue();
                    } else if (val instanceof String str) {
                        try {
                            totalSleepHours = (totalSleepHours == null ? 0.0 : totalSleepHours) + Double.parseDouble(str);
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (entity.getDurationMins() != null) {
                    totalSleepHours = (totalSleepHours == null ? 0.0 : totalSleepHours) + (entity.getDurationMins() / 60.0);
                }
            }
        }

        Map<String, Long> entriesByType = entities.stream()
                .collect(Collectors.groupingBy(e -> e.getHealthType().name(), Collectors.counting()));

        return DailyHealthSummaryResponse.builder()
                .date(queryDate)
                .totalEntries(entities.size())
                .totalWorkoutMinutes(totalWorkoutMinutes)
                .totalSleepHours(totalSleepHours)
                .entriesByType(entriesByType)
                .entries(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HealthStatsResponse getHealthStats(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveEnd = (endDate != null) ? endDate : LocalDate.now();
        LocalDate effectiveStart = (startDate != null) ? startDate : effectiveEnd.minusDays(30);

        if (effectiveEnd.isBefore(effectiveStart)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        List<HealthEntryEntity> entries = healthEntryRepository
                .findAllByUserIdAndEntryDateBetweenAndDeletedAtIsNullOrderByEntryDateDescEntryTimeDesc(
                        userId, effectiveStart, effectiveEnd
                );

        long totalEntries = entries.size();

        List<HealthEntryEntity> workoutEntries = entries.stream()
                .filter(e -> e.getHealthType() == HealthType.WORKOUT)
                .toList();

        long totalWorkoutSessions = workoutEntries.size();
        long totalWorkoutMinutes = workoutEntries.stream()
                .filter(e -> e.getDurationMins() != null)
                .mapToLong(HealthEntryEntity::getDurationMins)
                .sum();

        double averageWorkoutDuration = (totalWorkoutSessions > 0)
                ? (double) totalWorkoutMinutes / totalWorkoutSessions
                : 0.0;

        Map<String, Long> entriesByType = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getHealthType().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> intensityBreakdown = entries.stream()
                .filter(e -> e.getIntensity() != null)
                .collect(Collectors.groupingBy(e -> e.getIntensity().name(), LinkedHashMap::new, Collectors.counting()));

        return HealthStatsResponse.builder()
                .startDate(effectiveStart)
                .endDate(effectiveEnd)
                .totalEntries(totalEntries)
                .totalWorkoutMinutes(totalWorkoutMinutes)
                .totalWorkoutSessions(totalWorkoutSessions)
                .averageWorkoutDurationMins(Math.round(averageWorkoutDuration * 10.0) / 10.0)
                .entriesByType(entriesByType)
                .intensityBreakdown(intensityBreakdown)
                .build();
    }

    private HealthEntryEntity findEntryOrThrow(UUID id, UUID userId) {
        return healthEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Health entry not found: " + id));
    }
}
