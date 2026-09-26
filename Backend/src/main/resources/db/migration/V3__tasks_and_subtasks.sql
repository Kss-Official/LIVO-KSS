-- V3__tasks_and_subtasks.sql
CREATE TABLE tasks (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                 UUID          NOT NULL,
    goal_id                 UUID          NULL,
    milestone_id            UUID          NULL,
    trip_id                 UUID          NULL,
    title                   VARCHAR(150)  NOT NULL,
    description             VARCHAR(500)  NULL,
    project_label           VARCHAR(100)  NULL,
    due_date                DATE          NULL,
    due_time                TIME          NULL,
    duration_mins           INT           NOT NULL DEFAULT 30,
    actual_duration_mins    INT           NULL,
    started_at              TIMESTAMPTZ   NULL,
    priority                VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    category                VARCHAR(50)   NOT NULL DEFAULT 'WORK',
    status                  VARCHAR(20)   NOT NULL DEFAULT 'TODO',
    repeat_rule             VARCHAR(100)  NULL,
    reminder_minutes_before INT           NULL,
    completed_at            TIMESTAMPTZ   NULL,
    completed_dates         DATE[]        NOT NULL DEFAULT '{}',
    skipped_dates           DATE[]        NOT NULL DEFAULT '{}',
    version                 BIGINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ   NULL,

    CONSTRAINT tasks_pk                 PRIMARY KEY (id),
    CONSTRAINT tasks_user_fk            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT tasks_id_user_uq         UNIQUE (id, user_id),
    CONSTRAINT tasks_priority_ck        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT tasks_status_ck          CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT tasks_duration_ck        CHECK (duration_mins > 0),
    CONSTRAINT tasks_actual_duration_ck CHECK (actual_duration_mins IS NULL OR actual_duration_mins >= 0),
    CONSTRAINT tasks_reminder_ck        CHECK (reminder_minutes_before IS NULL OR reminder_minutes_before >= 0)
);

CREATE INDEX idx_tasks_user_status ON tasks (user_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_due ON tasks (user_id, due_date, due_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_goal ON tasks (goal_id) WHERE goal_id IS NOT NULL;
CREATE INDEX idx_tasks_milestone ON tasks (milestone_id) WHERE milestone_id IS NOT NULL;
CREATE INDEX idx_tasks_trip ON tasks (trip_id) WHERE trip_id IS NOT NULL;

CREATE TRIGGER trg_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_tasks_protect_created_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION fn_protect_created_at();

CREATE TABLE task_tags (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    task_id    UUID        NOT NULL,
    tag_id     UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    version    BIGINT      NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT task_tags_pk      PRIMARY KEY (id),
    CONSTRAINT task_tags_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT task_tags_task_fk FOREIGN KEY (task_id, user_id) REFERENCES tasks(id, user_id) ON DELETE CASCADE,
    CONSTRAINT task_tags_tag_fk  FOREIGN KEY (tag_id, user_id) REFERENCES tags(id, user_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_task_tags_unique ON task_tags (task_id, tag_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_task_tags_tag ON task_tags (tag_id);
CREATE TRIGGER trg_task_tags_updated_at BEFORE UPDATE ON task_tags FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE subtasks (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    task_id      UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    title        VARCHAR(150) NOT NULL,
    is_completed BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order   INT          NOT NULL DEFAULT 1,
    version      BIGINT       NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ  NULL,

    CONSTRAINT subtasks_pk      PRIMARY KEY (id),
    CONSTRAINT subtasks_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT subtasks_task_fk FOREIGN KEY (task_id, user_id) REFERENCES tasks(id, user_id) ON DELETE CASCADE
);

CREATE INDEX idx_subtasks_task ON subtasks (task_id);
CREATE TRIGGER trg_subtasks_updated_at BEFORE UPDATE ON subtasks FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
