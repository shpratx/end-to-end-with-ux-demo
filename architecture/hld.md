# High-Level Design — Dunelm Loyalty Program

**Version:** 1.0.0 | **Mode:** Greenfield | **Date:** 2026-05-18  
**Tech Stack:** TypeScript, NestJS, PostgreSQL, Redis, Kafka, React Native, React/Dunelm.js

---

## 1. System Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CLIENTS                                           │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │Mobile App  │  │  Web App   │  │Admin Portal│  │  POS Terminal      │    │
│  │(React Nat.)│  │(Dunelm.js)   │  │(React SPA) │  │  (Store Staff)     │    │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘  └─────────┬──────────┘    │
└────────┼────────────────┼───────────────┼───────────────────┼───────────────┘
         │                │               │                   │
         ▼                ▼               ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                                          │
│   Rate Limiting (1000 req/min) │ JWT/API-Key Auth │ CORS │ Correlation ID   │
└────────────────────────────────────────┬────────────────────────────────────┘
                                         │
         ┌───────────┬───────────────────┼───────────────────┬────────────┐
         ▼           ▼                   ▼                   ▼            ▼
┌─────────────┐ ┌──────────┐ ┌────────────────┐ ┌────────────────┐ ┌─────────┐
│ BFF Service │ │  Auth    │ │  Loyalty Core  │ │  Notification  │ │  Admin  │
│             │ │ Service  │ │    Service     │ │    Service     │ │ Service │
│• Mobile BFF │ │• Register│ │• Points Earn   │ │• Push (FCM/    │ │• Config │
│• Web BFF    │ │• Login   │ │• Points Redeem │ │  APNs)         │ │• Reports│
│• POS BFF    │ │• JWT/    │ │• Balance       │ │• Email (SG/SES)│ │• Audit  │
│• Webhooks   │ │  Refresh │ │• Tiers         │ │• In-App        │ │• Campaig│
│             │ │• Social  │ │• Rules Engine  │ │• Templates     │ │  ns     │
└──────┬──────┘ └────┬─────┘ └───────┬────────┘ └───────┬────────┘ └────┬────┘
       │              │               │                  │               │
       └──────────────┴───────────────┼──────────────────┴───────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       DATA & MESSAGING LAYER                                 │
│                                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌────────────────────────────────┐ │
│  │  PostgreSQL  │    │    Redis     │    │          Apache Kafka          │ │
│  │  (Primary +  │    │  (3-node    │    │  (3 brokers, RF=3)             │ │
│  │   Standby)   │    │   cluster)  │    │                                │ │
│  │              │    │              │    │  Topics:                       │ │
│  │• customers   │    │• Rate limits │    │  • customer-events             │ │
│  │• points_ledg.│    │• Balance     │    │  • loyalty-events              │ │
│  │• notificatio.│    │  cache (2s)  │    │  • notification-commands       │ │
│  │• audit_logs  │    │• Dedup keys  │    │  • notification-events         │ │
│  └──────────────┘    └──────────────┘    └────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
         │                                              │
         ▼                                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        EXTERNAL SYSTEMS                                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌───────────┐ ┌─────────────┐  │
│  │  Email   │ │FCM / APNs│ │Google / Apple│ │E-commerce │ │ POS System  │  │
│  │(SendGrid/│ │  (Push)  │ │   OAuth      │ │ (Webhooks)│ │ (REST/File) │  │
│  │  SES)    │ │          │ │              │ │           │ │             │  │
│  └──────────┘ └──────────┘ └──────────────┘ └───────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Data Flow Diagrams

### 2.1 Customer Registration & Login

```
Customer                Gateway        Auth Service       PostgreSQL      Kafka         Notification Svc
   │                       │               │                 │              │                │
   │──POST /auth/register─▶│──rate check──▶│                 │              │                │
   │                       │               │──INSERT customer─▶│              │                │
   │                       │               │   (status=pending)│              │                │
   │                       │               │──generate OTP────▶│              │                │
   │                       │               │──emit─────────────────────────▶│                │
   │                       │               │  (notification.send: OTP email) │                │
   │◀──202 {customer_id}──│◀──────────────│                 │              │──send email───▶│
   │                       │               │                 │              │                │
   │──POST /auth/verify───▶│──────────────▶│──validate OTP──▶│              │                │
   │                       │               │──UPDATE status=active──────────▶│                │
   │                       │               │──emit customer.registered──────▶│──welcome msg─▶│
   │◀──200 {tokens}───────│◀──────────────│                 │              │                │
   │                       │               │                 │              │                │
   │──POST /auth/login────▶│──rate check──▶│──verify creds──▶│              │                │
   │                       │               │──issue JWT+refresh──────────────│                │
   │◀──200 {access,refresh}│◀──────────────│                 │              │                │
```

### 2.2 Points Accrual (Online + In-Store)

```
ONLINE:
E-commerce Platform       Gateway/BFF         Loyalty Core        PostgreSQL      Kafka
   │                          │                    │                  │              │
   │──POST /webhooks/ecomm──▶│                    │                  │              │
   │  (HMAC-SHA256 signed)   │──verify sig────────│                  │              │
   │                          │──dedup check──────▶│ (Redis)          │              │
   │                          │──POST /points/earn▶│                  │              │
   │                          │                    │──calc points─────│              │
   │                          │                    │  (amount × rate) │              │
   │                          │                    │──INSERT ledger──▶│              │
   │                          │                    │──invalidate cache│ (Redis)      │
   │                          │                    │──emit points.earned────────────▶│
   │◀──202 Accepted──────────│                    │                  │              │

IN-STORE:
POS Terminal              Gateway/BFF         Loyalty Core        PostgreSQL      Kafka
   │                          │                    │                  │              │
   │──scan QR / loyalty_id──▶│                    │                  │              │
   │  POST /webhooks/pos     │──validate API key──│                  │              │
   │  (API key auth)         │──dedup check──────▶│ (Redis)          │              │
   │                          │──POST /points/earn▶│                  │              │
   │                          │                    │──INSERT ledger──▶│              │
   │                          │                    │──emit points.earned────────────▶│
   │◀──202 Accepted──────────│                    │                  │              │
```

### 2.3 Points Redemption

```
Customer        BFF           Loyalty Core       PostgreSQL        Redis         Kafka
   │              │                │                 │               │              │
   │──redeem req─▶│──POST /redeem─▶│                 │               │              │
   │              │                │──check balance──▶│               │              │
   │              │                │──validate rules──│               │              │
   │              │                │  (min balance,   │               │              │
   │              │                │   redemption amt)│               │              │
   │              │                │──SELECT FOR UPDATE (row lock)───▶│              │
   │              │                │──deduct points──▶│               │              │
   │              │                │──INSERT ledger   │               │              │
   │              │                │  (type=redeem)  ▶│               │              │
   │              │                │──invalidate─────────────────────▶│              │
   │              │                │──emit points.redeemed───────────────────────────▶│
   │◀──200 {disc}─│◀──────────────│                 │               │              │
```

### 2.4 Notification Delivery

```
Domain Event       Kafka        Notification Svc     Template Engine    Channel Router
   │                 │                │                    │                 │
   │──publish───────▶│                │                    │                 │
   │ (points.earned) │──consume──────▶│                    │                 │
   │                 │                │──resolve template─▶│                 │
   │                 │                │◀──rendered msg─────│                 │
   │                 │                │──route by prefs────────────────────▶│
   │                 │                │                    │                 │
   │                 │                │                    │    ┌────────────┼────────┐
   │                 │                │                    │    ▼            ▼        ▼
   │                 │                │                    │  Email       FCM/APNs  In-App
   │                 │                │                    │  (SendGrid)  (Push)    (DB write)
   │                 │                │                    │
   │                 │◀──notification.delivered────────────│
   │                 │  (or .failed → retry/DLQ)          │
```

### 2.5 Account Linking

```
Customer        BFF           Auth Service       PostgreSQL      External OAuth    Kafka
   │              │                │                 │                │              │
   │──link req───▶│──POST /link───▶│                 │                │              │
   │              │                │──generate state──▶│               │              │
   │◀──redirect──│◀───────────────│  (CSRF token)   │                │              │
   │              │                │                 │                │              │
   │──OAuth flow─────────────────────────────────────────────────────▶│              │
   │◀──callback with code────────────────────────────────────────────│              │
   │              │                │                 │                │              │
   │──confirm────▶│──POST /confirm▶│                 │                │              │
   │              │                │──exchange code───────────────────▶│              │
   │              │                │◀──id_token + profile─────────────│              │
   │              │                │──validate state─▶│               │              │
   │              │                │──INSERT social_accounts─────────▶│              │
   │              │                │──emit customer.account_linked──────────────────▶│
   │◀──200 OK────│◀───────────────│                 │                │              │
```

---

## 3. Customer State Machine

```
                    ┌───────────────────┐
                    │ pending_verification│
          ┌────────┤                   ├────────┐
          │        └─────────┬─────────┘        │
          │                  │                  │
          │ (OTP expired,    │ (OTP verified)   │ (fraud detected)
          │  no re-verify)   │                  │
          ▼                  ▼                  │
   ┌────────────┐    ┌────────────┐            │
   │  deleted   │◀───│   active   │◀───────────┘
   │            │    │            │
   └────────────┘    └──────┬─────┘
          ▲                 │
          │                 │ (policy violation /
          │                 │  admin action)
          │                 ▼
          │          ┌────────────┐
          └──────────│ suspended  │
                     │            │
                     └────────────┘
```

### Transitions Table

| From | To | Trigger | Validations | Side Effects |
|------|-----|---------|-------------|--------------|
| pending_verification | active | OTP verified successfully | Valid OTP, not expired, attempts < 5 | Emit `customer.verified`, send welcome notification, generate loyalty_id |
| pending_verification | deleted | Account purge (72h no verification) | Scheduled job checks created_at | Purge PII, emit `customer.purged` |
| active | suspended | Admin suspension / fraud flag | Admin role required, reason mandatory | Revoke all refresh tokens, emit `customer.suspended`, notify customer |
| active | deleted | Customer self-deletion (GDPR) | Confirm via password/OTP, zero balance required | Anonymise PII, soft-delete, emit `customer.deleted`, 30-day grace |
| suspended | active | Admin reinstatement | Admin role required, review completed | Emit `customer.reinstated`, notify customer |
| suspended | deleted | Admin permanent deletion | Admin role + manager approval | Anonymise PII, emit `customer.deleted` |

---

## 4. Points Transaction State Machine

```
         ┌──────────┐
         │ pending  │
         └────┬─────┘
              │
     ┌────────┼────────┐
     │                  │
     ▼                  ▼
┌──────────┐     ┌──────────┐
│completed │     │  failed  │
└────┬─────┘     └──────────┘
     │
     │ (refund / admin reversal)
     ▼
┌──────────┐
│ reversed │
└──────────┘
```

### Transitions Table

| From | To | Trigger | Validations | Side Effects |
|------|-----|---------|-------------|--------------|
| pending | completed | Transaction confirmed | Sufficient balance (redeem), valid reference_id, dedup check passes | Update balance cache (Redis), emit `points.earned` or `points.redeemed`, notify customer |
| pending | failed | Validation failure / timeout | Insufficient balance, duplicate detected, external system timeout (>10s) | Emit `points.failed`, log reason, no balance change |
| completed | reversed | Refund webhook / admin reversal | Original txn exists, not already reversed, admin approval if manual (>500 pts) | Credit/debit inverse entry, update balance, emit `points.reversed`, notify customer |

---

## 5. Deployment View

### Namespace: `loyalty-app`

| Service | Replicas | CPU (req/lim) | Memory (req/lim) | Scaling Trigger | Health Check |
|---------|----------|---------------|------------------|-----------------|--------------|
| api-gateway | 3 | 250m/500m | 256Mi/512Mi | CPU > 70% OR 500 req/s/pod | GET /health (liveness), GET /health/ready (readiness) |
| bff-service | 3 | 250m/500m | 256Mi/512Mi | CPU > 70% OR 500 req/s/pod | GET /health, GET /health/ready |
| auth-service | 3 | 250m/500m | 256Mi/512Mi | CPU > 70% | GET /health, GET /health/ready |
| loyalty-core | 3 | 250m/500m | 256Mi/512Mi | CPU > 70% OR queue lag > 1000 | GET /health, GET /health/ready |
| notification-service | 2 | 125m/250m | 128Mi/256Mi | Kafka consumer lag > 5000 | GET /health |
| admin-service | 2 | 125m/250m | 128Mi/256Mi | CPU > 70% | GET /health |
| web-app | 2 | 125m/250m | 128Mi/256Mi | CPU > 70% | GET / (200 OK) |

### Namespace: `loyalty-data`

| Service | Replicas | CPU (req/lim) | Memory (req/lim) | Scaling Trigger | Health Check |
|---------|----------|---------------|------------------|-----------------|--------------|
| postgresql-primary | 1 | 1000m/2000m | 2Gi/4Gi | N/A (vertical) | pg_isready |
| postgresql-standby | 1 | 1000m/2000m | 2Gi/4Gi | N/A | pg_isready + replication lag |
| pgbouncer | 2 | 125m/250m | 128Mi/256Mi | Connection count > 80% | TCP port 6432 |
| redis-cluster | 3 | 250m/500m | 512Mi/1Gi | Memory > 80% | redis-cli ping |
| kafka-broker | 3 | 500m/1000m | 1Gi/2Gi | N/A (fixed) | kafka-broker-api-versions |
| kafka-schema-registry | 1 | 125m/250m | 256Mi/512Mi | N/A | GET /subjects (200) |

### Namespace: `loyalty-monitoring`

| Service | Replicas | CPU (req/lim) | Memory (req/lim) | Scaling Trigger | Health Check |
|---------|----------|---------------|------------------|-----------------|--------------|
| prometheus | 1 | 500m/1000m | 1Gi/2Gi | N/A | GET /-/healthy |
| grafana | 1 | 250m/500m | 256Mi/512Mi | N/A | GET /api/health |
| jaeger | 1 | 250m/500m | 512Mi/1Gi | N/A | GET / (14269) |
| alertmanager | 1 | 125m/250m | 128Mi/256Mi | N/A | GET /-/healthy |

### Scaling Policy

| Parameter | Value |
|-----------|-------|
| HPA min replicas | 2 (non-critical), 3 (critical: gateway, auth, loyalty-core) |
| HPA max replicas | 15 |
| Scale-up stabilisation | 0s (immediate) |
| Scale-down cooldown | 300s |
| Pod disruption budget | maxUnavailable: 1 per service |

---

## 6. Security View

### 6.1 Token Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TOKEN LIFECYCLE                                    │
│                                                                          │
│  LOGIN                                                                   │
│    │                                                                     │
│    ▼                                                                     │
│  ┌─────────────────┐     ┌──────────────────┐                           │
│  │  Access Token   │     │  Refresh Token   │                           │
│  │  (JWT, RS256)   │     │  (opaque, hashed │                           │
│  │  TTL: 15 min    │     │   in DB)         │                           │
│  │  Stateless      │     │  TTL: 30d mobile │                           │
│  │  verification   │     │       7d web     │                           │
│  └────────┬────────┘     └────────┬─────────┘                           │
│           │                       │                                      │
│           │ (expired)             │ (POST /auth/refresh)                 │
│           ▼                       ▼                                      │
│  ┌─────────────────────────────────────────┐                            │
│  │         TOKEN ROTATION                   │                            │
│  │  1. Validate refresh token (DB lookup)   │                            │
│  │  2. Revoke old refresh token             │                            │
│  │  3. Issue NEW access token (15 min)      │                            │
│  │  4. Issue NEW refresh token (rotated)    │                            │
│  │  5. Store new token_hash in DB           │                            │
│  └─────────────────────────────────────────┘                            │
│                                                                          │
│  LOGOUT / REVOCATION                                                     │
│    • Mark refresh_token.revoked_at = NOW()                               │
│    • Access token remains valid until 15min expiry (stateless)           │
│    • Force-logout: add token jti to Redis blacklist (15min TTL)          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Encryption Boundaries

```
┌─────────────────────────────────────────────────────────────────────────┐
│  INTERNET                          TLS 1.2+                              │
│  (Client ←──────────────────────────────────────────────▶ API Gateway)  │
└─────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CLUSTER INTERNAL                  mTLS (service mesh)                   │
│  (Gateway ←────────────────────────────────────────────▶ Services)      │
│  (Service ←────────────────────────────────────────────▶ Service)       │
└─────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  DATA LAYER                                                              │
│                                                                          │
│  PostgreSQL:  AES-256 at rest (volume encryption)                        │
│              + AES-256 column-level (email, phone, name)                 │
│                                                                          │
│  Redis:       TLS in-transit, encrypted at rest (volume)                 │
│                                                                          │
│  Kafka:       TLS in-transit, encrypted at rest (volume)                 │
│              + SASL authentication                                        │
│                                                                          │
│  Passwords:   bcrypt (work factor 12) — never reversible                 │
│  OTP codes:   bcrypt — never reversible                                  │
│  Refresh tkns: SHA-256 hash stored — original never persisted            │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.3 RBAC Matrix

| Resource | customer | loyalty_staff | loyalty_manager | loyalty_admin | platform_engineer |
|----------|----------|---------------|-----------------|---------------|-------------------|
| Own Profile | R/W | — | — | R | — |
| Own Balance | R | — | — | R | — |
| Own Transactions | R | — | — | R | — |
| Own Notifications | R/W | — | — | R | — |
| Redeem Points | Execute | — | — | — | — |
| Customer Lookup | — | R | R | R | — |
| Earn Points (POS) | — | Execute | Execute | Execute | — |
| Adjustments (≤500) | — | Create | Create/Approve | Create/Approve | — |
| Adjustments (>500) | — | — | Approve | Create/Approve | — |
| Tier Config | — | — | R | R/W | — |
| Campaign CRUD | — | — | R | R/W | — |
| Rate Config | — | — | — | R/W | — |
| Audit Logs | — | — | R (own store) | R (all) | — |
| System Config | — | — | — | R/W | R |
| Infrastructure | — | — | — | — | Full |
| Monitoring | — | — | — | R | R/W |
| Deploy/Rollback | — | — | — | — | Execute |

---

## 7. Sequence Diagrams

### 7.1 Points Redemption (Multi-Service)

```
Customer       BFF Service      Loyalty Core       PostgreSQL         Redis           Kafka        Notification Svc
   │               │                 │                 │                │               │                │
   │──POST /redeem─▶│                 │                 │                │               │                │
   │  {points:500,  │                 │                 │                │               │                │
   │   order_id}    │                 │                 │                │               │                │
   │               │──validate JWT───│                 │                │               │                │
   │               │                 │                 │                │               │                │
   │               │──POST /points/  │                 │                │               │                │
   │               │  redeem────────▶│                 │                │               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──GET balance────────────────────▶│               │                │
   │               │                 │◀──{balance:1200}─────────────────│               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──validate rules─│                │               │                │
   │               │                 │  (min 100,      │                │               │                │
   │               │                 │   max balance,  │                │               │                │
   │               │                 │   100pt incr.)  │                │               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──BEGIN TXN──────▶│                │               │                │
   │               │                 │──SELECT FOR     │                │               │                │
   │               │                 │  UPDATE (lock)─▶│                │               │                │
   │               │                 │◀──row locked────│                │               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──INSERT ledger  │                │               │                │
   │               │                 │  (type=redeem,  │                │               │                │
   │               │                 │   points=-500)─▶│                │               │                │
   │               │                 │──COMMIT─────────▶│                │               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──DEL balance_cache───────────────▶│               │                │
   │               │                 │                 │                │               │                │
   │               │                 │──emit points.redeemed────────────────────────────▶│                │
   │               │                 │  {customer_id, points:500,       │               │                │
   │               │                 │   order_id, discount:£25}        │               │──consume──────▶│
   │               │                 │                 │                │               │                │
   │               │◀──200 {discount:│                 │                │               │  ┌─────────────│
   │               │   £25, new_bal: │                 │                │               │  │render tmpl  │
   │               │   700}──────────│                 │                │               │  │route channel│
   │◀──200─────────│                 │                 │                │               │  │send push    │
   │               │                 │                 │                │               │  └─────────────│
```

### 7.2 Webhook Processing (E-commerce → Points Earn)

```
E-commerce        API Gateway       BFF Service         Redis          Loyalty Core      PostgreSQL       Kafka
   │                  │                 │                  │                │                │               │
   │──POST /webhooks/ │                 │                  │                │                │               │
   │  ecommerce──────▶│                 │                  │                │                │               │
   │  Headers:        │                 │                  │                │                │               │
   │  X-Webhook-Sig   │                 │                  │                │                │               │
   │  X-Timestamp     │                 │                  │                │                │               │
   │                  │──forward────────▶│                  │                │                │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │──validate sig────│                │                │               │
   │                  │                 │  HMAC-SHA256(    │                │                │               │
   │                  │                 │  secret, body)   │                │                │               │
   │                  │                 │  == X-Webhook-Sig│                │                │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │──check timestamp─│                │                │               │
   │                  │                 │  (reject if >5m) │                │                │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │──dedup check────▶│                │                │               │
   │                  │                 │  GET SHA256(     │                │                │               │
   │                  │                 │  order_id+type)  │                │                │               │
   │                  │                 │◀──NULL (new)─────│                │                │               │
   │                  │                 │──SET dedup key──▶│                │                │               │
   │                  │                 │  (TTL 72h)       │                │                │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │──POST /points/earn──────────────▶│                │               │
   │                  │                 │                  │                │──calc points───│               │
   │                  │                 │                  │                │  (£99.99 × 1   │               │
   │                  │                 │                  │                │   = 99 pts)    │               │
   │                  │                 │                  │                │──INSERT ledger─▶│               │
   │                  │                 │                  │                │  (type=earn,   │               │
   │                  │                 │                  │                │   channel=web) │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │                  │                │──emit──────────────────────────▶│
   │                  │                 │                  │                │  points.earned │               │
   │                  │                 │                  │                │                │               │
   │                  │                 │◀──200 {points:99}────────────────│                │               │
   │◀──202 Accepted──│◀────────────────│                  │                │                │               │
```

---

*End of document.*
