package com.livo.api.modules.tag.repository;

import com.livo.api.modules.tag.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    List<TagEntity> findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(UUID userId);

    List<TagEntity> findAllByIdInAndUserIdAndDeletedAtIsNull(Collection<UUID> ids, UUID userId);

    Optional<TagEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<TagEntity> findByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID userId, String name);
}
