-- V6__habits_and_logs.sql
CREATE TABLE habits (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    goal_id           UUID         NULL,
    title             VARCHAR(150) NOT NULL,
    description       VARCHAR(500) NULL,
    motivation_note   VARCHAR(300) NULL,
    icon_key          VARCHAR(50)  NOT NULL DEFAULT 'WATER',
    color_hex         VARCHAR(7)   NOT NULL DEFAULT '#10B981',
    frequency_type    VARCHAR(20)  NOT NULL DEFAULT 'DAILY',
    custom_days       INT[]        NOT NULL DEFAULT '{1,2,3,4,5,6,7}',
    target_count      INT          NOT NULL DEFAULT 1,
    target_unit       VARCHAR(30)  NOT NULL DEFAULT 'times',
    preferred_time    VARCHAR(20)  NULL,
    reminder_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    reminder_time     TIME         NULL,
    current_streak    INT          NOT NULL DEFAULT 0,
    longest_streak    INT          NOT NULL DEFAULT 0,
    start_date        DATE         NOT NULL DEFAULT CURRENT_DATE,
    end_date          DATE         NULL,
    is_archived       BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 1,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ  NULL,

    CONSTRAINT habits_pk           PRIMARY KEY (id),
    CONSTRAINT habits_user_fk      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT habits_goal_fk      FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
    CONSTRAINT habits_freq_ck      CHECK (frequency_type IN ('DAILY', 'WEEKLY', 'WEEKDAYS', 'SPECIFIC_DAYS', 'CUSTOM')),
    CONSTRAINT habits_pref_time_ck CHECK (preferred_time IS NULL OR preferred_time IN ('MORNING', 'AFTERNOON', 'EVENING', 'SPECIFIC_TIME'))
);

CREATE TRIGGER trg_habits_updated_at BEFORE UPDATE ON habits FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Longest streak trigger function AFTER habits table exists
CREATE OR REPLACE FUNCTION fn_update_longest_streak()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.current_streak > NEW.longest_streak THEN
        NEW.longest_streak = NEW.current_streak;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_habits_longest_streak
    BEFORE UPDATE OF current_streak ON habits
    FOR EACH ROW EXECUTE FUNCTION fn_update_longest_streak();

CREATE TABLE habit_logs (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    habit_id        UUID        NOT NULL,
    user_id         UUID        NOT NULL,
    log_date        DATE        NOT NULL,
    count_completed INT         NOT NULL DEFAULT 1,
    logged_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT      NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ NULL,

    CONSTRAINT habit_logs_pk       PRIMARY KEY (id),
    CONSTRAINT habit_logs_habit_fk FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT habit_logs_user_fk  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT habit_logs_unique   UNIQUE (habit_id, log_date)
);

CREATE INDEX idx_habit_logs_lookup ON habit_logs (habit_id, log_date) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_habit_logs_updated_at BEFORE UPDATE ON habit_logs FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
