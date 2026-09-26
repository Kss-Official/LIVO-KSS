package com.livo.api.modules.ai.repository;

import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiProposalRepository extends JpaRepository<AiProposalEntity, UUID> {

    List<AiProposalEntity> findAllByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, AiProposalStatus status);

    Optional<AiProposalEntity> findByIdAndUserId(UUID id, UUID userId);

    List<AiProposalEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
