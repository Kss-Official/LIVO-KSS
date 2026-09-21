-- V9__trips_and_itinerary.sql
CREATE TABLE trips (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID          NOT NULL,
    goal_id             UUID          NULL,
    title               VARCHAR(150)  NOT NULL,
    destination         VARCHAR(150)  NOT NULL,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    travel_mode         VARCHAR(20)   NOT NULL DEFAULT 'FLIGHT',
    accommodation_type  VARCHAR(20)   NOT NULL DEFAULT 'HOTEL',
    accommodation_notes VARCHAR(500)  NULL,
    travel_with         VARCHAR(20)   NOT NULL DEFAULT 'SOLO',
    budget_amount       NUMERIC(12,2) NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'INR',
    notes               VARCHAR(1000) NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PLANNING',
    version             BIGINT        NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ   NULL,

    CONSTRAINT trips_pk           PRIMARY KEY (id),
    CONSTRAINT trips_user_fk      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT trips_goal_fk      FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
    CONSTRAINT trips_date_order   CHECK (end_date >= start_date),
    CONSTRAINT trips_mode_ck      CHECK (travel_mode IN ('FLIGHT', 'TRAIN', 'BUS', 'CAR', 'OTHER')),
    CONSTRAINT trips_accom_ck     CHECK (accommodation_type IN ('HOTEL', 'AIRBNB', 'HOSTEL', 'RESORT', 'OTHER')),
    CONSTRAINT trips_with_ck      CHECK (travel_with IN ('SOLO', 'PARTNER', 'FAMILY', 'FRIENDS', 'BUSINESS', 'OTHER')),
    CONSTRAINT trips_status_ck    CHECK (status IN ('PLANNING', 'UPCOMING', 'ONGOING', 'COMPLETED'))
);

CREATE TRIGGER trg_trips_updated_at BEFORE UPDATE ON trips FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Foreign keys referencing trips
ALTER TABLE tasks ADD CONSTRAINT tasks_trip_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL;
ALTER TABLE events ADD CONSTRAINT events_trip_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL;
ALTER TABLE transactions ADD CONSTRAINT transactions_trip_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL;

CREATE TABLE itinerary_items (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    trip_id     UUID         NOT NULL,
    user_id     UUID         NOT NULL,
    day_number  INT          NOT NULL,
    item_date   DATE         NOT NULL,
    time_string VARCHAR(20)  NULL,
    title       VARCHAR(150) NOT NULL,
    location    VARCHAR(200) NULL,
    notes       VARCHAR(500) NULL,
    version     BIGINT       NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  NULL,

    CONSTRAINT itinerary_items_pk      PRIMARY KEY (id),
    CONSTRAINT itinerary_items_trip_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT itinerary_items_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_itinerary_items_updated_at BEFORE UPDATE ON itinerary_items FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
