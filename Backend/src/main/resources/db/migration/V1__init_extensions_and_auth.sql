-- V1__init_extensions_and_auth.sql

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_protect_created_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.created_at IS DISTINCT FROM OLD.created_at THEN
        RAISE EXCEPTION 'created_at is immutable and cannot be changed on table %', TG_TABLE_NAME;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Users table (Firebase Auth integration)
CREATE TABLE users (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    firebase_uid            VARCHAR(128)  NOT NULL,
    email                   VARCHAR(255)  NOT NULL,
    full_name               VARCHAR(100)  NOT NULL,
    bio                     VARCHAR(300)  NULL,
    avatar_url              TEXT          NULL,
    timezone                VARCHAR(50)   NOT NULL DEFAULT 'Asia/Kolkata',
    language                VARCHAR(10)   NOT NULL DEFAULT 'en',
    currency                VARCHAR(3)    NOT NULL DEFAULT 'INR',
    date_format             VARCHAR(20)   NOT NULL DEFAULT 'DD/MM/YYYY',
    week_start              VARCHAR(3)    NOT NULL DEFAULT 'MON',
    theme                   VARCHAR(10)   NOT NULL DEFAULT 'LIGHT',
    is_onboarded            BOOLEAN       NOT NULL DEFAULT FALSE,
    onboarding_step_reached SMALLINT      NOT NULL DEFAULT 1,
    terms_accepted_version  VARCHAR(20)   NULL,
    terms_accepted_at       TIMESTAMPTZ   NULL,
    is_active               BOOLEAN       NOT NULL DEFAULT TRUE,
    version                 BIGINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ   NULL,

    CONSTRAINT users_pk               PRIMARY KEY (id),
    CONSTRAINT users_firebase_uid_uq  UNIQUE (firebase_uid),
    CONSTRAINT users_full_name_ck     CHECK (LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT users_currency_ck      CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT users_theme_ck         CHECK (theme IN ('LIGHT', 'DARK', 'SYSTEM')),
    CONSTRAINT users_week_start_ck    CHECK (week_start IN ('MON', 'SUN'))
);

CREATE UNIQUE INDEX users_email_lower_uq ON users (LOWER(email));
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_users_protect_created_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION fn_protect_created_at();

-- User preferences
CREATE TABLE user_preferences (
    id                          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id                     UUID         NOT NULL,
    daily_wake_time             TIME         NOT NULL DEFAULT '07:00',
    daily_sleep_time            TIME         NOT NULL DEFAULT '23:00',
    max_planned_hours_per_day   NUMERIC(4,2) NOT NULL DEFAULT 8.00,
    preferred_deep_work_start   TIME         NULL,
    preferred_deep_work_end     TIME         NULL,
    ai_proactive_suggestions    BOOLEAN      NOT NULL DEFAULT TRUE,
    ai_preference_level         VARCHAR(20)  NOT NULL DEFAULT 'BALANCED',
    ai_allow_finance            BOOLEAN      NOT NULL DEFAULT FALSE,
    ai_allow_health             BOOLEAN      NOT NULL DEFAULT FALSE,
    notifications_enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    notification_quiet_start    TIME         NOT NULL DEFAULT '22:00',
    notification_quiet_end      TIME         NOT NULL DEFAULT '07:00',
    dismissed_overload_dates    DATE[]       NOT NULL DEFAULT '{}',
    version                     BIGINT       NOT NULL DEFAULT 1,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT user_preferences_pk          PRIMARY KEY (id),
    CONSTRAINT user_preferences_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT user_preferences_user_uq     UNIQUE (user_id),
    CONSTRAINT user_preferences_max_hours_ck CHECK (max_planned_hours_per_day BETWEEN 1 AND 24),
    CONSTRAINT user_preferences_ai_level_ck CHECK (ai_preference_level IN ('CONSERVATIVE', 'BALANCED', 'PROACTIVE'))
);

CREATE TRIGGER trg_user_preferences_updated_at BEFORE UPDATE ON user_preferences FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User life areas
CREATE TABLE user_life_areas (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    area_name     VARCHAR(20)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order SMALLINT     NOT NULL DEFAULT 1,
    version       BIGINT       NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ  NULL,

    CONSTRAINT user_life_areas_pk      PRIMARY KEY (id),
    CONSTRAINT user_life_areas_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT user_life_areas_name_ck CHECK (area_name IN ('TASKS','CALENDAR','GOALS','HABITS','FINANCE','LEARNING','TRAVEL','HEALTH','INSIGHTS'))
);

CREATE UNIQUE INDEX idx_user_life_areas_uq ON user_life_areas (user_id, area_name) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_user_life_areas_updated_at BEFORE UPDATE ON user_life_areas FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- FCM Device tokens
CREATE TABLE user_device_tokens (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    device_token VARCHAR(500) NOT NULL,
    platform     VARCHAR(10)  NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT user_device_tokens_pk         PRIMARY KEY (id),
    CONSTRAINT user_device_tokens_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT user_device_tokens_token_uq   UNIQUE (device_token),
    CONSTRAINT user_device_tokens_plat_ck    CHECK (platform IN ('ANDROID', 'IOS', 'WEB'))
);

CREATE TRIGGER trg_user_device_tokens_updated_at BEFORE UPDATE ON user_device_tokens FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
