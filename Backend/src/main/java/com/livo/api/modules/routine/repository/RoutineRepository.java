package com.livo.api.modules.routine.repository;

import com.livo.api.modules.routine.entity.RoutineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutineRepository extends JpaRepository<RoutineEntity, UUID> {

    List<RoutineEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<RoutineEntity> findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID userId);

    Optional<RoutineEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
