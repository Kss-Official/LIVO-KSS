-- V8__learning_items_and_sessions.sql
CREATE TABLE learning_items (
    id                         UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                    UUID          NOT NULL,
    goal_id                    UUID          NULL,
    title                      VARCHAR(150)  NOT NULL,
    description                VARCHAR(500)  NULL,
    objective                  VARCHAR(300)  NULL,
    notes                      VARCHAR(1000) NULL,
    learning_type              VARCHAR(20)   NOT NULL DEFAULT 'COURSE',
    category                   VARCHAR(50)   NOT NULL DEFAULT 'TECH',
    difficulty_level           VARCHAR(20)   NOT NULL DEFAULT 'BEGINNER',
    target_study_time_minutes  INT           NOT NULL DEFAULT 30,
    study_frequency            VARCHAR(20)   NOT NULL DEFAULT 'DAILY',
    progress_percentage        SMALLINT      NOT NULL DEFAULT 0,
    status                     VARCHAR(20)   NOT NULL DEFAULT 'IN_PROGRESS',
    start_date                 DATE          NOT NULL,
    target_completion_date     DATE          NULL,
    version                    BIGINT        NOT NULL DEFAULT 1,
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at                 TIMESTAMPTZ   NULL,

    CONSTRAINT learning_items_pk                 PRIMARY KEY (id),
    CONSTRAINT learning_items_user_fk            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT learning_items_id_user_uq         UNIQUE (id, user_id),
    CONSTRAINT learning_items_goal_fk            FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE SET NULL (goal_id),
    CONSTRAINT learning_items_type_ck            CHECK (learning_type IN ('COURSE', 'BOOK', 'VIDEO_SERIES', 'SKILL_PRACTICE', 'MENTORSHIP')),
    CONSTRAINT learning_items_diff_ck            CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT learning_items_progress_ck        CHECK (progress_percentage BETWEEN 0 AND 100),
    CONSTRAINT learning_items_status_ck          CHECK (status IN ('SAVED', 'IN_PROGRESS', 'COMPLETED', 'PAUSED')),
    CONSTRAINT learning_items_target_time_ck     CHECK (target_study_time_minutes > 0),
    CONSTRAINT learning_items_completion_date_ck CHECK (target_completion_date IS NULL OR target_completion_date >= start_date)
);

CREATE INDEX idx_learning_items_goal ON learning_items (goal_id) WHERE goal_id IS NOT NULL;
CREATE TRIGGER trg_learning_items_updated_at BEFORE UPDATE ON learning_items FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE learning_resources (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    learning_item_id UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    resource_type    VARCHAR(20)  NOT NULL DEFAULT 'WEBSITE',
    title            VARCHAR(150) NOT NULL,
    url              TEXT         NULL,
    notes            VARCHAR(500) NULL,
    is_completed     BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order       INT          NOT NULL DEFAULT 1,
    version          BIGINT       NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ  NULL,

    CONSTRAINT learning_resources_pk      PRIMARY KEY (id),
    CONSTRAINT learning_resources_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT learning_resources_item_fk FOREIGN KEY (learning_item_id, user_id) REFERENCES learning_items(id, user_id) ON DELETE CASCADE,
    CONSTRAINT learning_resources_url_ck  CHECK (url IS NULL OR (url ~* '^https?://' AND char_length(url) <= 2048))
);

CREATE INDEX idx_learning_resources_item ON learning_resources (learning_item_id);
CREATE TRIGGER trg_learning_resources_updated_at BEFORE UPDATE ON learning_resources FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE learning_sessions (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    learning_item_id UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    session_date     DATE         NOT NULL,
    duration_minutes INT          NOT NULL,
    notes            VARCHAR(500) NULL,
    version          BIGINT       NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ  NULL,

    CONSTRAINT learning_sessions_pk          PRIMARY KEY (id),
    CONSTRAINT learning_sessions_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT learning_sessions_item_fk     FOREIGN KEY (learning_item_id, user_id) REFERENCES learning_items(id, user_id) ON DELETE CASCADE,
    CONSTRAINT learning_sessions_duration_ck CHECK (duration_minutes BETWEEN 1 AND 1440)
);

CREATE INDEX idx_learning_sessions_item ON learning_sessions (learning_item_id);
CREATE TRIGGER trg_learning_sessions_updated_at BEFORE UPDATE ON learning_sessions FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
