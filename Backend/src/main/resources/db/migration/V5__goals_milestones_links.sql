-- V5__goals_milestones_links.sql
CREATE TABLE goals (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                UUID          NOT NULL,
    title                  VARCHAR(150)  NOT NULL,
    description            VARCHAR(500)  NULL,
    related_area           VARCHAR(20)   NULL,
    target_description     VARCHAR(200)  NULL,
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
    CONSTRAINT goals_id_user_uq  UNIQUE (id, user_id),
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

    CONSTRAINT milestones_pk         PRIMARY KEY (id),
    CONSTRAINT milestones_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT milestones_goal_fk    FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE CASCADE,
    CONSTRAINT milestones_id_user_uq UNIQUE (id, user_id)
);

CREATE INDEX idx_milestones_goal ON milestones (goal_id);
CREATE TRIGGER trg_milestones_updated_at BEFORE UPDATE ON milestones FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Foreign keys on tasks referencing goals and milestones (same-user composite FK)
ALTER TABLE tasks ADD CONSTRAINT tasks_goal_fk
  FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE SET NULL (goal_id);

ALTER TABLE tasks ADD CONSTRAINT tasks_milestone_fk
  FOREIGN KEY (milestone_id, user_id) REFERENCES milestones(id, user_id) ON DELETE SET NULL (milestone_id);

CREATE TABLE goal_progress_history (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    goal_id        UUID          NOT NULL,
    user_id        UUID          NOT NULL,
    recorded_date  DATE          NOT NULL,
    previous_value NUMERIC(10,2) NOT NULL,
    new_value      NUMERIC(10,2) NOT NULL,
    notes          VARCHAR(500)  NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT goal_progress_hist_pk      PRIMARY KEY (id),
    CONSTRAINT goal_progress_hist_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT goal_progress_hist_goal_fk FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE CASCADE
);

CREATE INDEX idx_goal_progress_hist_goal ON goal_progress_history (goal_id);
