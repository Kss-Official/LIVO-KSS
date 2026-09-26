-- V16__add_tokens_revoked_before_to_users.sql

ALTER TABLE users ADD COLUMN IF NOT EXISTS tokens_revoked_before TIMESTAMPTZ NULL;
