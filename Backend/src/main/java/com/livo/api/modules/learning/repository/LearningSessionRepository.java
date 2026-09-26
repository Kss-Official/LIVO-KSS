package com.livo.api.modules.learning.repository;

import com.livo.api.modules.learning.entity.LearningSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSessionEntity, UUID> {

    List<LearningSessionEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<LearningSessionEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<LearningSessionEntity> findAllByLearningItemIdAndUserIdAndDeletedAtIsNullOrderBySessionDateDesc(UUID learningItemId, UUID userId);

    List<LearningSessionEntity> findAllByLearningItemIdInAndUserIdAndDeletedAtIsNull(Collection<UUID> learningItemIds, UUID userId);

    List<LearningSessionEntity> findAllByUserIdAndSessionDateBetweenAndDeletedAtIsNullOrderBySessionDateDesc(
            UUID userId, LocalDate startDate, LocalDate endDate
    );

    List<LearningSessionEntity> findAllByUserIdAndDeletedAtIsNullOrderBySessionDateDesc(UUID userId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
