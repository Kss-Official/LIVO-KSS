-- V12__ai_intelligence_and_audit.sql
CREATE TABLE ai_conversations (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    title      VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ai_conversations_pk      PRIMARY KEY (id),
    CONSTRAINT ai_conversations_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_ai_conversations_updated_at BEFORE UPDATE ON ai_conversations FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE ai_messages (
    id                     UUID        NOT NULL DEFAULT gen_random_uuid(),
    conversation_id        UUID        NOT NULL,
    user_id                UUID        NOT NULL,
    role                   VARCHAR(10) NOT NULL,
    content                TEXT        NOT NULL,
    operating_mode         VARCHAR(20) NULL,
    cards_json             JSONB       NULL,
    suggested_replies_json JSONB       NULL,
    attachment_ids         UUID[]      NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ai_messages_pk       PRIMARY KEY (id),
    CONSTRAINT ai_messages_conv_fk  FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    CONSTRAINT ai_messages_user_fk  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ai_messages_role_ck  CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE TABLE ai_recommendations (
    id                     UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id                UUID         NOT NULL,
    type                   VARCHAR(50)  NOT NULL,
    title                  VARCHAR(150) NOT NULL,
    reason                 TEXT         NOT NULL,
    recommendation_priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    permission_level       VARCHAR(10)  NOT NULL DEFAULT 'SUGGEST',
    related_entity_type    VARCHAR(20)  NULL,
    related_entity_id      UUID         NULL,
    action_type            VARCHAR(50)  NULL,
    action_payload         JSONB        NULL,
    dedupe_key             VARCHAR(100) NULL,
    snooze_until           TIMESTAMPTZ  NULL,
    feedback_type          VARCHAR(20)  NULL,
    user_note              VARCHAR(500) NULL,
    responded_at           TIMESTAMPTZ  NULL,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ai_recommendations_pk        PRIMARY KEY (id),
    CONSTRAINT ai_recommendations_user_fk   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ai_recommendations_perm_ck   CHECK (permission_level IN ('INFORM', 'SUGGEST', 'PREPARE', 'EXECUTE')),
    CONSTRAINT ai_recommendations_feed_ck   CHECK (feedback_type IS NULL OR feedback_type IN ('ACCEPTED', 'DISMISSED', 'SNOOZED'))
);

CREATE INDEX idx_ai_rec_active ON ai_recommendations (user_id, is_active) WHERE is_active = TRUE;

CREATE TABLE ai_proposals (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,
    conversation_id  UUID         NULL,
    action_type      VARCHAR(50)  NOT NULL,
    proposal_payload JSONB        NOT NULL,
    undo_payload     JSONB        NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    confirmed_at     TIMESTAMPTZ  NULL,
    reverted_at      TIMESTAMPTZ  NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ai_proposals_pk      PRIMARY KEY (id),
    CONSTRAINT ai_proposals_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ai_proposals_status_ck CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'REVERTED'))
);

CREATE TABLE ai_audit_logs (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    action_type VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   UUID         NOT NULL,
    old_value   JSONB        NULL,
    new_value   JSONB        NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ai_audit_logs_pk      PRIMARY KEY (id),
    CONSTRAINT ai_audit_logs_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
