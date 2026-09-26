package com.livo.api.modules.user.repository;

import com.livo.api.modules.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    List<UserEntity> findAllByDeletedAtIsNull();

    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<UserEntity> findByFirebaseUidAndDeletedAtIsNull(String firebaseUid);

    Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT u.timezone FROM UserEntity u WHERE u.deletedAt IS NULL AND u.timezone IS NOT NULL AND u.timezone != ''")
    List<String> findDistinctActiveTimezones();
}
