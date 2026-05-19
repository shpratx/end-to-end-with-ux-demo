-- V1: Auth service tables for Dunelm Loyalty Program

CREATE TABLE customers (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email                 VARCHAR(255) NOT NULL,
  phone                 VARCHAR(255),
  name                  VARCHAR(255),
  password_hash         VARCHAR(255) NOT NULL,
  status                VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
  loyalty_id            VARCHAR(10) NOT NULL UNIQUE,
  email_verified        BOOLEAN NOT NULL DEFAULT FALSE,
  phone_verified        BOOLEAN NOT NULL DEFAULT FALSE,
  failed_login_attempts INTEGER NOT NULL DEFAULT 0,
  locked_until          TIMESTAMPTZ,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted            BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_customers_email ON customers(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_customers_status ON customers(status);

CREATE TABLE otp_codes (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  code_hash     VARCHAR(255) NOT NULL,
  purpose       VARCHAR(30) NOT NULL,
  expires_at    TIMESTAMPTZ NOT NULL,
  used_at       TIMESTAMPTZ,
  attempts      INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_otp_codes_customer_id ON otp_codes(customer_id);

CREATE TABLE refresh_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  token_hash    VARCHAR(255) NOT NULL,
  device_info   VARCHAR(255),
  expires_at    TIMESTAMPTZ NOT NULL,
  revoked_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_customer_id ON refresh_tokens(customer_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

CREATE TABLE social_accounts (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id       UUID NOT NULL REFERENCES customers(id),
  provider          VARCHAR(20) NOT NULL,
  provider_user_id  VARCHAR(255) NOT NULL,
  email             VARCHAR(255),
  created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_social_accounts_provider_uid ON social_accounts(provider, provider_user_id);

CREATE TABLE consents (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  type          VARCHAR(30) NOT NULL,
  version       VARCHAR(20) NOT NULL,
  accepted      BOOLEAN NOT NULL,
  ip_address    VARCHAR(45),
  accepted_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_consents_customer_id ON consents(customer_id);

CREATE TABLE audit_logs (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_type      VARCHAR(20) NOT NULL,
  actor_id        UUID NOT NULL,
  action          VARCHAR(100) NOT NULL,
  entity_type     VARCHAR(50) NOT NULL,
  entity_id       UUID NOT NULL,
  before_state    JSONB,
  after_state     JSONB,
  ip_address      VARCHAR(45),
  correlation_id  UUID NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);
