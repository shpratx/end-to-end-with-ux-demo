# Solution Architecture — Next Loyalty Program

**Version:** 1.0.0  
**Mode:** Greenfield  
**Date:** 2026-05-18  
**Delivery:** 4 Sprints (8 weeks)

---

## 1. System Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ACTORS                                               │
│                                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   Customer   │  │    Store     │  │    Marketing    │  │    Platform     │  │
│  │ (Mobile/Web) │  │  Associate   │  │    Manager      │  │    Engineer     │  │
│  │              │  │   (POS)      │  │ (Admin Portal)  │  │                 │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬────────┘  └────────┬────────┘  │
└─────────┼──────────────────┼───────────────────┼───────────────────┼────────────┘
          │                  │                   │                   │
          ▼                  ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY / BFF LAYER                                   │
│         Rate Limiting (1000 req/min) │ OAuth2/JWT │ API Versioning (/v1/)         │
└────────────────────────────────┬────────────────────────────────────────────────┘
                                 │
         ┌───────────┬───────────┼───────────┬───────────────┐
         ▼           ▼           ▼           ▼               ▼
┌─────────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│auth-service │ │loyalty-  │ │notifica- │ │admin-    │ │bff-      │
│             │ │core      │ │tion-svc  │ │service   │ │service   │
│• Register   │ │• Accrual │ │• Push    │ │• Config  │ │• Mobile  │
│• Login      │ │• Redeem  │ │• Email   │ │• Reports │ │• Web     │
│• JWT/Refresh│ │• Ledger  │ │• In-App  │ │• Audit   │ │• POS     │
│• Social Auth│ │• Tiers   │ │• Template│ │• Campaign│ │          │
└──────┬──────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────────┘
       │              │            │             │
       ▼              ▼            ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DATA & MESSAGING LAYER                                  │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────────────────────────────┐ │
│  │ PostgreSQL │  │   Redis    │  │              Apache Kafka                   │ │
│  │ (Primary + │  │  (Cache +  │  │  Topics: customer-events, loyalty-events,  │ │
│  │  Standby)  │  │  Rate Lim) │  │  notification-commands, notification-events│ │
│  └────────────┘  └────────────┘  └────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
          │                                          │
          ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL SYSTEMS                                          │
│  ┌────────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐ ┌────────────────┐  │
│  │   Email    │ │FCM/APNs  │ │Google/Apple  │ │E-commerce│ │  POS System    │  │
│  │  Provider  │ │(Push)    │ │   OAuth      │ │ Platform │ │  (In-Store)    │  │
│  │(SendGrid/  │ │          │ │              │ │(Webhooks)│ │  (REST/File)   │  │
│  │   SES)     │ │          │ │              │ │          │ │                │  │
│  └────────────┘ └──────────┘ └──────────────┘ └──────────┘ └────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Bounded Contexts

| Context | Responsibility | Key Entities | Owner Service |
|---------|---------------|--------------|---------------|
| **Auth Context** | Registration, login, sessions, social auth, password reset, OTP verification | `customers`, `otp_codes`, `refresh_tokens`, `social_accounts`, `consents` | `auth-service` (Sprint 1) |
| **Loyalty Context** | Points ledger, accrual engine, redemption, rules engine, balance management | `points_ledger`, `redemption_rules`, `accrual_config` | `loyalty-core` (Sprint 2) |
| **Customer Context** | Profiles, preferences, account linking, QR code generation | `customers` (profile fields), `preferences`, `linked_accounts` | `auth-service` / `bff-service` (Sprint 2) |
| **Notification Context** | Template rendering, multi-channel delivery (push/email/in-app), preferences | `notifications`, `push_tokens`, `notification_templates` | `notification-service` (Sprint 1) |
| **Tier Context** | Tier engine, auto-upgrade, annual downgrade review, tier benefits | `tiers`, `customer_tier_history` | `loyalty-core` (Sprint 3) |
| **Campaign Context** | Promotional bonuses, eligibility rules, budget tracking, early access | `campaigns`, `campaign_awards`, `eligibility_rules` | `admin-service` (Sprint 3) |
| **Admin Context** | Configuration management, reporting, audit log viewer, staff management | `audit_logs`, `system_config`, `staff_users` | `admin-service` (Sprint 3) |
| **Integration Context** | Webhook receivers, POS adapter, e-commerce adapter, DLQ management | `webhook_events`, `dead_letter_queue` | `bff-service` (Sprint 2) |

---

## 3. Technology Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Language | TypeScript / Node.js | Full-stack consistency (frontend + backend), strong typing, large ecosystem |
| Backend Framework | NestJS | Modular architecture, built-in DI, CQRS support, decorator-based, enterprise-grade |
| Database | PostgreSQL (Primary + Sync Standby) | ACID for points ledger, pgvector for future personalisation, row-level locking |
| Connection Pooling | PgBouncer | Manage connection limits under high concurrency |
| Cache | Redis Cluster (3-node) | Sub-ms rate limiting, balance cache (2s TTL), session metadata |
| Messaging | Apache Kafka (3 brokers, RF=3) | Durable event log, replay capability, partition by customer_id for ordering |
| Mobile | React Native | Single codebase iOS+Android, shared design system with web, native modules for push/biometric |
| Web Frontend | React / Next.js | SSR for SEO, shared component library with mobile, responsive |
| Admin Portal | React (SPA) | Internal tool, no SEO needed, role-based access |
| IaC | Terraform | Multi-cloud capable, declarative, state management, modular |
| CI/CD | GitHub Actions | Git → lint → test → SAST → build → deploy staging (auto) → prod (manual approval) |
| Container Registry | Private (vulnerability scanning) | Image storage with security scanning gate |
| Monitoring | Prometheus + Grafana | Metrics collection, dashboards, alerting on SLA breaches |
| Logging | Structured JSON (ELK/CloudWatch) | 90-day hot retention, correlation_id on every log line |
| Tracing | OpenTelemetry + Jaeger | W3C Trace Context propagation, distributed tracing |
| API Documentation | OpenAPI 3.0.3 | Contract-first design, auto-generated client SDKs |
| Schema Migrations | Versioned (Flyway/node-pg-migrate) | Repeatable, auditable database changes |

---

## 4. Security Architecture

### 4.1 Authentication Flows

| Actor | Method | Token | Expiry | Storage |
|-------|--------|-------|--------|---------|
| Customer (mobile) | Email/password + optional biometric | JWT access + refresh | 15min / 30 days | Keychain/Keystore |
| Customer (web) | Email/password | JWT access + refresh | 15min / 7 days | httpOnly cookie |
| Customer (social) | Google/Apple OAuth 2.0/OIDC | JWT (linked to account) | 15min / 30 days | Keychain/Keystore |
| Store Associate | Employee ID + SSO (AD/Okta) | Staff JWT | 4 hours (shift-based) | POS memory |
| POS Terminal | API key (device-level) | API key + terminal_id claim | Rotated quarterly | Secure config |
| Admin/Marketing | SSO + MFA mandatory | Short-lived JWT | 1 hour | httpOnly cookie |
| E-commerce Webhook | HMAC-SHA256 signature | Per-request validation | N/A | Shared secret |

### 4.2 Encryption

| Layer | Standard | Scope |
|-------|----------|-------|
| At Rest (DB) | AES-256 (database-level) | All data |
| At Rest (PII) | AES-256 (application-level) | email, phone, name columns |
| In Transit | TLS 1.2+ mandatory | All communications |
| Internal Services | mTLS | Service-to-service |
| Passwords | bcrypt (work factor 12) | Customer passwords |
| OTP Codes | bcrypt | Stored hashed |
| Refresh Tokens | SHA-256 | Stored hashed |

### 4.3 RBAC Model

| Role | Permissions |
|------|------------|
| `customer` | Read own profile/balance/history, redeem points, manage preferences |
| `loyalty_staff` | Identify customer, view loyalty summary, initiate earn/redeem, request adjustment (≤500 pts) |
| `loyalty_manager` | All staff + approve adjustments (>500 pts), view reports |
| `loyalty_admin` | Full system configuration, tier/campaign CRUD, audit log access |
| `platform_engineer` | Infrastructure access, deployment, monitoring (no customer data) |

### 4.4 Security Controls

| Control | Implementation |
|---------|---------------|
| Rate Limiting | Sliding window (Redis): 5 reg/IP/hr, 3 OTP/10min, 10 login/IP/hr, 1000 req/min/client |
| Account Lockout | 5 failed attempts → 30 min lockout |
| CORS | Whitelist: app domains only |
| CSP Headers | `script-src 'self'; style-src 'self' 'unsafe-inline'` |
| Input Validation | Server-side on all inputs, parameterised queries |
| PII Logging | PII fields excluded from application logs |
| Audit Logging | All state changes logged with actor, timestamp, before/after state |

---

## 5. Deployment Model

### 5.1 Kubernetes Namespaces

| Namespace | Contents |
|-----------|----------|
| `loyalty-services` | auth-service, loyalty-core, notification-service, admin-service, bff-service |
| `loyalty-data` | PostgreSQL operator, Redis cluster, Kafka brokers, PgBouncer |
| `loyalty-monitoring` | Prometheus, Grafana, Jaeger, alertmanager |

### 5.2 Service Deployment

| Service | Replicas (Prod) | CPU | Memory | Health Check | Sprint |
|---------|----------------|-----|--------|--------------|--------|
| auth-service | 3 | 500m | 512Mi | /health, /health/ready | 1 |
| loyalty-core | 3 | 500m | 512Mi | /health, /health/ready | 2 |
| notification-service | 2 | 250m | 256Mi | /health | 1 |
| admin-service | 2 | 250m | 256Mi | /health | 3 |
| bff-service | 3 | 500m | 512Mi | /health | 2 |
| api-gateway | 3 | 500m | 512Mi | /health | 1 |
| web-app | 2 (behind CDN) | 250m | 256Mi | / (200 OK) | 1 |

### 5.3 Scaling

| Strategy | Configuration |
|----------|--------------|
| HPA Trigger | CPU > 70% OR request rate > 500 req/s per pod |
| Min Replicas | 2 (non-critical), 3 (critical: auth, loyalty-core, gateway) |
| Max Replicas | 15 per service |
| Scale-up Time | < 2 minutes |
| Scale-down Cooldown | 5 minutes |

### 5.4 Disaster Recovery

| Parameter | Target |
|-----------|--------|
| RTO | 1 hour |
| RPO | 5 minutes |
| Deployment | Multi-AZ |
| DB Replication | Synchronous standby |
| Failover | Automated (30s health check failure → promote standby) |
| Backups | Daily full, 30-day retention, point-in-time recovery (5-min granularity) |
| DR Drills | Quarterly |

### 5.5 Environments

| Environment | Purpose | Deploy Method | URL |
|-------------|---------|---------------|-----|
| Development | Local dev | Docker Compose | localhost:* |
| Staging | Integration/QA | Auto-deploy on merge to main | staging.loyalty.next.co.uk |
| Production | Live customers | Manual approval gate | loyalty.next.co.uk |

---

## 6. Data Architecture

### 6.1 Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│  customers   │──1:N──│ points_ledger│       │    tiers     │
│              │       │              │       │              │
│ id (PK)      │       │ id (PK)      │       │ id (PK)      │
│ email ◆      │       │ customer_id  │       │ name         │
│ phone ◆      │       │ type         │       │ threshold    │
│ name ◆       │       │ points       │       │ multiplier   │
│ password_hash│       │ reference_id │       │ badge_color  │
│ status       │       │ channel      │       │ benefits     │
│ tier_id (FK) │──N:1──│ campaign_id  │       └──────────────┘
│ loyalty_id   │       │ created_at   │
└──────┬───────┘       └──────────────┘
       │
       ├──1:N──┌──────────────┐    ┌──────────────────┐
       │       │  otp_codes   │    │  refresh_tokens  │
       │       └──────────────┘    └──────────────────┘
       │
       ├──1:N──┌──────────────┐    ┌──────────────────┐
       │       │social_accounts│    │    consents      │
       │       └──────────────┘    └──────────────────┘
       │
       ├──1:N──┌──────────────┐    ┌──────────────────┐
       │       │notifications │    │   push_tokens    │
       │       └──────────────┘    └──────────────────┘
       │
       └──1:N──┌──────────────┐
               │  audit_logs  │ (append-only)
               └──────────────┘
```

### 6.2 Tables by Bounded Context

| Context | Table | PII Encrypted | Retention | Sprint |
|---------|-------|---------------|-----------|--------|
| **Auth** | customers | email, phone, name (AES-256) | Active + 3yr post-closure | 1 |
| **Auth** | otp_codes | code_hash (bcrypt) | 24 hours (auto-purge) | 1 |
| **Auth** | refresh_tokens | token_hash (SHA-256) | Until expiry/revocation | 1 |
| **Auth** | social_accounts | email (AES-256) | Tied to customer lifecycle | 1 |
| **Auth** | consents | — | Indefinite (proof of consent) | 1 |
| **Notification** | notifications | — | 90 days | 1 |
| **Notification** | push_tokens | — | Until deactivation | 1 |
| **Notification** | notification_templates | — | Indefinite | 1 |
| **Loyalty** | points_ledger | — | Active + 7 years | 2 |
| **Tier** | tiers | — | Indefinite | 3 |
| **Campaign** | campaigns | — | 7 years | 3 |
| **Admin** | audit_logs | — (append-only, no UPDATE/DELETE) | 7 years | 1 |
| **Admin** | staff_users | — | Employment + 1 year | 3 |

---

## 7. Cross-Cutting Concerns

### 7.1 Observability

| Aspect | Implementation | Standard |
|--------|---------------|----------|
| Logging | Structured JSON, correlation_id on every line | PII excluded, 90-day hot / 1-year cold |
| Metrics | RED method (Rate, Errors, Duration) per service | Prometheus + Grafana dashboards |
| Tracing | OpenTelemetry with W3C Trace Context | 7-day retention, sample rate configurable |
| Alerting | SLO-based (error budget alerts) | Latency >2s warning, error >1% page, availability drop critical |
| Business Metrics | Sign-ups, points earned/redeemed, active users | Real-time dashboard |

### 7.2 Feature Flags

| Aspect | Choice |
|--------|--------|
| Provider | LaunchDarkly or Unleash (self-hosted) |
| Use Cases | Progressive rollout, kill switches, A/B testing campaigns |
| Evaluation | Server-side (backend) + client-side (mobile/web) |
| Audit | Flag changes logged with actor and timestamp |

### 7.3 Audit Trail

| Property | Implementation |
|----------|---------------|
| Storage | Append-only table (no UPDATE/DELETE permissions) |
| Schema | `{event_id, timestamp, actor_type, actor_id, action, entity_type, entity_id, before_state, after_state, source_system, ip_address, correlation_id}` |
| Retention | 7 years |
| Access | Admin-only query API, CSV export for compliance |

### 7.4 Rate Limiting

| Property | Implementation |
|----------|---------------|
| Algorithm | Redis sliding window |
| Scope | Per-IP (public), per-client (authenticated), per-terminal (POS) |
| Headers | `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset` |
| Failure Mode | Fail-closed (reject if Redis unavailable for writes) |

### 7.5 Error Handling

| Property | Implementation |
|----------|---------------|
| Format | RFC 7807 Problem Details: `{type, title, status, detail, instance, trace_id}` |
| Client Errors | 4xx with actionable detail |
| Server Errors | 5xx with trace_id (no internal details exposed) |
| Validation | Inline field-level errors array |

---

## 8. User Journey Mapping

### 8.1 Enrolment Journey (Sprint 1)

```
Landing Page → Programme Overview → Data Use Explanation → Registration Form → OTP Verification → Confirmation → Dashboard

**Screen mapping (from wireframes):**
| Step | Wireframe Screen | Service | API |
|------|-----------------|---------|-----|
| 1 | Loyalty Landing Page | BFF (static) | — |
| 2 | Programme Overview | BFF (static) | — |
| 3 | Data Use Statement | BFF (static) | — |
| 4 | Enrolment Form | Auth Service | POST /auth/register |
| 5 | OTP Verification | Auth Service | POST /auth/verify-otp |
| 6 | Confirmation & QR Code | Auth + Loyalty | GET /customers/me/qr-code |
| 7 | Loyalty Dashboard | BFF | GET /customers/me/dashboard |
     │                │                    │                      │                   │                │            │
     ▼                ▼                    ▼                      ▼                   ▼                ▼            ▼
  web-app/        web-app/             web-app/              auth-service         auth-service      auth-service  bff-service
  mobile-app      mobile-app           mobile-app            (RegisterCmd)        (VerifyOtpCmd)    (LoginCmd)    (Dashboard)
                                                                  │                   │                │
                                                                  ▼                   ▼                ▼
                                                             PostgreSQL           Email Provider     Kafka
                                                             (customers)          (OTP delivery)     (customer.registered)
                                                                                                         │
                                                                                                         ▼
                                                                                                  notification-service
                                                                                                  (Welcome email/push)
```

### 8.2 In-Store Points Earning (Sprint 2)

**Wireframe screens:** POS Transaction Screen → QR Code Screen (customer) → Scan Confirmation → Payment Screen → POS Receipt with Points Summary → Post-Purchase Balance Update (app)

```
POS Scan → Identify Customer → Process Transaction → Earn Points → Notification
    │              │                    │                  │              │
    ▼              ▼                    ▼                  ▼              ▼
POS System    bff-service          POS System         loyalty-core   notification-service
              (GET /identify)      (POST /earn)       (EarnPointsCmd)  (push + in-app)
                   │                                       │
                   ▼                                       ▼
              PostgreSQL                              PostgreSQL        Kafka
              (customer lookup)                       (points_ledger)   (points.earned)
```

### 8.3 Online Points Earning (Sprint 2)

```
Purchase Complete → Webhook Fires → Validate Signature → Earn Points → Balance Update → Notification
       │                  │                 │                  │              │               │
       ▼                  ▼                 ▼                  ▼              ▼               ▼
  E-commerce         bff-service        bff-service       loyalty-core     Redis          notification-service
  Platform           (webhook recv)     (HMAC verify)     (EarnPointsCmd)  (balance cache) (push + in-app)
                                                               │
                                                               ▼
                                                          PostgreSQL + Kafka
```

### 8.4 Points Redemption (Sprint 2)

```
Checkout → Select Points → Validate Rules → Apply Discount → Deduct Points → Notification
    │            │                │                │                │               │
    ▼            ▼                ▼                ▼                ▼               ▼
 web-app/    bff-service      loyalty-core     E-commerce/      loyalty-core    notification-service
 POS         (redeem UI)      (Rules Engine)   POS (discount)   (RedeemCmd)     (push + in-app)
                                    │                                 │
                                    ▼                                 ▼
                              Redis (balance)                    PostgreSQL
                              + PostgreSQL                       (ledger entry,
                              (row lock)                         atomic deduct)
```

---

## 9. Architecture Decision Records

### ADR-01: PostgreSQL as Primary Database

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Loyalty program requires ACID transactions for points ledger (strong consistency per NFR-08). Need pgvector extension for future personalisation. |
| **Decision** | Use PostgreSQL as the single relational database for all loyalty services. Points ledger requires row-level locking for concurrent redemption safety. |
| **Consequences** | Strong consistency guaranteed. Horizontal read scaling via read replicas. Must manage connection pooling (PgBouncer). Schema migrations via versioned migration tool. |

### ADR-02: JWT + Refresh Token for Customer Authentication

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Customers access via mobile app (long sessions) and web (shorter sessions). Need stateless auth for horizontal scaling. |
| **Decision** | Short-lived JWT access tokens (15 min) + long-lived refresh tokens (30 days mobile, 7 days web). Refresh tokens stored in DB for revocation capability. |
| **Consequences** | Stateless verification for most requests (no DB hit). Revocation requires checking refresh token on renewal. Token rotation on each refresh prevents replay. |

### ADR-03: Event-Driven Notification Delivery

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Multiple features trigger notifications (points earned, redeemed, expired, tier change). Notification delivery must not block business logic. |
| **Decision** | Business services emit domain events (e.g., `points.earned`). Notification service consumes events asynchronously, renders templates, dispatches via appropriate channel. |
| **Consequences** | Decoupled: business logic unaffected by notification failures. Retry with backoff on delivery failure. Dead letter queue for undeliverable notifications. |

### ADR-04: Monorepo with Service-per-Bounded-Context

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Sprint 1 establishes foundation. Team is small. Need fast iteration without premature distribution complexity. |
| **Decision** | Monorepo with clear module boundaries: auth-service, notification-service, loyalty-core (Sprint 2+). Deploy as separate containers from same repo. Shared libraries for common concerns (logging, auth middleware, error handling). |
| **Consequences** | Fast development, shared tooling, atomic commits across services. Must enforce module boundaries via linting. Can extract to separate repos later if team grows. |

### ADR-05: Redis for Session Cache and Rate Limiting

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Rate limiting (registration, OTP, login) needs shared state across service instances. Balance cache (Sprint 2) needs sub-500ms reads. |
| **Decision** | Redis cluster for: rate limit counters (sliding window), session metadata, and future points balance cache. TTL-based expiry for all keys. |
| **Consequences** | Fast reads for rate limiting without DB load. Must handle Redis unavailability gracefully (fail-open for reads, fail-closed for writes). Cluster mode for HA. |

### ADR-06: Kafka for Domain Events

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Need reliable event delivery for notifications, future analytics, and cross-service communication. Must guarantee at-least-once delivery. |
| **Decision** | Kafka (3 brokers, replication factor 3) for all domain events. Topics: `customer-events`, `loyalty-events`, `notification-commands`. Consumer groups per service. Partition by customer_id for ordering. |
| **Consequences** | Durable event log enables replay. Must handle idempotent consumers (deduplication). Schema registry for contract evolution. |

### ADR-07: Cross-Platform Mobile with React Native

| Field | Value |
|-------|-------|
| **Status** | Accepted (Sprint 1) |
| **Context** | Need iOS + Android app. Small team. Design system must be shared. Barcode scanning and push notifications required. |
| **Decision** | React Native for mobile app. Shared component library between web (React) and mobile (React Native) where possible. Native modules for push tokens and biometric auth. |
| **Consequences** | Single codebase for both platforms. Faster iteration. Some native bridge work for push/biometric. Performance acceptable for loyalty app (not gaming/video). |

---

## 10. Sprint Delivery Map

| Sprint | Epics | Key Deliverables |
|--------|-------|-----------------|
| **Sprint 1** | EP-01, EP-02 | Infrastructure, CI/CD, Design System, API Gateway, Observability, Auth (register/login/OTP), Notification Service, Mobile/Web Shell |
| **Sprint 2** | EP-03, EP-04 | Points Accrual Engine, Balance Display, POS Identification, Transaction History, Redemption, Rules Engine, Customer Profile, Dashboard, Webhook Adapters |
| **Sprint 3** | EP-05, EP-06 | POS Loyalty Display, Manual Adjustments, Staff Auth, Audit Trail, Tier Engine, Campaigns, Admin Portal, Sign-up Bonus |
| **Sprint 4** | EP-07, EP-08 | Account Linking, Points Expiration, Gamification Badges, DR/Failover, Load Testing, Fraud Prevention, Offline Mode |

---

## 11. Key Integration Contracts

| System | Protocol | Direction | Auth | Circuit Breaker | Sprint |
|--------|----------|-----------|------|-----------------|--------|
| Email Provider (SendGrid/SES) | REST API | Outbound | API key | 5 failures/30s → 60s break | 1 |
| FCM (Android Push) | REST API | Outbound | Service account | 5 failures/30s → 60s break | 1 |
| APNs (iOS Push) | HTTP/2 | Outbound | Certificate | 5 failures/30s → 60s break | 1 |
| Google OAuth | OAuth 2.0/OIDC | Outbound | Client credentials | 3 failures/30s → 30s break | 1 |
| Apple Sign In | OAuth 2.0/OIDC | Outbound | Client credentials | 3 failures/30s → 30s break | 1 |
| E-commerce Platform | HTTPS Webhook | Inbound | HMAC-SHA256 signature | N/A (inbound) | 2 |
| POS System | REST API / File polling | Bidirectional | API key | 5 failures/30s → 60s break | 2 |

---

## 12. Performance Targets

| Operation | Target (p95) | Validation |
|-----------|-------------|------------|
| Balance query | < 500ms | Redis cache hit |
| Points accrual | < 2s | End-to-end including notification emit |
| Points redemption | < 2s | Including row lock + atomic deduct |
| POS customer lookup | < 2s | Including network to POS |
| Notification delivery | < 30s | From event to push received |
| App cold start | < 3s | Including splash + auth check |
| 10x volume capacity | Sustained | Load tested in Sprint 4 |
| Availability | 99.9% | Multi-AZ + automated failover |
