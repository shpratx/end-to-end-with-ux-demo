-- V1__create_loyalty_tables.sql

CREATE TABLE tiers (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(50) NOT NULL UNIQUE,
  threshold       INTEGER NOT NULL,
  multiplier      DECIMAL(3,2) NOT NULL DEFAULT 1.00,
  badge_color     VARCHAR(7),
  benefits        JSONB NOT NULL DEFAULT '[]',
  sort_order      INTEGER NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tiers_threshold ON tiers(threshold);

CREATE TABLE campaigns (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                  VARCHAR(255) NOT NULL,
  type                  VARCHAR(20) NOT NULL CHECK (type IN ('MULTIPLIER','FIXED_BONUS','CATEGORY')),
  value                 DECIMAL(10,2) NOT NULL,
  start_date            TIMESTAMPTZ NOT NULL,
  end_date              TIMESTAMPTZ NOT NULL,
  eligibility_rules     VARCHAR(30),
  max_budget            INTEGER,
  budget_used           INTEGER NOT NULL DEFAULT 0,
  status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','ACTIVE','PAUSED','COMPLETED')),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_dates ON campaigns(start_date, end_date) WHERE status = 'ACTIVE';

CREATE TABLE points_ledger (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id     UUID NOT NULL,
  type            VARCHAR(20) NOT NULL CHECK (type IN ('EARN','REDEEM','ADJUST','BONUS','EXPIRE','REVERSE')),
  points          INTEGER NOT NULL,
  running_balance INTEGER NOT NULL,
  reference_id    VARCHAR(255),
  channel         VARCHAR(20) NOT NULL CHECK (channel IN ('online','in_store','app','system')),
  campaign_id     UUID REFERENCES campaigns(id),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_points_ledger_customer_id ON points_ledger(customer_id);
CREATE INDEX idx_points_ledger_customer_created ON points_ledger(customer_id, created_at DESC);
CREATE INDEX idx_points_ledger_reference ON points_ledger(reference_id);
CREATE INDEX idx_points_ledger_campaign ON points_ledger(campaign_id);

CREATE TABLE badges (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(100) NOT NULL UNIQUE,
  description   TEXT,
  icon_url      VARCHAR(500),
  rarity        VARCHAR(20) NOT NULL CHECK (rarity IN ('common','rare','epic')),
  criteria      JSONB NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE customer_badges (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL,
  badge_id      UUID NOT NULL REFERENCES badges(id),
  earned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_customer_badges_unique ON customer_badges(customer_id, badge_id);
CREATE INDEX idx_customer_badges_customer ON customer_badges(customer_id);

CREATE TABLE adjustments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id     UUID NOT NULL,
  action          VARCHAR(10) NOT NULL CHECK (action IN ('ADD','DEDUCT')),
  points          INTEGER NOT NULL CHECK (points > 0),
  reason          VARCHAR(50) NOT NULL,
  notes           VARCHAR(500),
  status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  requested_by    UUID NOT NULL,
  approved_by     UUID,
  review_notes    VARCHAR(500),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_adjustments_customer ON adjustments(customer_id);
CREATE INDEX idx_adjustments_status ON adjustments(status) WHERE status = 'PENDING';

-- Seed tier data
INSERT INTO tiers (name, threshold, multiplier, badge_color, benefits, sort_order) VALUES
  ('Bronze', 0, 1.00, '#CD7F32', '["1x points on all purchases"]', 1),
  ('Silver', 2000, 1.25, '#C0C0C0', '["1.25x points on all purchases", "Free delivery on orders over £50"]', 2),
  ('Gold', 5000, 1.50, '#FFD700', '["1.5x points on all purchases", "Early access to sales (24 hours)", "Free delivery on all orders"]', 3),
  ('Platinum', 10000, 2.00, '#E5E4E2', '["2x points on all purchases", "Early access to sales (48 hours)", "Free delivery", "Exclusive events"]', 4);
