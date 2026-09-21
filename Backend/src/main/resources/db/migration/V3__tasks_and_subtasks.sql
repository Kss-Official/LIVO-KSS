-- V3__tasks_and_subtasks.sql
CREATE TABLE tasks (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                UUID          NOT NULL,
    goal_id                UUID          NULL,
    milestone_id           UUID          NULL,
    trip_id                UUID          NULL,
    title                  VARCHAR(150)  NOT NULL,
    description            VARCHAR(500)  NULL,
    project_label          VARCHAR(100)  NULL,
    due_date               DATE          NULL,
    due_time               TIME          NULL,
    duration_mins          INT           NOT NULL DEFAULT 30,
    actual_duration_mins   INT           NULL,
    started_at             TIMESTAMPTZ   NULL,
    priority               VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    category               VARCHAR(50)   NOT NULL DEFAULT 'WORK',
    status                 VARCHAR(20)   NOT NULL DEFAULT 'TODO',
    repeat_rule            VARCHAR(100)  NULL,
    reminder_minutes_before INT          NULL,
    completed_at           TIMESTAMPTZ   NULL,
    version                BIGINT        NOT NULL DEFAULT 1,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ   NULL,

    CONSTRAINT tasks_pk           PRIMARY KEY (id),
    CONSTRAINT tasks_user_fk      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT tasks_priority_ck  CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT tasks_status_ck    CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT tasks_duration_ck  CHECK (duration_mins > 0)
);

CREATE INDEX idx_tasks_user_status ON tasks (user_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_due ON tasks (user_id, due_date, due_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_title_trgm ON tasks USING gin (title gin_trgm_ops);

CREATE TRIGGER trg_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_tasks_protect_created_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION fn_protect_created_at();

CREATE TABLE task_tags (
    task_id    UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag_id     UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT task_tags_pk PRIMARY KEY (task_id, tag_id)
);

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
    CONSTRAINT subtasks_task_fk FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT subtasks_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_subtasks_updated_at BEFORE UPDATE ON subtasks FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
