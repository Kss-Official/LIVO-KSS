package com.livo.api.modules.goal.repository;

import com.livo.api.modules.goal.entity.GoalEntity;
import com.livo.api.modules.goal.entity.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {

    List<GoalEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<GoalEntity> findAllByUserIdAndStatusAndDeletedAtIsNull(UUID userId, GoalStatus status);

    Optional<GoalEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<GoalEntity> findAllByUserIdAndRelatedAreaAndDeletedAtIsNull(UUID userId, String relatedArea);

    List<GoalEntity> findAllByUserIdAndStatusAndRelatedAreaAndDeletedAtIsNull(UUID userId, GoalStatus status, String relatedArea);

    long countByUserIdAndStatusAndDeletedAtIsNull(UUID userId, GoalStatus status);

    List<GoalEntity> findAllByUserIdAndUpdatedAtAfter(UUID userId, java.time.Instant since);

    Optional<GoalEntity> findByIdAndUserId(UUID id, UUID userId);
 
    @org.springframework.data.jpa.repository.Query("SELECT g FROM GoalEntity g WHERE g.userId = :userId AND g.deletedAt IS NULL AND (" +
           "LOWER(g.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(g.description IS NOT NULL AND LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(g.targetDescription IS NOT NULL AND LOWER(g.targetDescription) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<GoalEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}

