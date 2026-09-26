-- V9__trips_and_itinerary.sql
CREATE TABLE trips (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID          NOT NULL,
    goal_id             UUID          NULL,
    title               VARCHAR(150)  NOT NULL,
    destination         VARCHAR(150)  NOT NULL,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    trip_type           VARCHAR(20)   NOT NULL DEFAULT 'LEISURE',
    travel_mode         VARCHAR(20)   NOT NULL DEFAULT 'FLIGHT',
    accommodation_type  VARCHAR(20)   NOT NULL DEFAULT 'HOTEL',
    accommodation_notes VARCHAR(500)  NULL,
    travel_with         VARCHAR(20)   NOT NULL DEFAULT 'SOLO',
    budget_amount       NUMERIC(12,2) NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'INR',
    notes               VARCHAR(1000) NULL,
    version             BIGINT        NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ   NULL,

    CONSTRAINT trips_pk          PRIMARY KEY (id),
    CONSTRAINT trips_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT trips_id_user_uq  UNIQUE (id, user_id),
    CONSTRAINT trips_goal_fk     FOREIGN KEY (goal_id, user_id) REFERENCES goals(id, user_id) ON DELETE SET NULL (goal_id),
    CONSTRAINT trips_date_order  CHECK (end_date >= start_date),
    CONSTRAINT trips_type_ck     CHECK (trip_type IN ('LEISURE', 'BUSINESS', 'ADVENTURE', 'OTHER')),
    CONSTRAINT trips_mode_ck     CHECK (travel_mode IN ('FLIGHT', 'TRAIN', 'BUS', 'CAR', 'OTHER')),
    CONSTRAINT trips_accom_ck    CHECK (accommodation_type IN ('HOTEL', 'AIRBNB', 'HOSTEL', 'RESORT', 'OTHER')),
    CONSTRAINT trips_with_ck     CHECK (travel_with IN ('SOLO', 'PARTNER', 'FAMILY', 'FRIENDS', 'BUSINESS', 'OTHER'))
);

CREATE INDEX idx_trips_goal ON trips (goal_id) WHERE goal_id IS NOT NULL;
CREATE TRIGGER trg_trips_updated_at BEFORE UPDATE ON trips FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Foreign keys referencing trips (same-user composite FK)
ALTER TABLE tasks ADD CONSTRAINT tasks_trip_fk
  FOREIGN KEY (trip_id, user_id) REFERENCES trips(id, user_id) ON DELETE SET NULL (trip_id);

ALTER TABLE events ADD CONSTRAINT events_trip_fk
  FOREIGN KEY (trip_id, user_id) REFERENCES trips(id, user_id) ON DELETE SET NULL (trip_id);

ALTER TABLE transactions ADD CONSTRAINT transactions_trip_fk
  FOREIGN KEY (trip_id, user_id) REFERENCES trips(id, user_id) ON DELETE SET NULL (trip_id);

CREATE TABLE itinerary_items (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    trip_id    UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    item_date  DATE         NOT NULL,
    item_time  TIME         NULL,
    title      VARCHAR(150) NOT NULL,
    location   VARCHAR(200) NULL,
    notes      VARCHAR(500) NULL,
    version    BIGINT       NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ  NULL,

    CONSTRAINT itinerary_items_pk      PRIMARY KEY (id),
    CONSTRAINT itinerary_items_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT itinerary_items_trip_fk FOREIGN KEY (trip_id, user_id) REFERENCES trips(id, user_id) ON DELETE CASCADE
);

CREATE INDEX idx_itinerary_items_trip ON itinerary_items (trip_id);
CREATE TRIGGER trg_itinerary_items_updated_at BEFORE UPDATE ON itinerary_items FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
