-- V10__health_entries.sql
CREATE TABLE health_entries (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    goal_id       UUID         NULL,
    title         VARCHAR(150) NOT NULL,
    description   VARCHAR(500) NULL,
    health_type   VARCHAR(20)  NOT NULL,
    entry_date    DATE         NOT NULL,
    entry_time    TIME         NULL,
    duration_mins INT          NULL,
    intensity     VARCHAR(10)  NULL,
    metrics_json  JSONB        NOT NULL DEFAULT '{}'::jsonb,
    notes         VARCHAR(500) NULL,
    version       BIGINT       NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ  NULL,

    CONSTRAINT health_entries_pk         PRIMARY KEY (id),
    CONSTRAINT health_entries_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT health_entries_goal_fk    FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE SET NULL (goal_id),
    CONSTRAINT health_entries_type_ck    CHECK (health_type IN ('WORKOUT', 'NUTRITION', 'MEDICAL', 'SLEEP', 'CHECKUP', 'MENTAL_HEALTH')),
    CONSTRAINT health_entries_intens_ck  CHECK (intensity IS NULL OR intensity IN ('LOW', 'MODERATE', 'HIGH')),
    CONSTRAINT health_entries_dur_ck     CHECK (duration_mins IS NULL OR duration_mins > 0),
    CONSTRAINT health_entries_metrics_ck CHECK (jsonb_typeof(metrics_json) = 'object')
);

CREATE INDEX idx_health_entries_user_date ON health_entries (user_id, entry_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_health_entries_goal ON health_entries (goal_id) WHERE goal_id IS NOT NULL;
CREATE TRIGGER trg_health_entries_updated_at BEFORE UPDATE ON health_entries FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
