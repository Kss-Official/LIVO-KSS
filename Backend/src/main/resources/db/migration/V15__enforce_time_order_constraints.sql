-- V15__enforce_time_order_constraints.sql

-- 1) Enforce strict time ordering on routines (end_time > start_time)
ALTER TABLE routines DROP CONSTRAINT IF EXISTS routines_time_order;
ALTER TABLE routines ADD CONSTRAINT routines_time_order CHECK (end_time > start_time);

-- 2) Enforce strict time ordering on schedule_blocks (end_time > start_time)
ALTER TABLE schedule_blocks DROP CONSTRAINT IF EXISTS schedule_blocks_time_order;
ALTER TABLE schedule_blocks ADD CONSTRAINT schedule_blocks_time_order CHECK (end_time > start_time);
