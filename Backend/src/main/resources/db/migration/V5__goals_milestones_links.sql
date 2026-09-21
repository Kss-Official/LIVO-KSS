-- V5__goals_milestones_links.sql
CREATE TABLE goals (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                UUID          NOT NULL,
    title                  VARCHAR(150)  NOT NULL,
    description            VARCHAR(500)  NULL,
    category               VARCHAR(50)   NOT NULL DEFAULT 'PERSONAL',
    priority               VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    target_date            DATE          NULL,
    progress_tracking_type VARCHAR(20)   NOT NULL DEFAULT 'PERCENTAGE',
    target_value           NUMERIC(10,2) NULL,
    current_value          NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    unit                   VARCHAR(30)   NULL,
    reminder_frequency     VARCHAR(20)   NULL,
    status                 VARCHAR(20)   NOT NULL DEFAULT 'IN_PROGRESS',
    version                BIGINT        NOT NULL DEFAULT 1,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ   NULL,

    CONSTRAINT goals_pk          PRIMARY KEY (id),
    CONSTRAINT goals_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT goals_priority_ck CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT goals_tracking_ck CHECK (progress_tracking_type IN ('PERCENTAGE', 'NUMERICAL', 'MILESTONE_BASED')),
    CONSTRAINT goals_status_ck   CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'PAUSED'))
);

CREATE TRIGGER trg_goals_updated_at BEFORE UPDATE ON goals FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE milestones (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    goal_id         UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    title           VARCHAR(150) NOT NULL,
    target_date     DATE         NULL,
    is_completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order      INT          NOT NULL DEFAULT 1,
    is_ai_generated BOOLEAN      NOT NULL DEFAULT FALSE,
    version         BIGINT       NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  NULL,

    CONSTRAINT milestones_pk      PRIMARY KEY (id),
    CONSTRAINT milestones_goal_fk FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE,
    CONSTRAINT milestones_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_milestones_updated_at BEFORE UPDATE ON milestones FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Foreign keys on tasks referencing goals and milestones
ALTER TABLE tasks ADD CONSTRAINT tasks_goal_fk FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL;
ALTER TABLE tasks ADD CONSTRAINT tasks_milestone_fk FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE SET NULL;

CREATE TABLE goal_links (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    goal_id     UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    entity_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT goal_links_pk      PRIMARY KEY (id),
    CONSTRAINT goal_links_goal_fk FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE,
    CONSTRAINT goal_links_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT goal_links_type_ck CHECK (entity_type IN ('TASK', 'HABIT', 'LEARNING', 'EXPENSE', 'TRIP', 'HEALTH')),
    CONSTRAINT goal_links_uq      UNIQUE (goal_id, entity_type, entity_id)
);

CREATE TABLE goal_progress_history (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    goal_id        UUID          NOT NULL,
    user_id        UUID          NOT NULL,
    recorded_date  DATE          NOT NULL DEFAULT CURRENT_DATE,
    previous_value NUMERIC(10,2) NOT NULL,
    new_value      NUMERIC(10,2) NOT NULL,
    notes          VARCHAR(500)  NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT goal_progress_hist_pk      PRIMARY KEY (id),
    CONSTRAINT goal_progress_hist_goal_fk FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE,
    CONSTRAINT goal_progress_hist_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
