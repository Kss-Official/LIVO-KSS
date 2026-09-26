package com.livo.api.modules.user.repository;

import com.livo.api.modules.user.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UUID> {

    Optional<UserPreferenceEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(UUID userId);
}
