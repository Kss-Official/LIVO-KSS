-- V14__bulk_version_sync_rls.sql

-- 1) version bump on every table that has version + updated_at
CREATE OR REPLACE FUNCTION fn_bump_version() RETURNS trigger AS $$
BEGIN
  NEW.version := OLD.version + 1;
  NEW.updated_at := NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ DECLARE r record; BEGIN
  FOR r IN SELECT c.table_name FROM information_schema.columns c
           WHERE c.table_schema='public' AND c.column_name='version'
             AND EXISTS (SELECT 1 FROM information_schema.columns u
                         WHERE u.table_schema='public' AND u.table_name=c.table_name AND u.column_name='updated_at')
  LOOP
    EXECUTE format('DROP TRIGGER IF EXISTS trg_%s_updated_at ON %I', r.table_name, r.table_name);
    EXECUTE format('DROP TRIGGER IF EXISTS trg_%s_bump ON %I', r.table_name, r.table_name);
    EXECUTE format('CREATE TRIGGER trg_%s_bump BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION fn_bump_version()', r.table_name, r.table_name);
  END LOOP;
END $$;

-- 2) sync index (NOT partial: deleted rows must be pulled too)
DO $$ DECLARE r record; BEGIN
  FOR r IN SELECT c.table_name FROM information_schema.columns c
           WHERE c.table_schema='public' AND c.column_name='user_id'
             AND EXISTS (SELECT 1 FROM information_schema.columns u
                         WHERE u.table_schema='public' AND u.table_name=c.table_name AND u.column_name='updated_at')
  LOOP
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_sync ON %I (user_id, updated_at)', r.table_name, r.table_name);
  END LOOP;
END $$;

-- 3) lock Supabase's public API: RLS on, NO policies (the backend's owner role still sees everything)
DO $$ DECLARE r record; BEGIN
  FOR r IN SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename <> 'flyway_schema_history'
  LOOP
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', r.tablename);
  END LOOP;
END $$;
