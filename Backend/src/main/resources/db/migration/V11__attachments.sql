-- V11__attachments.sql
CREATE TABLE attachments (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    entity_type     VARCHAR(20)  NOT NULL,
    entity_id       UUID         NOT NULL,
    storage_key     VARCHAR(255) NOT NULL,
    file_url        TEXT         NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  NULL,

    CONSTRAINT attachments_pk      PRIMARY KEY (id),
    CONSTRAINT attachments_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT attachments_type_ck CHECK (entity_type IN ('TASK', 'GOAL', 'EVENT', 'TRIP', 'EXPENSE', 'HEALTH')),
    CONSTRAINT attachments_size_ck CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
    CONSTRAINT attachments_mime_ck CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/webp', 'application/pdf')),
    CONSTRAINT attachments_url_ck  CHECK (file_url ~ '^https://')
);

CREATE INDEX idx_attachments_entity ON attachments (entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_attachments_updated_at BEFORE UPDATE ON attachments FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
