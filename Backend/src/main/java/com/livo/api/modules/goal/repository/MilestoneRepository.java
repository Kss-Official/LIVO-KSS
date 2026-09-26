package com.livo.api.modules.goal.repository;

import com.livo.api.modules.goal.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, UUID> {

    List<MilestoneEntity> findAllByGoalIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID goalId, UUID userId);

    List<MilestoneEntity> findAllByGoalIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(Collection<UUID> goalIds, UUID userId);

    List<MilestoneEntity> findAllByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID goalId);

    Optional<MilestoneEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<MilestoneEntity> findByIdAndGoalIdAndUserIdAndDeletedAtIsNull(UUID id, UUID goalId, UUID userId);

    long countByGoalIdAndUserIdAndDeletedAtIsNull(UUID goalId, UUID userId);

    long countByGoalIdAndUserIdAndIsCompletedTrueAndDeletedAtIsNull(UUID goalId, UUID userId);
}
