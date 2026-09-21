-- V13__notifications_and_sync.sql
CREATE TABLE notifications (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL,
    type                VARCHAR(30)  NOT NULL,
    title               VARCHAR(150) NOT NULL,
    body                VARCHAR(500) NOT NULL,
    related_entity_type VARCHAR(20)  NULL,
    related_entity_id   UUID         NULL,
    is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMPTZ  NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ  NULL,

    CONSTRAINT notifications_pk      PRIMARY KEY (id),
    CONSTRAINT notifications_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_unread ON notifications (user_id, is_read) WHERE is_read = FALSE;
CREATE TRIGGER trg_notifications_updated_at BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE offline_mutations (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,
    idempotency_key  VARCHAR(100) NOT NULL,
    domain_entity    VARCHAR(50)  NOT NULL,
    action           VARCHAR(20)  NOT NULL,
    payload          JSONB        NOT NULL,
    client_timestamp TIMESTAMPTZ  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'APPLIED',
    applied_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT offline_mutations_pk         PRIMARY KEY (id),
    CONSTRAINT offline_mutations_user_fk    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT offline_mutations_user_idemp UNIQUE (user_id, idempotency_key)
);
