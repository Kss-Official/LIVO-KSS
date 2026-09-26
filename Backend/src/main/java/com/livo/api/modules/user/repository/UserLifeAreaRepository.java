package com.livo.api.modules.user.repository;

import com.livo.api.modules.user.entity.UserLifeAreaEntity;
import com.livo.api.modules.user.entity.enums.LifeAreaName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLifeAreaRepository extends JpaRepository<UserLifeAreaEntity, UUID> {

    List<UserLifeAreaEntity> findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(UUID userId);

    List<UserLifeAreaEntity> findAllByUserIdAndIsActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAsc(UUID userId);

    Optional<UserLifeAreaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<UserLifeAreaEntity> findByUserIdAndAreaNameAndDeletedAtIsNull(UUID userId, LifeAreaName areaName);

    boolean existsByUserIdAndAreaNameAndDeletedAtIsNull(UUID userId, LifeAreaName areaName);
}
