package com.livo.api.modules.user.repository;

import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceTokenEntity, UUID> {

    List<UserDeviceTokenEntity> findAllByUserIdAndIsActiveTrue(UUID userId);

    Optional<UserDeviceTokenEntity> findByDeviceToken(String deviceToken);

    Optional<UserDeviceTokenEntity> findByUserIdAndDeviceToken(UUID userId, String deviceToken);

    void deleteAllByUserIdAndDeviceToken(UUID userId, String deviceToken);

    @Modifying
    @Query("UPDATE UserDeviceTokenEntity t SET t.isActive = false WHERE t.deviceToken = :deviceToken")
    void deactivateToken(@Param("deviceToken") String deviceToken);
}
