package com.livo.api.modules.task.entity;

import com.livo.api.common.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Join entity associating tasks with tags, supporting optimistic locking and soft-delete.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_tags")
public class TaskTagEntity extends BaseSyncEntity {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;
}
