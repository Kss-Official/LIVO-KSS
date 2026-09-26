package com.livo.api.modules.ai.repository;

import com.livo.api.modules.ai.entity.AiAuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiAuditLogRepository extends JpaRepository<AiAuditLogEntity, UUID> {

    List<AiAuditLogEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<AiAuditLogEntity> findAllByUserId(UUID userId, Pageable pageable);

    List<AiAuditLogEntity> findAllByUserIdAndProposalId(UUID userId, UUID proposalId);

    List<AiAuditLogEntity> findAllByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);
}
