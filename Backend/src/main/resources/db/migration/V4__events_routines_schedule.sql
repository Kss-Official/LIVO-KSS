-- V4__events_routines_schedule.sql
CREATE TABLE events (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL,
    trip_id          UUID          NULL,
    title            VARCHAR(150)  NOT NULL,
    description      VARCHAR(500)  NULL,
    location         VARCHAR(200)  NULL,
    format           VARCHAR(20)   NOT NULL DEFAULT 'IN_PERSON',
    priority         VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    category         VARCHAR(50)   NOT NULL DEFAULT 'GENERAL',
    start_time       TIMESTAMPTZ   NOT NULL,
    end_time         TIMESTAMPTZ   NOT NULL,
    reminder_minutes INT           NULL,
    repeat_rule      VARCHAR(100)  NULL,
    skipped_dates    DATE[]        NOT NULL DEFAULT '{}',
    notes            VARCHAR(1000) NULL,
    version          BIGINT        NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ   NULL,

    CONSTRAINT events_pk            PRIMARY KEY (id),
    CONSTRAINT events_user_fk       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT events_id_user_uq    UNIQUE (id, user_id),
    CONSTRAINT events_format_ck     CHECK (format IN ('IN_PERSON', 'ONLINE', 'PHONE')),
    CONSTRAINT events_priority_ck   CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT events_time_order_ck CHECK (end_time > start_time)
);

CREATE INDEX idx_events_user_time ON events (user_id, start_time, end_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_events_trip ON events (trip_id) WHERE trip_id IS NOT NULL;
CREATE TRIGGER trg_events_updated_at BEFORE UPDATE ON events FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE routines (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    title        VARCHAR(100) NOT NULL,
    start_time   TIME         NOT NULL,
    end_time     TIME         NOT NULL,
    days_of_week INT[]        NOT NULL DEFAULT '{1,2,3,4,5,6,7}',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    version      BIGINT       NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ  NULL,

    CONSTRAINT routines_pk         PRIMARY KEY (id),
    CONSTRAINT routines_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT routines_time_order CHECK (end_time <> start_time),
    CONSTRAINT routines_days_ck    CHECK (cardinality(days_of_week) > 0 AND days_of_week <@ ARRAY[1,2,3,4,5,6,7])
);

CREATE TRIGGER trg_routines_updated_at BEFORE UPDATE ON routines FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE schedule_blocks (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    task_id    UUID         NULL,
    event_id   UUID         NULL,
    habit_id   UUID         NULL,
    block_date DATE         NOT NULL,
    start_time TIME         NOT NULL,
    end_time   TIME         NOT NULL,
    title      VARCHAR(150) NOT NULL,
    category   VARCHAR(50)  NOT NULL DEFAULT 'PERSONAL',
    is_locked  BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ  NULL,

    CONSTRAINT schedule_blocks_pk         PRIMARY KEY (id),
    CONSTRAINT schedule_blocks_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT schedule_blocks_task_fk    FOREIGN KEY (task_id, user_id) REFERENCES tasks(id, user_id) ON DELETE SET NULL (task_id),
    CONSTRAINT schedule_blocks_event_fk   FOREIGN KEY (event_id, user_id) REFERENCES events(id, user_id) ON DELETE SET NULL (event_id),
    CONSTRAINT schedule_blocks_time_order CHECK (end_time <> start_time)
);

CREATE INDEX idx_schedule_blocks_user_date ON schedule_blocks (user_id, block_date, start_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_schedule_blocks_task ON schedule_blocks (task_id) WHERE task_id IS NOT NULL;
CREATE INDEX idx_schedule_blocks_event ON schedule_blocks (event_id) WHERE event_id IS NOT NULL;
CREATE TRIGGER trg_schedule_blocks_updated_at BEFORE UPDATE ON schedule_blocks FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
