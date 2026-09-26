package com.livo.api.modules.routine.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.routine.dto.CreateRoutineRequest;
import com.livo.api.modules.routine.dto.RoutineResponse;
import com.livo.api.modules.routine.dto.UpdateRoutineRequest;
import com.livo.api.modules.routine.entity.RoutineEntity;
import com.livo.api.modules.routine.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineService {

    private final RoutineRepository routineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> getRoutines(UUID userId, boolean activeOnly) {
        List<RoutineEntity> list = activeOnly
                ? routineRepository.findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId)
                : routineRepository.findAllByUserIdAndDeletedAtIsNull(userId);

        return list.stream()
                .map(RoutineResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineResponse getRoutineById(UUID userId, UUID routineId) {
        RoutineEntity routine = findRoutineOrThrow(userId, routineId);
        return RoutineResponse.fromEntity(routine);
    }

    @Override
    @Transactional
    public RoutineResponse createRoutine(UUID userId, CreateRoutineRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        List<Integer> days = validateDays(request.getDaysOfWeek());

        RoutineEntity routine = RoutineEntity.builder()
                .title(request.getTitle().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .daysOfWeek(days)
                .isActive(request.isActive())
                .build();
        routine.setUserId(userId);
        routine.setVersion(1L);

        return RoutineResponse.fromEntity(routineRepository.save(routine));
    }

    @Override
    @Transactional
    public RoutineResponse updateRoutine(UUID userId, UUID routineId, UpdateRoutineRequest request) {
        RoutineEntity routine = findRoutineOrThrow(userId, routineId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            routine.setTitle(request.getTitle().trim());
        }

        LocalTime newStart = request.getStartTime() != null ? request.getStartTime() : routine.getStartTime();
        LocalTime newEnd = request.getEndTime() != null ? request.getEndTime() : routine.getEndTime();
        validateTimes(newStart, newEnd);
        routine.setStartTime(newStart);
        routine.setEndTime(newEnd);

        if (request.getDaysOfWeek() != null) {
            routine.setDaysOfWeek(validateDays(request.getDaysOfWeek()));
        }
        if (request.getIsActive() != null) {
            routine.setActive(request.getIsActive());
        }

        return RoutineResponse.fromEntity(routineRepository.save(routine));
    }

    @Override
    @Transactional
    public void deleteRoutine(UUID userId, UUID routineId) {
        RoutineEntity routine = findRoutineOrThrow(userId, routineId);
        routine.markDeleted();
        routineRepository.save(routine);
    }

    private RoutineEntity findRoutineOrThrow(UUID userId, UUID routineId) {
        return routineRepository.findByIdAndUserIdAndDeletedAtIsNull(routineId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", routineId));
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Routine start time and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("Routine end time must be after start time");
        }
    }

    private List<Integer> validateDays(List<Integer> days) {
        if (days == null || days.isEmpty()) {
            return List.of(1, 2, 3, 4, 5, 6, 7);
        }
        for (Integer d : days) {
            if (d == null || d < 1 || d > 7) {
                throw new BadRequestException("Day of week must be between 1 (Monday) and 7 (Sunday)");
            }
        }
        return new ArrayList<>(days.stream().distinct().sorted().toList());
    }
}
