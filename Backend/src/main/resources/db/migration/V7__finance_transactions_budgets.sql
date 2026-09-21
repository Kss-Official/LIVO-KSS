-- V7__finance_transactions_budgets.sql
CREATE TABLE transactions (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL,
    goal_id          UUID          NULL,
    trip_id          UUID          NULL,
    type             VARCHAR(10)   NOT NULL DEFAULT 'EXPENSE',
    amount           NUMERIC(12,2) NOT NULL,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'INR',
    title            VARCHAR(150)  NOT NULL,
    description      VARCHAR(500)  NULL,
    category         VARCHAR(50)   NOT NULL,
    payment_method   VARCHAR(20)   NOT NULL DEFAULT 'UPI',
    transaction_date DATE          NOT NULL DEFAULT CURRENT_DATE,
    transaction_time TIME          NULL,
    version          BIGINT        NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ   NULL,

    CONSTRAINT transactions_pk        PRIMARY KEY (id),
    CONSTRAINT transactions_user_fk   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT transactions_goal_fk   FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
    CONSTRAINT transactions_type_ck   CHECK (type IN ('EXPENSE', 'INCOME')),
    CONSTRAINT transactions_method_ck CHECK (payment_method IN ('CASH', 'UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'))
);

CREATE INDEX idx_transactions_user_date ON transactions (user_id, transaction_date) WHERE deleted_at IS NULL;
CREATE TRIGGER trg_transactions_updated_at BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE budgets (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                 UUID          NOT NULL,
    category                VARCHAR(50)   NOT NULL,
    monthly_limit           NUMERIC(12,2) NOT NULL,
    currency                VARCHAR(3)    NOT NULL DEFAULT 'INR',
    alert_threshold_percent SMALLINT      NOT NULL DEFAULT 80,
    month_year              VARCHAR(7)    NOT NULL,
    version                 BIGINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ   NULL,

    CONSTRAINT budgets_pk          PRIMARY KEY (id),
    CONSTRAINT budgets_user_fk     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT budgets_unique      UNIQUE (user_id, category, month_year),
    CONSTRAINT budgets_limit_ck    CHECK (monthly_limit > 0),
    CONSTRAINT budgets_thresh_ck   CHECK (alert_threshold_percent BETWEEN 1 AND 100)
);

CREATE TRIGGER trg_budgets_updated_at BEFORE UPDATE ON budgets FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
