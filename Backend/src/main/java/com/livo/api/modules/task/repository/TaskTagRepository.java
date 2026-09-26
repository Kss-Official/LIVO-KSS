package com.livo.api.modules.task.repository;

import com.livo.api.modules.task.entity.TaskTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskTagRepository extends JpaRepository<TaskTagEntity, UUID> {

    List<TaskTagEntity> findAllByTaskIdAndUserIdAndDeletedAtIsNull(UUID taskId, UUID userId);

    List<TaskTagEntity> findAllByTaskIdInAndUserIdAndDeletedAtIsNull(Collection<UUID> taskIds, UUID userId);

    List<TaskTagEntity> findAllByTagIdAndUserIdAndDeletedAtIsNull(UUID tagId, UUID userId);

    Optional<TaskTagEntity> findByTaskIdAndTagIdAndDeletedAtIsNull(UUID taskId, UUID tagId);

    boolean existsByTaskIdAndTagIdAndDeletedAtIsNull(UUID taskId, UUID tagId);
}
