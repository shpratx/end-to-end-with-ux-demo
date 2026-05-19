# Low-Level Design — Next Loyalty Program

**Version:** 1.0.0  
**Tech Stack:** TypeScript, NestJS, PostgreSQL (TypeORM), Redis, Kafka (KafkaJS), bcrypt, jsonwebtoken  
**Mode:** Greenfield  

---

## 1. Domain Model

```
┌─────────────────┐       ┌─────────────────────┐
│      Tier       │       │      Campaign       │
│─────────────────│       │─────────────────────│
│ id: UUID        │       │ id: UUID            │
│ name: string    │       │ name: string        │
│ threshold: int  │       │ type: enum          │
│ multiplier: dec │       │ status: enum        │
│ badge_color     │       │ start_date          │
│ benefits: json  │       │ end_date            │
└────────┬────────┘       │ max_budget: int     │
         │ 1              └──────────┬──────────┘
         │                           │ 1
         │ many                      │ many
         ▼                           ▼
┌─────────────────────────────────────────────────────────┐
│                       Customer                          │
│─────────────────────────────────────────────────────────│
│ id: UUID  │ email: string  │ phone: string              │
│ name: string │ password_hash │ status: enum              │
│ tier_id: FK  │ loyalty_id: string │ lifetime_points: int │
└──┬──────┬──────┬──────┬──────┬──────┬───────────────────┘
   │      │      │      │      │      │
   │1:N   │1:1   │1:N   │1:N   │1:N   │1:N
   ▼      ▼      ▼      ▼      ▼      ▼
┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────────────┐
│Points││Loyal-││Notif-││Refre-││Conse-││SocialAccount │
│Ledger││ty    ││icati-││sh    ││nt    ││──────────────│
│Entry ││Acct  ││on    ││Token ││      ││provider      │
│──────││──────││──────││──────││──────││provider_id   │
│type  ││avail ││title ││token ││type  ││email         │
│points││pendi-││body  ││hash  ││versi-│└──────────────┘
│ref_id││ng    ││type  ││devic-││on    │
│channe││tier_ ││chann-││e_inf-││accep-│  ┌──────────┐
│l     ││multi-││el    ││o     ││ted   │  │  Badge   │
│camp_ ││plier ││read_ ││expir-││ip    │  │──────────│
│id(FK)││      ││at    ││es_at ││      │  │name      │
└──────┘└──────┘└──────┘└──────┘└──────┘  │icon_url  │
                                           │rarity    │
                                           └──────────┘
```

**Relationships:**
- `Customer (1) → (N) PointsLedgerEntry` — via customer_id FK
- `Customer (1) → (1) LoyaltyAccount` — embedded in customers table (available_points, pending_points)
- `Customer (1) → (N) Notification` — via customer_id FK
- `Customer (1) → (N) RefreshToken` — via customer_id FK
- `Customer (1) → (N) Consent` — via customer_id FK
- `Customer (1) → (N) SocialAccount` — via customer_id FK
- `Customer (1) → (N) Badge` — via customer_badges join table
- `Tier (1) → (N) Customer` — via tier_id FK on customers
- `Campaign (1) → (N) PointsLedgerEntry` — via campaign_id FK

---

## 2. Database Schemas

### 2.1 customers

```sql
CREATE TABLE customers (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email           VARCHAR(255) NOT NULL,          -- [ENCRYPTED] AES-256
  phone           VARCHAR(20),                    -- [ENCRYPTED] AES-256
  name            VARCHAR(255),                   -- [ENCRYPTED] AES-256
  password_hash   VARCHAR(255) NOT NULL,          -- bcrypt (work factor 12)
  status          VARCHAR(30) NOT NULL DEFAULT 'pending_verification'
                    CHECK (status IN ('pending_verification','active','suspended','deleted')),
  tier_id         UUID REFERENCES tiers(id),
  loyalty_id      VARCHAR(10) NOT NULL UNIQUE,
  lifetime_points INTEGER NOT NULL DEFAULT 0,
  available_points INTEGER NOT NULL DEFAULT 0,
  pending_points  INTEGER NOT NULL DEFAULT 0,
  email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
  phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
  failed_login_attempts INTEGER NOT NULL DEFAULT 0,
  locked_until    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_customers_email ON customers(email) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX idx_customers_loyalty_id ON customers(loyalty_id);
CREATE INDEX idx_customers_tier_id ON customers(tier_id);
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_phone ON customers(phone) WHERE is_deleted = FALSE;
```

### 2.2 otp_codes

```sql
CREATE TABLE otp_codes (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  code_hash     VARCHAR(255) NOT NULL,            -- bcrypt hashed
  purpose       VARCHAR(30) NOT NULL
                  CHECK (purpose IN ('registration','password_reset','email_change','phone_change')),
  expires_at    TIMESTAMPTZ NOT NULL,
  used_at       TIMESTAMPTZ,
  attempts      INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_otp_codes_customer_id ON otp_codes(customer_id);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes(expires_at) WHERE used_at IS NULL;
```

### 2.3 refresh_tokens

```sql
CREATE TABLE refresh_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  token_hash    VARCHAR(255) NOT NULL,            -- SHA-256
  device_info   VARCHAR(255),
  expires_at    TIMESTAMPTZ NOT NULL,
  revoked_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_customer_id ON refresh_tokens(customer_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at) WHERE revoked_at IS NULL;
```

### 2.4 social_accounts

```sql
CREATE TABLE social_accounts (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id       UUID NOT NULL REFERENCES customers(id),
  provider          VARCHAR(20) NOT NULL CHECK (provider IN ('google','apple')),
  provider_user_id  VARCHAR(255) NOT NULL,
  email             VARCHAR(255),                 -- [ENCRYPTED] AES-256
  created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_social_accounts_provider_uid ON social_accounts(provider, provider_user_id);
CREATE INDEX idx_social_accounts_customer_id ON social_accounts(customer_id);
```

### 2.5 consents

```sql
CREATE TABLE consents (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  type          VARCHAR(30) NOT NULL
                  CHECK (type IN ('terms_and_conditions','marketing_email','marketing_push')),
  version       VARCHAR(20) NOT NULL,
  accepted      BOOLEAN NOT NULL,
  ip_address    VARCHAR(45),
  accepted_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_consents_customer_id ON consents(customer_id);
CREATE INDEX idx_consents_type_version ON consents(customer_id, type, version);
```

### 2.6 notifications

```sql
CREATE TABLE notifications (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
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
```

### 2.7 push_tokens

```sql
CREATE TABLE push_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  platform      VARCHAR(10) NOT NULL CHECK (platform IN ('ios','android','web')),
  token         VARCHAR(500) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_push_tokens_customer_id ON push_tokens(customer_id) WHERE active = TRUE;
CREATE UNIQUE INDEX idx_push_tokens_token ON push_tokens(token);
```

### 2.8 notification_templates

```sql
CREATE TABLE notification_templates (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type      VARCHAR(100) NOT NULL UNIQUE,
  title_template  VARCHAR(255) NOT NULL,
  body_template   TEXT NOT NULL,
  channels        TEXT[] NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_notification_templates_event ON notification_templates(event_type);
```

### 2.9 audit_logs

```sql
CREATE TABLE audit_logs (
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
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
  -- No updated_at, is_deleted — append-only table, no UPDATE/DELETE permitted
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_type, actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

### 2.10 points_ledger

```sql
CREATE TABLE points_ledger (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id     UUID NOT NULL REFERENCES customers(id),
  type            VARCHAR(20) NOT NULL
                    CHECK (type IN ('earn','redeem','adjustment','bonus','expired','reversal')),
  points          INTEGER NOT NULL,               -- positive=credit, negative=debit
  running_balance INTEGER NOT NULL,
  reference_id    VARCHAR(255),                   -- order ID, adjustment ID
  channel         VARCHAR(20) NOT NULL CHECK (channel IN ('online','in_store','app','system')),
  campaign_id     UUID REFERENCES campaigns(id),
  description     VARCHAR(500),
  status          VARCHAR(20) NOT NULL DEFAULT 'confirmed'
                    CHECK (status IN ('pending','confirmed','reversed','expired')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_points_ledger_customer_id ON points_ledger(customer_id);
CREATE INDEX idx_points_ledger_customer_created ON points_ledger(customer_id, created_at DESC);
CREATE INDEX idx_points_ledger_reference ON points_ledger(reference_id);
CREATE INDEX idx_points_ledger_campaign ON points_ledger(campaign_id);
CREATE INDEX idx_points_ledger_type ON points_ledger(customer_id, type);
CREATE INDEX idx_points_ledger_status ON points_ledger(status) WHERE status = 'pending';
```

### 2.11 tiers

```sql
CREATE TABLE tiers (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(50) NOT NULL UNIQUE,
  threshold       INTEGER NOT NULL,               -- lifetime points to reach
  multiplier      DECIMAL(3,2) NOT NULL DEFAULT 1.00,
  badge_color     VARCHAR(7),                     -- hex color
  benefits        JSONB NOT NULL DEFAULT '[]',
  sort_order      INTEGER NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tiers_threshold ON tiers(threshold);
```

### 2.12 campaigns

```sql
CREATE TABLE campaigns (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                  VARCHAR(255) NOT NULL,
  type                  VARCHAR(20) NOT NULL
                          CHECK (type IN ('multiplier','fixed_bonus','category_bonus')),
  value                 DECIMAL(10,2) NOT NULL,
  start_date            TIMESTAMPTZ NOT NULL,
  end_date              TIMESTAMPTZ NOT NULL,
  eligibility           VARCHAR(30) NOT NULL
                          CHECK (eligibility IN ('all_customers','tier_minimum','new_members_only')),
  minimum_tier          VARCHAR(50),
  applicable_categories TEXT[],
  max_budget            INTEGER,
  budget_used           INTEGER NOT NULL DEFAULT 0,
  status                VARCHAR(20) NOT NULL DEFAULT 'draft'
                          CHECK (status IN ('draft','active','paused','completed')),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted            BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_dates ON campaigns(start_date, end_date) WHERE status = 'active';
```

### 2.13 badges

```sql
CREATE TABLE badges (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(100) NOT NULL UNIQUE,
  description   TEXT,
  icon_url      VARCHAR(500),
  rarity        VARCHAR(20) NOT NULL CHECK (rarity IN ('common','rare','epic')),
  criteria      JSONB NOT NULL,                   -- {type: 'points_earned', target: 1000}
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);
```

### 2.14 customer_badges

```sql
CREATE TABLE customer_badges (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES customers(id),
  badge_id      UUID NOT NULL REFERENCES badges(id),
  earned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_customer_badges_unique ON customer_badges(customer_id, badge_id);
CREATE INDEX idx_customer_badges_customer ON customer_badges(customer_id);
```

### 2.15 adjustments

```sql
CREATE TABLE adjustments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id     UUID NOT NULL REFERENCES customers(id),
  action          VARCHAR(10) NOT NULL CHECK (action IN ('add','deduct')),
  points          INTEGER NOT NULL CHECK (points > 0),
  reason          VARCHAR(50) NOT NULL
                    CHECK (reason IN ('system_error_correction','goodwill_gesture',
                                      'promotional_credit','fraud_correction','other')),
  notes           VARCHAR(500),
  status          VARCHAR(20) NOT NULL DEFAULT 'pending_approval'
                    CHECK (status IN ('pending_approval','approved','rejected','applied')),
  requested_by    UUID NOT NULL,                  -- staff user ID
  approved_by     UUID,                           -- manager user ID
  review_notes    VARCHAR(500),
  ledger_entry_id UUID REFERENCES points_ledger(id),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_adjustments_customer ON adjustments(customer_id);
CREATE INDEX idx_adjustments_status ON adjustments(status) WHERE status = 'pending_approval';
CREATE INDEX idx_adjustments_requested_by ON adjustments(requested_by);
```

---

## 3. CQRS Handlers

### 3.1 Auth Handlers

#### RegisterCustomerCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ name: string (1-255), email: string (valid email), phone: string (E.164), password: string (min 12), termsAndConditionsVersion: string }` |
| **Business Logic** | 1. Check rate limit (5 reg/IP/hr) 2. Validate email not already registered 3. Hash password (bcrypt, factor 12) 4. Generate unique loyalty_id (10-digit) 5. Insert customer with status=pending_verification 6. Generate 6-digit OTP, hash with bcrypt, store with 5min expiry 7. Record T&C consent 8. Emit `customer.registered` event 9. Send OTP email via notification event |
| **Output DTO** | `{ customerId: UUID, status: 'pending_verification', message: string }` |
| **Events** | `customer.registered` → notification-commands topic |
| **Errors** | `EMAIL_ALREADY_EXISTS` (422), `RATE_LIMIT_EXCEEDED` (429), `VALIDATION_ERROR` (422) |

#### VerifyOtpCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ customerId: UUID, otpCode: string (6 digits) }` |
| **Business Logic** | 1. Find latest unused OTP for customer 2. Check not expired (5min) 3. Check attempts < 5 (else lockout 30min) 4. Increment attempts 5. Compare bcrypt hash 6. If valid: mark OTP used, set customer status=active, set email_verified=true 7. Issue JWT access token (15min) + refresh token (30d) 8. Emit `customer.verified` event |
| **Output DTO** | `{ verified: boolean, accessToken: string, refreshToken: string, expiresIn: 900 }` |
| **Events** | `customer.verified` |
| **Errors** | `OTP_EXPIRED` (422), `OTP_INVALID` (422), `ACCOUNT_LOCKED` (403), `CUSTOMER_NOT_FOUND` (404) |

#### LoginCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ email: string, password: string }` |
| **Business Logic** | 1. Find customer by email (active only) 2. Check not locked (failed_login_attempts >= 5 → locked_until) 3. Compare bcrypt password hash 4. If invalid: increment failed_login_attempts, lock if threshold reached 5. If valid: reset failed_login_attempts, issue JWT + refresh token 6. Store refresh token hash in DB 7. Emit `customer.login` event |
| **Output DTO** | `{ accessToken: string, refreshToken: string, expiresIn: 900, tokenType: 'Bearer' }` |
| **Events** | `customer.login` |
| **Errors** | `INVALID_CREDENTIALS` (401), `ACCOUNT_LOCKED` (403), `ACCOUNT_NOT_VERIFIED` (403), `ACCOUNT_SUSPENDED` (403) |

#### SocialLoginCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ provider: 'google' | 'apple', idToken: string }` |
| **Business Logic** | 1. Verify idToken with provider (Google/Apple OIDC) 2. Extract email + provider_user_id 3. Find existing social_account by provider+provider_user_id 4. If exists: load customer, issue tokens 5. If not: check if email matches existing customer → link 6. If new: create customer (status=active, email_verified=true), create social_account 7. Issue JWT + refresh token |
| **Output DTO** | `{ accessToken: string, refreshToken: string, expiresIn: 900, tokenType: 'Bearer', isNewAccount: boolean }` |
| **Events** | `customer.registered` (if new), `customer.login` |
| **Errors** | `INVALID_SOCIAL_TOKEN` (401), `PROVIDER_UNAVAILABLE` (503), `EMAIL_CONFLICT` (422) |

#### RefreshTokenCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ refreshToken: string }` |
| **Business Logic** | 1. Hash incoming token (SHA-256) 2. Find matching refresh_token record 3. Validate not revoked, not expired 4. Revoke current token (token rotation) 5. Issue new access token + new refresh token 6. Store new refresh token hash |
| **Output DTO** | `{ accessToken: string, refreshToken: string, expiresIn: 900, tokenType: 'Bearer' }` |
| **Events** | None |
| **Errors** | `TOKEN_REVOKED` (401), `TOKEN_EXPIRED` (401), `TOKEN_NOT_FOUND` (401) |

#### LogoutCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ refreshToken?: string }` (from authenticated context) |
| **Business Logic** | 1. Revoke refresh token (set revoked_at) 2. If no specific token provided, revoke all tokens for customer |
| **Output DTO** | `204 No Content` |
| **Events** | None |
| **Errors** | `UNAUTHORIZED` (401) |

#### RequestPasswordResetCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ email: string }` |
| **Business Logic** | 1. Always return 200 (prevent email enumeration) 2. Find customer by email 3. If exists: generate reset token (UUID), hash, store as OTP with purpose=password_reset, 1hr expiry 4. Send reset email via notification event |
| **Output DTO** | `{ message: 'If an account exists with this email, a reset link has been sent' }` |
| **Events** | `notification.send` (password reset email) |
| **Errors** | None (always 200) |

#### ConfirmPasswordResetCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ token: string, newPassword: string (min 12) }` |
| **Business Logic** | 1. Find OTP by token hash with purpose=password_reset 2. Validate not expired, not used 3. Hash new password (bcrypt) 4. Update customer password_hash 5. Mark OTP as used 6. Revoke all refresh tokens for customer 7. Audit log the change |
| **Output DTO** | `{ success: boolean }` |
| **Events** | `notification.send` (password changed confirmation) |
| **Errors** | `TOKEN_EXPIRED` (422), `TOKEN_INVALID` (422), `WEAK_PASSWORD` (422) |

### 3.2 Loyalty Handlers

#### EarnPointsCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ customerId: UUID, transactionAmount: number (>0.01), referenceId: string, channel: 'online'|'in_store'|'app', items?: [{sku, category, amount}] }` |
| **Business Logic** | 1. Idempotency check on referenceId 2. Validate customer exists and is active 3. Get customer tier multiplier 4. Check active campaigns (eligibility, date range, budget) 5. Calculate points: floor(amount) * baseRate * tierMultiplier * campaignMultiplier 6. BEGIN TRANSACTION: insert points_ledger entry, update customer.available_points, update campaign.budget_used 7. Invalidate balance cache 8. Emit `points.earned` event |
| **Output DTO** | `{ transactionId: UUID, pointsEarned: int, bonusPoints: int, newBalance: int, campaignApplied: string|null }` |
| **Events** | `points.earned` → loyalty-events topic |
| **Errors** | `CUSTOMER_NOT_FOUND` (404), `CUSTOMER_NOT_ACTIVE` (403), `DUPLICATE_REFERENCE` (409), `INVALID_AMOUNT` (422) |

#### RedeemPointsCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ pointsToRedeem: int (>=1), orderId: string, orderTotal: number, channel: 'online'|'in_store'|'app' }` (customerId from JWT) |
| **Business Logic** | 1. Idempotency check on orderId 2. Validate customer active 3. SELECT FOR UPDATE customer row (row-level lock) 4. Check available_points >= pointsToRedeem 5. Check minimum redemption threshold (100 pts) 6. Calculate discount: pointsToRedeem / 100 * 5 (£5 per 100pts) 7. Validate discount <= orderTotal 8. Debit points, insert ledger entry with negative points 9. Invalidate balance cache 10. Emit `points.redeemed` event |
| **Output DTO** | `{ success: boolean, transactionId: UUID, pointsRedeemed: int, discountApplied: number, remainingBalance: int }` |
| **Events** | `points.redeemed` → loyalty-events topic |
| **Errors** | `INSUFFICIENT_BALANCE` (422), `BELOW_MINIMUM_REDEMPTION` (422), `DISCOUNT_EXCEEDS_ORDER` (422), `DUPLICATE_ORDER` (409) |

#### GetBalanceQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ customerId: UUID }` (from JWT) |
| **Business Logic** | 1. Check Redis cache `customer:{id}:balance` 2. If miss: query customer.available_points, pending_points 3. Get tier multiplier 4. Calculate monetary equivalent (available / 100 * 5) 5. Cache result (2s TTL) |
| **Output DTO** | `{ availablePoints: int, monetaryEquivalent: number, pendingPoints: int, tierMultiplier: number, lastUpdated: ISO8601 }` |
| **Events** | None |
| **Errors** | `CUSTOMER_NOT_FOUND` (404) |

#### GetTransactionsQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ customerId: UUID, pageNumber?: int (default 1), pageSize?: int (default 20, max 100), type?: string, fromDate?: date, toDate?: date }` |
| **Business Logic** | 1. Build query with filters (type, date range) 2. Query points_ledger WHERE customer_id = :id, ordered by created_at DESC 3. Apply pagination (OFFSET/LIMIT) 4. Count total for pagination meta |
| **Output DTO** | `{ data: Transaction[], meta: { pageNumber, pageSize, totalItems, totalPages, hasNextPage, hasPreviousPage } }` |
| **Events** | None |
| **Errors** | `INVALID_DATE_RANGE` (422) |

#### GetCustomerDashboardQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ customerId: UUID }` (from JWT) |
| **Business Logic** | 1. Parallel fetch: balance (cached), tier info, recent 5 transactions, active promotions 2. Calculate next tier progress (lifetime_points vs next tier threshold) 3. Aggregate into single response |
| **Output DTO** | `{ balance: PointsBalance, tier: Tier, nextTierProgress: { nextTierName, pointsRequired, pointsEarned, progressPercent }, recentTransactions: Transaction[5], activePromotions: [{id, name, description, endsAt}] }` |
| **Events** | None |
| **Errors** | `CUSTOMER_NOT_FOUND` (404) |

#### ProcessEcommerceWebhookCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ event: string, orderId: string, customerId: UUID, amount: number, items: [{sku, category, amount}], timestamp: ISO8601 }` + webhook signature header |
| **Business Logic** | 1. Verify webhook HMAC signature 2. Idempotency check on orderId 3. Route by event type: 'order.completed' → EarnPointsCommand, 'order.refunded' → create reversal entry 4. For refund: find original ledger entry, create reversal, debit points 5. Audit log webhook receipt |
| **Output DTO** | `{ acknowledged: boolean, transactionId?: UUID }` |
| **Events** | `points.earned` or `points.reversed` |
| **Errors** | `INVALID_SIGNATURE` (401), `UNKNOWN_EVENT_TYPE` (422), `ORDER_NOT_FOUND` (404 for refunds) |

### 3.3 Notification Handlers

#### SendNotificationCommand
| Field | Value |
|-------|-------|
| **Type** | Command (Kafka consumer) |
| **Input DTO** | `{ customerId: UUID, templateId: string (event_type), variables: Record<string, string>, channels?: string[] }` |
| **Business Logic** | 1. Load notification_template by event_type 2. Check template is active 3. Render title/body with variables (Handlebars) 4. For each channel: if push → get active push_tokens, send via FCM/APNs; if email → send via SendGrid/SES; if in_app → insert notification record 5. Update delivered_at on success 6. On failure: retry with exponential backoff (3 attempts), then DLQ |
| **Output DTO** | `{ notificationId: UUID, deliveredChannels: string[] }` |
| **Events** | `notification.delivered` or `notification.failed` |
| **Errors** | `TEMPLATE_NOT_FOUND` (internal), `CUSTOMER_NOT_FOUND` (internal), `DELIVERY_FAILED` (retried) |

#### GetNotificationsQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ customerId: UUID, pageNumber?: int, pageSize?: int (max 100) }` |
| **Business Logic** | 1. Query notifications WHERE customer_id = :id ORDER BY created_at DESC 2. Apply pagination |
| **Output DTO** | `{ data: Notification[], meta: PaginationMeta }` |
| **Events** | None |
| **Errors** | None |

#### MarkNotificationReadCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ notificationId: UUID, customerId: UUID }` |
| **Business Logic** | 1. Find notification by id WHERE customer_id matches 2. Set read_at = NOW() |
| **Output DTO** | `{ success: boolean }` |
| **Events** | None |
| **Errors** | `NOTIFICATION_NOT_FOUND` (404), `FORBIDDEN` (403 if not owner) |

#### GetUnreadCountQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ customerId: UUID }` |
| **Business Logic** | 1. COUNT notifications WHERE customer_id = :id AND read_at IS NULL |
| **Output DTO** | `{ unreadCount: int }` |
| **Events** | None |
| **Errors** | None |

### 3.4 Staff Handlers

#### StaffLoginCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ employeeId: string, password: string, terminalId?: string }` |
| **Business Logic** | 1. Validate employee credentials against staff store 2. Check role (loyalty_staff, loyalty_manager, loyalty_admin) 3. If manager/admin: flag requiresMfa=true 4. Issue short-lived token (4hr expiry) with role claim 5. Audit log login with terminal ID |
| **Output DTO** | `{ accessToken: string, expiresIn: 14400, role: string, requiresMfa: boolean }` |
| **Events** | None |
| **Errors** | `INVALID_CREDENTIALS` (401), `ACCOUNT_DISABLED` (403) |

#### CreateAdjustmentCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ customerId: UUID, action: 'add'|'deduct', points: int (>=1), reason: enum, notes?: string (max 500) }` |
| **Business Logic** | 1. Validate customer exists and is active 2. If action=deduct: check available_points >= points 3. If points <= 500: auto-approve, apply immediately (insert ledger entry, update balance) 4. If points > 500: set status=pending_approval 5. Audit log with staff actor 6. If applied: invalidate balance cache, emit event |
| **Output DTO** | `{ adjustmentId: UUID, status: 'approved'|'pending_approval', pointsAdjusted: int, newBalance: int|null }` |
| **Events** | `points.adjusted` (if auto-approved) |
| **Errors** | `CUSTOMER_NOT_FOUND` (404), `INSUFFICIENT_BALANCE` (422 for deduct), `NOTES_REQUIRED` (422 if reason=other and no notes) |

#### ApproveAdjustmentCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ adjustmentId: UUID, approved: boolean, reviewNotes?: string }` (manager context) |
| **Business Logic** | 1. Find adjustment with status=pending_approval 2. Validate approver has manager role 3. Validate approver != requester 4. If approved: apply points (ledger entry + balance update), set status=approved 5. If rejected: set status=rejected 6. Record approved_by, review_notes 7. Invalidate cache, emit event |
| **Output DTO** | `{ adjustmentId: UUID, status: 'approved'|'rejected', newBalance: int|null }` |
| **Events** | `points.adjusted` (if approved) |
| **Errors** | `ADJUSTMENT_NOT_FOUND` (404), `ALREADY_REVIEWED` (409), `SELF_APPROVAL` (403), `INSUFFICIENT_ROLE` (403) |

#### StaffCustomerSearchQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ q: string (min 2), field: 'phone'|'email'|'name'|'loyalty_id' }` |
| **Business Logic** | 1. Search customers by field (decrypt PII for comparison) 2. Mask PII in results (j***@example.com, +44***0123) 3. Return max 20 results |
| **Output DTO** | `{ data: [{ customerId, name, maskedEmail, maskedPhone, tier, pointsBalance, memberSince }], totalResults: int }` |
| **Events** | None |
| **Errors** | `QUERY_TOO_SHORT` (422) |

### 3.5 Admin Handlers

#### GetAdminDashboardQuery
| Field | Value |
|-------|-------|
| **Type** | Query |
| **Input DTO** | `{ dateRange?: { from: date, to: date } }` |
| **Business Logic** | 1. Aggregate: total members, new registrations (period), total points issued, total redeemed, active campaigns count 2. Tier distribution breakdown 3. Top channels by volume |
| **Output DTO** | `{ totalMembers: int, newRegistrations: int, pointsIssued: int, pointsRedeemed: int, activeCampaigns: int, tierDistribution: Record<string,int> }` |
| **Events** | None |
| **Errors** | `INSUFFICIENT_ROLE` (403) |

#### CreateCampaignCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ name: string, type: 'multiplier'|'fixed_bonus'|'category_bonus', value: number, startDate: ISO8601, endDate: ISO8601, eligibility: enum, minimumTier?: string, applicableCategories?: string[], maxBudget?: int }` |
| **Business Logic** | 1. Validate endDate > startDate 2. Validate no overlapping campaign of same type (if multiplier) 3. Create campaign with status=draft 4. Audit log |
| **Output DTO** | `{ campaignId: UUID, status: 'draft' }` |
| **Events** | None |
| **Errors** | `INVALID_DATE_RANGE` (422), `OVERLAPPING_CAMPAIGN` (409) |

#### UpdateRatesConfigCommand
| Field | Value |
|-------|-------|
| **Type** | Command |
| **Input DTO** | `{ earnRate?: number, redemptionRate?: number, minimumRedemption?: int }` |
| **Business Logic** | 1. Validate rates are positive 2. Update config store (DB or Redis) 3. Audit log before/after state 4. Invalidate rate cache |
| **Output DTO** | `{ earnRate: number, redemptionRate: number, minimumRedemption: int, updatedAt: ISO8601 }` |
| **Events** | None |
| **Errors** | `INVALID_RATE` (422), `INSUFFICIENT_ROLE` (403) |

---

## 4. State Machine Transitions

### 4.1 Customer States

| From | To | Trigger | Validations | Side Effects |
|------|----|---------|-------------|--------------|
| — | `pending_verification` | RegisterCustomerCommand | Email unique, valid input | OTP sent, consent recorded |
| `pending_verification` | `active` | VerifyOtpCommand | Valid OTP, not expired, attempts < 5 | Tokens issued, welcome notification |
| `active` | `suspended` | Admin action / fraud detection | Admin role required | All tokens revoked, notification sent |
| `suspended` | `active` | Admin reinstatement | Admin role required | Notification sent |
| `active` | `deleted` | DeleteAccountCommand | Customer confirms | PII anonymised, tokens revoked, points forfeited |
| `suspended` | `deleted` | Admin deletion | Admin role required | PII anonymised |

### 4.2 Points Transaction States

| From | To | Trigger | Validations | Side Effects |
|------|----|---------|-------------|--------------|
| — | `pending` | Webhook received (hold period) | Valid customer, valid amount | Balance not yet updated |
| — | `confirmed` | EarnPointsCommand (instant) | Valid customer, idempotency | Balance updated, notification |
| `pending` | `confirmed` | Hold period elapsed (cron) | No refund received | Balance updated |
| `confirmed` | `reversed` | Refund webhook / manual reversal | Original entry exists | Balance debited, notification |
| `pending` | `expired` | Hold period exceeded (14d) | — | Entry archived |

### 4.3 Adjustment States

| From | To | Trigger | Validations | Side Effects |
|------|----|---------|-------------|--------------|
| — | `approved` | CreateAdjustmentCommand (≤500 pts) | Staff role, customer active | Ledger entry created, balance updated |
| — | `pending_approval` | CreateAdjustmentCommand (>500 pts) | Staff role, customer active | Manager notified |
| `pending_approval` | `approved` | ApproveAdjustmentCommand (approved=true) | Manager role, not self-approval | Ledger entry created, balance updated |
| `pending_approval` | `rejected` | ApproveAdjustmentCommand (approved=false) | Manager role | Requester notified |
| `approved` | `applied` | Points credited/debited | — | Final state |

### 4.4 Campaign States

| From | To | Trigger | Validations | Side Effects |
|------|----|---------|-------------|--------------|
| — | `draft` | CreateCampaignCommand | Admin role, valid dates | — |
| `draft` | `active` | UpdateCampaignCommand (activate) | start_date reached or manual | Points calculations include campaign |
| `active` | `paused` | UpdateCampaignCommand (pause) | Admin role | Campaign excluded from calculations |
| `paused` | `active` | UpdateCampaignCommand (resume) | Admin role, end_date not passed | Campaign re-included |
| `active` | `completed` | end_date reached / budget exhausted | Automatic (cron) | Final state, summary generated |
| `draft` | `completed` | Admin cancellation | Admin role | — |

---

## 5. Migration Plan

### Sprint 1 — Foundation
```
Migration 001: CREATE TABLE customers
Migration 002: CREATE TABLE otp_codes
Migration 003: CREATE TABLE refresh_tokens
Migration 004: CREATE TABLE social_accounts
Migration 005: CREATE TABLE consents
Migration 006: CREATE TABLE notifications
Migration 007: CREATE TABLE push_tokens
Migration 008: CREATE TABLE notification_templates
Migration 009: CREATE TABLE audit_logs
Migration 010: SEED notification_templates (welcome, otp, password_reset)
```

### Sprint 2 — Points & Transactions
```
Migration 011: CREATE TABLE points_ledger
Migration 012: ALTER TABLE customers ADD COLUMN available_points INTEGER DEFAULT 0
Migration 013: ALTER TABLE customers ADD COLUMN pending_points INTEGER DEFAULT 0
Migration 014: ALTER TABLE customers ADD COLUMN lifetime_points INTEGER DEFAULT 0
Migration 015: CREATE INDEX idx_points_ledger_customer_created
Migration 016: SEED config table with earn_rate=1, redemption_rate=0.05
```

### Sprint 3 — Tiers, Campaigns, Staff
```
Migration 017: CREATE TABLE tiers
Migration 018: CREATE TABLE campaigns
Migration 019: CREATE TABLE adjustments
Migration 020: CREATE TABLE badges
Migration 021: CREATE TABLE customer_badges
Migration 022: ALTER TABLE customers ADD COLUMN tier_id UUID REFERENCES tiers(id)
Migration 023: SEED tiers (Bronze: 0, Silver: 2000, Gold: 5000, Platinum: 10000)
Migration 024: ALTER TABLE points_ledger ADD COLUMN campaign_id UUID REFERENCES campaigns(id)
```

### Sprint 4 — Account Linking & Gamification
```
Migration 025: CREATE TABLE account_links (id, primary_customer_id, secondary_customer_id, status, linked_at)
Migration 026: ALTER TABLE customers ADD COLUMN merged_into UUID REFERENCES customers(id)
Migration 027: ALTER TABLE points_ledger ADD COLUMN original_customer_id UUID (for merged history)
```

### Zero-Downtime Strategy
- **Additive only per release:** new columns are always `NULL` or have defaults; no column drops in same release as code change
- **Nullable new columns:** all new FKs (tier_id, campaign_id) added as nullable first, backfilled, then optionally constrained in next release
- **Backfill scripts:** separate migration for data backfill (e.g., calculating lifetime_points from ledger), run as background job
- **Blue-green deploys:** old code must work with new schema (forward-compatible migrations)
- **Rollback safety:** each migration has a corresponding DOWN script; tested in staging before prod

---

## 6. Caching Strategy

| Cache Key Pattern | TTL | Invalidation Trigger | Data |
|-------------------|-----|---------------------|------|
| `customer:{id}:balance` | 2s | EarnPointsCommand, RedeemPointsCommand, adjustment applied | `{ available, pending, monetary, multiplier }` |
| `customer:{id}:tier` | 1hr | Tier recalculation (lifetime_points crosses threshold) | `{ tierId, name, multiplier, badgeColor }` |
| `customer:{id}:dashboard` | 30s | Any points mutation | Aggregated dashboard payload |
| `rate_limit:{ip}:{endpoint}` | Sliding window (1min) | Auto-expire | Counter (int) |
| `otp_attempts:{customer_id}` | 10min | Successful verification | Attempt counter |
| `login_attempts:{email}` | 30min | Successful login | Failed attempt counter |
| `campaign:active` | 5min | CreateCampaignCommand, UpdateCampaignCommand | List of active campaigns |
| `config:rates` | 10min | UpdateRatesConfigCommand | `{ earnRate, redemptionRate, minRedemption }` |
| `idempotency:{key}` | 24hr | Auto-expire | Response payload (for replay) |

**Implementation Notes:**
- Redis cluster with 3 nodes, automatic failover
- Cache-aside pattern: read from cache → miss → read DB → write cache
- Write-through invalidation: on mutation, DELETE cache key (not update)
- Fail-open for reads (serve stale or bypass), fail-closed for rate limiting

---

## 7. Error Handling

All errors follow RFC 7807 Problem Details format (`application/problem+json`).

| Error Code | HTTP Status | Message | When |
|------------|-------------|---------|------|
| `VALIDATION_ERROR` | 422 | One or more fields failed validation | Input DTO validation failure |
| `EMAIL_ALREADY_EXISTS` | 422 | Email address is already registered | Registration with existing email |
| `INVALID_CREDENTIALS` | 401 | Email or password is incorrect | Login with wrong credentials |
| `ACCOUNT_LOCKED` | 403 | Account temporarily locked due to failed attempts | 5+ failed login/OTP attempts |
| `ACCOUNT_NOT_VERIFIED` | 403 | Account pending email verification | Login before OTP verification |
| `ACCOUNT_SUSPENDED` | 403 | Account has been suspended | Any action on suspended account |
| `OTP_EXPIRED` | 422 | Verification code has expired | OTP older than 5 minutes |
| `OTP_INVALID` | 422 | Verification code is incorrect | Wrong OTP code |
| `TOKEN_EXPIRED` | 401 | Token has expired | Expired JWT or refresh token |
| `TOKEN_REVOKED` | 401 | Token has been revoked | Using revoked refresh token |
| `INSUFFICIENT_BALANCE` | 422 | Insufficient points balance | Redeem/deduct more than available |
| `BELOW_MINIMUM_REDEMPTION` | 422 | Minimum redemption is 100 points | Redeem < 100 points |
| `DISCOUNT_EXCEEDS_ORDER` | 422 | Discount cannot exceed order total | Points value > order amount |
| `DUPLICATE_REFERENCE` | 409 | Transaction already processed | Duplicate referenceId/orderId |
| `CUSTOMER_NOT_FOUND` | 404 | Customer not found | Invalid customer UUID |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests, try again later | Rate limit threshold hit |
| `INVALID_SIGNATURE` | 401 | Webhook signature verification failed | Tampered/invalid webhook |
| `INSUFFICIENT_ROLE` | 403 | Insufficient permissions for this action | Staff accessing admin endpoint |
| `SELF_APPROVAL` | 403 | Cannot approve own adjustment | Manager approving own request |
| `ADJUSTMENT_NOT_FOUND` | 404 | Adjustment not found | Invalid adjustment UUID |
| `ALREADY_REVIEWED` | 409 | Adjustment already reviewed | Re-approving/rejecting |
| `PROVIDER_UNAVAILABLE` | 503 | Social login provider unavailable | Google/Apple OIDC down |
| `CAMPAIGN_BUDGET_EXHAUSTED` | 422 | Campaign budget has been exhausted | Campaign max_budget reached |
| `OVERLAPPING_CAMPAIGN` | 409 | Overlapping campaign of same type exists | Date conflict on multiplier |
| `INTERNAL_ERROR` | 500 | An unexpected error occurred | Unhandled exception |

**Error Response Structure:**
```json
{
  "type": "https://loyalty.next.co.uk/problems/{error-code}",
  "title": "Human Readable Title",
  "status": 422,
  "detail": "Specific explanation for this occurrence",
  "instance": "/api/v1/points/redeem",
  "traceId": "correlation-uuid",
  "errors": [{ "field": "pointsToRedeem", "message": "Exceeds available balance", "code": "INSUFFICIENT_BALANCE" }]
}
```

---

## 8. Additional Handlers (Sprint 2-4)

The following handlers complete the API coverage. They follow the same CQRS patterns as Section 3.

### Auth Service (Additional)

### ResendOtpCommand
- **Type:** Command
- **Input:** `{ customerId: UUID }`
- **Validation:** Customer exists, status = pending_verification, rate limit (3/10min) not exceeded
- **Logic:** Generate new 6-digit OTP → hash → store in otp_codes → invalidate previous → send via email
- **Output:** `{ sent: true, nextResendAt: ISO8601 }`
- **Events:** notification.send (OTP email)
- **Errors:** RATE_LIMITED (429), CUSTOMER_NOT_FOUND (404), ALREADY_VERIFIED (422)

### LogoutCommand
- **Type:** Command
- **Input:** `{ refreshToken: string }`
- **Logic:** Find refresh token by hash → mark revoked_at = now → delete push token for device
- **Output:** `{ success: true }`
- **Errors:** INVALID_TOKEN (401)

### Customer Service

### GetProfileQuery
- **Type:** Query
- **Input:** `{ customerId: UUID }` (from JWT)
- **Logic:** SELECT customer by id → decrypt PII fields → include tier info → return
- **Output:** `{ name, email, phone, memberSince, loyaltyId, tier, lifetimePoints }`
- **Errors:** CUSTOMER_NOT_FOUND (404)

### UpdateProfileCommand
- **Type:** Command
- **Input:** `{ customerId: UUID, name?: string, email?: string, phone?: string }`
- **Validation:** If email/phone changed → require OTP re-verification (return pendingVerification flag)
- **Logic:** Update allowed fields → encrypt PII → audit log → if email/phone changed, trigger OTP
- **Output:** `{ updated: true, pendingVerification: boolean }`
- **Events:** customer.profile_updated
- **Errors:** VALIDATION_ERROR (422), EMAIL_TAKEN (409)

### DeleteAccountCommand
- **Type:** Command
- **Input:** `{ customerId: UUID, confirmation: "DELETE" }`
- **Validation:** confirmation must equal "DELETE"
- **Logic:** Soft-delete (is_deleted=true) → anonymise PII (hash email/phone) → revoke all tokens → forfeit points → audit log
- **Output:** `{ deleted: true }`
- **Events:** customer.deleted
- **Errors:** INVALID_CONFIRMATION (422)

### GetPreferencesQuery
- **Type:** Query
- **Input:** `{ customerId: UUID }`
- **Logic:** SELECT consents WHERE customer_id AND type IN (marketing_email, marketing_push, transactional)
- **Output:** `{ transactional: true, promotional: boolean, system: boolean, emailCopies: boolean }`

### UpdatePreferencesCommand
- **Type:** Command
- **Input:** `{ customerId: UUID, promotional?: boolean, system?: boolean, emailCopies?: boolean }`
- **Logic:** Upsert consent records → audit log (preference change)
- **Output:** `{ updated: true }`
- **Events:** customer.preferences_updated

### GetCustomerQrCodeQuery
- **Type:** Query
- **Input:** `{ customerId: UUID }`
- **Logic:** Encrypt(customerId + timestamp + nonce) with rotating key → base64 encode → set 60s expiry
- **Output:** `{ qrPayload: string, expiresAt: ISO8601, refreshInSeconds: 60 }`

### IdentifyCustomerQuery
- **Type:** Query (POS API key auth)
- **Input:** `{ method: "phone"|"loyalty_id"|"qr", value: string }`
- **Logic:** If qr → decrypt + validate expiry. If phone/loyalty_id → lookup customer. Return summary.
- **Output:** `{ customerId, name, tier, pointsBalance, memberSince }`
- **Errors:** CUSTOMER_NOT_FOUND (404), QR_EXPIRED (422)

### GetCustomerBadgesQuery
- **Type:** Query
- **Input:** `{ customerId: UUID }`
- **Logic:** LEFT JOIN badges with customer_badges → calculate progress for unearned badges
- **Output:** `[{ id, name, iconUrl, rarity, earned, earnedAt, progress: { current, target, percent } }]`

### InitiateAccountLinkingCommand
- **Type:** Command
- **Input:** `{ customerId: UUID, targetIdentifier: string, identifierType: "email"|"phone" }`
- **Validation:** Target account exists, not already linked, not same account
- **Logic:** Create link_request record → send OTP to target identifier → return masked identifier
- **Output:** `{ linkRequestId: UUID, otpSentTo: "j***@example.com", expiresAt: ISO8601 }`
- **Events:** notification.send (linking OTP)
- **Errors:** ACCOUNT_NOT_FOUND (404), ALREADY_LINKED (422), SELF_LINK (422)

### ConfirmAccountLinkingCommand
- **Type:** Command
- **Input:** `{ customerId: UUID, linkRequestId: UUID, otpCode: string }`
- **Validation:** OTP valid + not expired, link request exists and belongs to customer
- **Logic:** BEGIN TRANSACTION → merge points (sum balances) → merge history → mark secondary as merged → update tier (keep higher) → COMMIT → audit log
- **Output:** `{ mergedBalance: number, transactionsMerged: number, linkedAt: ISO8601 }`
- **Events:** customer.accounts_linked
- **Errors:** INVALID_OTP (422), LINK_EXPIRED (422)

### Loyalty Service (Additional)

### GetCustomerTierQuery
- **Type:** Query
- **Input:** `{ customerId: UUID }`
- **Logic:** Get customer tier_id → join tiers table → calculate progress to next tier
- **Output:** `{ currentTier: { name, multiplier, badge }, nextTier: { name, pointsRequired, progress } }`

### GetTiersQuery
- **Type:** Query (Public)
- **Input:** none
- **Logic:** SELECT * FROM tiers ORDER BY sort_order
- **Output:** `[{ id, name, threshold, multiplier, benefits }]`

### Staff Service (Additional)

### GetLoyaltySummaryQuery
- **Type:** Query (Staff token)
- **Input:** `{ customerId: UUID }`
- **Logic:** Get customer + tier + balance + last 3 transactions + lifetime points
- **Output:** `{ name, tier, pointsBalance, lifetimePoints, memberSince, recentTransactions: [...] }`

### Admin Service (Additional)

### GetRatesConfigQuery
- **Type:** Query (Admin token)
- **Input:** none
- **Logic:** SELECT current config (accrual_rate, redemption_rate, min_redemption, max_discount_pct)
- **Output:** `{ accrualRate, redemptionRate, minRedemption, maxDiscountPercent }`

### GetCampaignsQuery
- **Type:** Query (Admin token)
- **Input:** `{ status?: string, pageNumber: number, pageSize: number }`
- **Logic:** SELECT campaigns with filters + pagination
- **Output:** `{ data: [...], meta: { page, pageSize, total } }`

### UpdateCampaignCommand
- **Type:** Command (Admin token)
- **Input:** `{ campaignId: UUID, name?, value?, endDate?, status?, maxBudget? }`
- **Logic:** Validate campaign exists → update fields → audit log
- **Output:** `{ updated: true }`
- **Errors:** CAMPAIGN_NOT_FOUND (404), INVALID_STATUS_TRANSITION (422)

### GetAuditLogsQuery
- **Type:** Query (Admin token)
- **Input:** `{ customerId?: UUID, action?: string, from?: date, to?: date, pageNumber, pageSize }`
- **Logic:** SELECT audit_logs with filters + pagination (read-only, append-only table)
- **Output:** `{ data: [...], meta: { page, pageSize, total } }`
