package com.livo.api.modules.learning.repository;

import com.livo.api.modules.learning.entity.LearningResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResourceEntity, UUID> {

    List<LearningResourceEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<LearningResourceEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<LearningResourceEntity> findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID learningItemId, UUID userId);

    List<LearningResourceEntity> findAllByLearningItemIdInAndUserIdAndDeletedAtIsNullOrderBySortOrderAsc(Collection<UUID> learningItemIds, UUID userId);

    List<LearningResourceEntity> findAllByLearningItemIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID learningItemId);

    Optional<LearningResourceEntity> findByIdAndLearningItemIdAndUserIdAndDeletedAtIsNull(UUID id, UUID learningItemId, UUID userId);

    long countByLearningItemIdAndUserIdAndDeletedAtIsNull(UUID learningItemId, UUID userId);

    long countByLearningItemIdAndUserIdAndIsCompletedTrueAndDeletedAtIsNull(UUID learningItemId, UUID userId);
}
