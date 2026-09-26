package com.livo.api.modules.task.repository;

import com.livo.api.modules.task.entity.SubtaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubtaskRepository extends JpaRepository<SubtaskEntity, UUID> {

    List<SubtaskEntity> findAllByTaskIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID taskId, UUID userId);

    List<SubtaskEntity> findAllByTaskIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(Collection<UUID> taskIds, UUID userId);

    List<SubtaskEntity> findAllByTaskIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID taskId);

    Optional<SubtaskEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
