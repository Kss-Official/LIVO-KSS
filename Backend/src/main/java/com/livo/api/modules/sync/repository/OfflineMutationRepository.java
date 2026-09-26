package com.livo.api.modules.sync.repository;

import com.livo.api.modules.sync.entity.OfflineMutationEntity;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfflineMutationRepository extends JpaRepository<OfflineMutationEntity, UUID> {

    List<OfflineMutationEntity> findAllByUserIdOrderByAppliedAtDesc(UUID userId);

    Optional<OfflineMutationEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    boolean existsByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    List<OfflineMutationEntity> findAllByUserIdAndStatusOrderByAppliedAtDesc(UUID userId, OfflineMutationStatus status);
}
