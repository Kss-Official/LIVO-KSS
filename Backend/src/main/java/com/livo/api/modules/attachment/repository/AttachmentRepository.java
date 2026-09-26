package com.livo.api.modules.attachment.repository;

import com.livo.api.modules.attachment.entity.AttachmentEntity;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    List<AttachmentEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<AttachmentEntity> findAllByUserIdAndEntityTypeAndDeletedAtIsNull(UUID userId, AttachmentEntityType entityType);

    List<AttachmentEntity> findAllByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(UUID userId, AttachmentEntityType entityType, UUID entityId);

    List<AttachmentEntity> findAllByEntityTypeAndEntityIdAndDeletedAtIsNull(AttachmentEntityType entityType, UUID entityId);

    Optional<AttachmentEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<AttachmentEntity> findByStorageKeyAndDeletedAtIsNull(String storageKey);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    long countByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(UUID userId, AttachmentEntityType entityType, UUID entityId);
}
