package com.livo.api.modules.learning.repository;

import com.livo.api.modules.learning.entity.LearningItemEntity;
import com.livo.api.modules.learning.entity.enums.LearningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningItemRepository extends JpaRepository<LearningItemEntity, UUID>, JpaSpecificationExecutor<LearningItemEntity> {

    List<LearningItemEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<LearningItemEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<LearningItemEntity> findAllByUserIdAndStatusAndDeletedAtIsNull(UUID userId, LearningStatus status);

    List<LearningItemEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    List<LearningItemEntity> findAllByUserIdAndCategoryAndDeletedAtIsNull(UUID userId, String category);

    long countByUserIdAndStatusAndDeletedAtIsNull(UUID userId, LearningStatus status);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT l FROM LearningItemEntity l WHERE l.userId = :userId AND l.deletedAt IS NULL " +
            "AND (:status IS NULL OR l.status = :status) " +
            "AND (:category IS NULL OR LOWER(l.category) = :category) " +
            "AND (:goalId IS NULL OR l.goalId = :goalId) " +
            "ORDER BY l.createdAt DESC")
    List<LearningItemEntity> findFilteredLearningItems(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("status") LearningStatus status,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("goalId") UUID goalId
    );
 
    @org.springframework.data.jpa.repository.Query("SELECT l FROM LearningItemEntity l WHERE l.userId = :userId AND l.deletedAt IS NULL AND (" +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(l.description IS NOT NULL AND LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(l.notes IS NOT NULL AND LOWER(l.notes) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(l.category IS NOT NULL AND LOWER(l.category) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<LearningItemEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}
