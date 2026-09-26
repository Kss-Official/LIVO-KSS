-- V2__tags_and_categories.sql
CREATE TABLE tags (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    color_hex   VARCHAR(7)  NOT NULL DEFAULT '#6B7280',
    version     BIGINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,

    CONSTRAINT tags_pk        PRIMARY KEY (id),
    CONSTRAINT tags_user_fk   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT tags_id_user_uq UNIQUE (id, user_id)
);

CREATE UNIQUE INDEX idx_tags_user_name ON tags (user_id, name) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_tags_updated_at BEFORE UPDATE ON tags FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE categories (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    icon_key    VARCHAR(50) NOT NULL DEFAULT 'folder',
    color_hex   VARCHAR(7)  NOT NULL DEFAULT '#3B82F6',
    domain_type VARCHAR(20) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,

    CONSTRAINT categories_pk        PRIMARY KEY (id),
    CONSTRAINT categories_user_fk   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT categories_domain_ck CHECK (domain_type IN ('TASK', 'EVENT', 'EXPENSE'))
);

CREATE UNIQUE INDEX idx_categories_user_domain_name ON categories (user_id, domain_type, name) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_categories_updated_at BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
