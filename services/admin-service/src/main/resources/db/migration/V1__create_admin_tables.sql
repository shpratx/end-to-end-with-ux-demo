CREATE TABLE IF NOT EXISTS rates_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accrual_rate    DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    redemption_rate DOUBLE PRECISION NOT NULL DEFAULT 20.0,
    minimum_redemption INTEGER NOT NULL DEFAULT 100,
    max_discount_percentage DOUBLE PRECISION NOT NULL DEFAULT 50.0,
    expiration_months INTEGER NOT NULL DEFAULT 12,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO rates_config (accrual_rate, redemption_rate, minimum_redemption, max_discount_percentage, expiration_months)
VALUES (1.0, 20.0, 100, 50.0, 12);

CREATE TABLE IF NOT EXISTS campaigns (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(255) NOT NULL,
    type                  VARCHAR(20) NOT NULL CHECK (type IN ('multiplier','fixed_bonus','category_bonus')),
    value                 DOUBLE PRECISION NOT NULL,
    start_date            TIMESTAMPTZ NOT NULL,
    end_date              TIMESTAMPTZ NOT NULL,
    eligibility           VARCHAR(30) NOT NULL CHECK (eligibility IN ('all_customers','tier_minimum','new_members_only')),
    minimum_tier          VARCHAR(50),
    applicable_categories TEXT[],
    max_budget            INTEGER,
    budget_used           INTEGER NOT NULL DEFAULT 0,
    status                VARCHAR(20) NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','paused','completed')),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_type      VARCHAR(20) NOT NULL CHECK (actor_type IN ('customer','staff','system')),
    actor_id        UUID NOT NULL,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID NOT NULL,
    before_state    JSONB,
    after_state     JSONB,
    ip_address      VARCHAR(45),
    correlation_id  UUID NOT NULL,
    source_system   VARCHAR(20) DEFAULT 'admin',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_type, actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
