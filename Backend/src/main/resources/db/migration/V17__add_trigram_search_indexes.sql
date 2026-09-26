-- V17__add_trigram_search_indexes.sql
-- Optimizes substring and keyword searches across core domain entities using GIN trigram indexes

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_tasks_title_trgm ON tasks USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_tasks_description_trgm ON tasks USING gin (description gin_trgm_ops) WHERE description IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_goals_title_trgm ON goals USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_goals_description_trgm ON goals USING gin (description gin_trgm_ops) WHERE description IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_events_title_trgm ON events USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_events_description_trgm ON events USING gin (description gin_trgm_ops) WHERE description IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_habits_title_trgm ON habits USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_learning_title_trgm ON learning_items USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_transactions_title_trgm ON transactions USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_trips_title_trgm ON trips USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_trips_destination_trgm ON trips USING gin (destination gin_trgm_ops) WHERE destination IS NOT NULL;
