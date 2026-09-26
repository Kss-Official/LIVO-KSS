package com.livo.api.modules.notification.repository;

import com.livo.api.modules.notification.entity.NotificationEntity;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    List<NotificationEntity> findAllByUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    List<NotificationEntity> findAllByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, NotificationType type);

    List<NotificationEntity> findAllByUserIdAndTypeAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, NotificationType type);

    Optional<NotificationEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    long countByUserIdAndReadAtIsNullAndDeletedAtIsNull(UUID userId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(n) > 0 FROM NotificationEntity n " +
            "WHERE n.userId = :userId AND n.type = :type " +
            "AND n.title LIKE CONCAT(:titlePrefix, '%') AND n.createdAt >= :since " +
            "AND n.deletedAt IS NULL")
    boolean existsNotificationSince(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("type") NotificationType type,
            @org.springframework.data.repository.query.Param("titlePrefix") String titlePrefix,
            @org.springframework.data.repository.query.Param("since") java.time.Instant since
    );

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(n) > 0 FROM NotificationEntity n " +
            "WHERE n.userId = :userId AND n.type = com.livo.api.modules.notification.entity.enums.NotificationType.BUDGET " +
            "AND n.title LIKE CONCAT(:titlePrefix, '%') " +
            "AND LOWER(n.body) LIKE LOWER(CONCAT('%', :category, '%')) " +
            "AND n.createdAt >= :since AND n.deletedAt IS NULL")
    boolean existsBudgetAlertSince(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("titlePrefix") String titlePrefix,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("since") java.time.Instant since
    );
}
