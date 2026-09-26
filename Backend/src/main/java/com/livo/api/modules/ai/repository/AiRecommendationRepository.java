package com.livo.api.modules.ai.repository;

import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendationEntity, UUID> {

    List<AiRecommendationEntity> findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<AiRecommendationEntity> findByIdAndUserId(UUID id, UUID userId);

    List<AiRecommendationEntity> findAllByUserIdAndTypeAndIsActiveTrue(UUID userId, AiRecommendationType type);

    Optional<AiRecommendationEntity> findByUserIdAndDedupeKeyAndIsActiveTrue(UUID userId, String dedupeKey);
}
