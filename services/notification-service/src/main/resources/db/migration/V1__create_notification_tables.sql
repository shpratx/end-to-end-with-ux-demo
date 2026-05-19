CREATE TABLE notifications (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL,
  title         VARCHAR(255) NOT NULL,
  body          TEXT NOT NULL,
  type          VARCHAR(20) NOT NULL CHECK (type IN ('transactional','promotional','system')),
  channel       VARCHAR(20) NOT NULL CHECK (channel IN ('push','email','in_app')),
  read_at       TIMESTAMPTZ,
  delivered_at  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX idx_notifications_unread ON notifications(customer_id, read_at) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_created_at ON notifications(customer_id, created_at DESC);

CREATE TABLE push_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL,
  platform      VARCHAR(10) NOT NULL CHECK (platform IN ('ios','android','web')),
  token         VARCHAR(500) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_push_tokens_customer_id ON push_tokens(customer_id) WHERE active = TRUE;
CREATE UNIQUE INDEX idx_push_tokens_token ON push_tokens(token);

CREATE TABLE notification_templates (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type      VARCHAR(100) NOT NULL UNIQUE,
  title_template  VARCHAR(255) NOT NULL,
  body_template   TEXT NOT NULL,
  channels        VARCHAR(100) NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

-- Seed templates
INSERT INTO notification_templates (event_type, title_template, body_template, channels) VALUES
('customer.registered', 'Welcome to Dunelm Loyalty!', 'Hi! Welcome to the Dunelm Loyalty Program. Start earning points on every purchase.', 'email,in_app'),
('points.earned', 'Points Earned!', 'You earned {points} points! New balance: {balance} points.', 'push,in_app'),
('points.redeemed', 'Points Redeemed', 'You redeemed {points} points for £{discount} off your order.', 'push,in_app'),
('tier.upgraded', 'Tier Upgrade!', 'Congratulations! You''ve been upgraded to {tier} tier.', 'push,email,in_app'),
('password.reset', 'Password Reset', 'Your password has been reset successfully.', 'email');
