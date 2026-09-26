package com.livo.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base JPA Entity for domain models with multi-tenant user_id,
 * optimistic version bump, and soft-delete support for offline sync.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseSyncEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 1L;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
