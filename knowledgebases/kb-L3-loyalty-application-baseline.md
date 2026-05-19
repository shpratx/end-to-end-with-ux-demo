# Application Baseline — Next Loyalty Program
### kb-L3-loyalty-application-baseline v1.0.0 (Sprint 1)
### Current state of the Next Loyalty application after Sprint 1 delivery.
### Used by agents to classify requirements as new/enhancement/existing in subsequent sprints.

---

## BL1: Product Inventory

| Product ID | Name | Type | Status | Parameters |
|---|---|---|---|---|
| loyalty-program-v1 | Next Loyalty Program | Points-based loyalty | 🚧 Sprint 1 (Foundation only) | Accrual: 1pt/£1 (configurable), Redemption: 100pts=£5 (configurable), Tiers: TBD (Sprint 3), Expiry: 12mo inactivity (Sprint 4) |

---

## BL2: Feature Inventory

| Feature ID | Feature Name | Status | Module | Description |
|---|---|---|---|---|
| F-01.1 | Cloud Infrastructure | ✅ Sprint 1 | Platform | Kubernetes cluster, PostgreSQL, Redis, Kafka, object storage provisioned via Terraform |
| F-01.2 | API Gateway | ✅ Sprint 1 | Platform | OAuth2/API-key auth, rate limiting (1000 req/min), versioned endpoints (/v1/), standardised errors |
| F-01.3 | Observability Stack | ✅ Sprint 1 | Platform | Structured JSON logging (90-day retention), Prometheus metrics, alerting on SLA breaches |
| F-01.4 | Design System | ✅ Sprint 1 | Frontend | Shared component library (buttons, forms, cards, modals, navigation), WCAG 2.1 AA compliant |
| F-01.5 | CI/CD Pipeline | ✅ Sprint 1 | Platform | Git → build → test → SAST scan → deploy staging (auto) → deploy prod (manual approval) → rollback |
| F-01.6 | Notification Service | ✅ Sprint 1 | Notifications | Event-driven: consumes domain events, renders templates, dispatches via push (FCM/APNs) or email |
| F-01.7 | Mobile App Shell | ✅ Sprint 1 | Frontend | React Native app: bottom nav (Home, History, QR, Profile), deep linking, push permission, offline detection |
| F-02.1 | Customer Registration | ✅ Sprint 1 | Auth | Multi-channel sign-up (app, web, in-store assisted): name, email, phone, password, T&C acceptance |
| F-02.2 | OTP Verification | ✅ Sprint 1 | Auth | 6-digit email OTP, 5-min expiry, 3 resends/10min, 5 attempts before lockout |
| F-02.3 | Authentication | ✅ Sprint 1 | Auth | Email/password + social (Google, Apple), JWT (15min) + refresh (30d mobile/7d web), biometric optional |
| F-02.4 | Terms & Conditions | ✅ Sprint 1 | Auth | Acceptance during registration, version tracking, re-acceptance on update, consent audit trail |

---

## BL3: Screen Inventory

| Screen | Route | Platform | Features Used | Status |
|---|---|---|---|---|
| App Shell (Navigation) | / | Mobile | F-01.7 | ✅ Sprint 1 |
| Web Shell (Layout) | / | Web | F-01.7 | ✅ Sprint 1 |
| Registration | /auth/register | Mobile + Web | F-02.1, F-02.4 | ✅ Sprint 1 |
| OTP Verification | /auth/verify | Mobile + Web | F-02.2 | ✅ Sprint 1 |
| Login | /auth/login | Mobile + Web | F-02.3 | ✅ Sprint 1 |
| Password Reset | /auth/reset-password | Mobile + Web | F-02.3 | ✅ Sprint 1 |
| Notification Center | /notifications | Mobile + Web | F-01.6 | ✅ Sprint 1 |
| Home (Dashboard) | /home | Mobile + Web | — | 🔲 Placeholder (Sprint 2: F-04.5) |
| Transaction History | /history | Mobile + Web | — | 🔲 Placeholder (Sprint 2: F-03.5) |
| QR Code | /qr | Mobile | — | 🔲 Placeholder (Sprint 2: F-03.4) |
| Profile | /profile | Mobile + Web | — | 🔲 Placeholder (Sprint 2: F-04.4) |

---

## BL4: API Inventory

| Endpoint | Method | Handler | Auth | Sprint | Status |
|---|---|---|---|---|---|
| /api/v1/auth/register | POST | RegisterCustomerCommand | Public (rate-limited) | 1 | ✅ |
| /api/v1/auth/verify-otp | POST | VerifyOtpCommand | Public (rate-limited) | 1 | ✅ |
| /api/v1/auth/resend-otp | POST | ResendOtpCommand | Public (rate-limited) | 1 | ✅ |
| /api/v1/auth/login | POST | LoginCommand | Public (rate-limited) | 1 | ✅ |
| /api/v1/auth/login/social | POST | SocialLoginCommand | Public | 1 | ✅ |
| /api/v1/auth/refresh | POST | RefreshTokenCommand | Refresh token | 1 | ✅ |
| /api/v1/auth/logout | POST | LogoutCommand | Bearer token | 1 | ✅ |
| /api/v1/auth/reset-password/request | POST | RequestPasswordResetCommand | Public | 1 | ✅ |
| /api/v1/auth/reset-password/confirm | POST | ConfirmPasswordResetCommand | Reset token | 1 | ✅ |
| /api/v1/notifications | GET | GetNotificationsQuery | Bearer token | 1 | ✅ |
| /api/v1/notifications/{id}/read | PATCH | MarkNotificationReadCommand | Bearer token | 1 | ✅ |
| /api/v1/notifications/unread-count | GET | GetUnreadCountQuery | Bearer token | 1 | ✅ |
| /api/v1/health | GET | HealthController | Public | 1 | ✅ |
| /api/v1/health/ready | GET | ReadinessController | Public | 1 | ✅ |
| /api/v1/points/earn | POST | EarnPointsCommand | API key (POS/e-commerce) | 2 | 🔲 |
| /api/v1/points/redeem | POST | RedeemPointsCommand | Bearer + API key | 2 | 🔲 |
| /api/v1/points/balance | GET | GetBalanceQuery | Bearer token | 2 | 🔲 |
| /api/v1/points/transactions | GET | GetTransactionsQuery | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/profile | GET | GetProfileQuery | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/profile | PUT | UpdateProfileCommand | Bearer token | 2 | 🔲 |
| /api/v1/customers/me | DELETE | DeleteAccountCommand | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/preferences | GET | GetPreferencesQuery | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/preferences | PUT | UpdatePreferencesCommand | Bearer token | 2 | 🔲 |
| /api/v1/customers/identify | GET | IdentifyCustomerQuery | API key (POS) | 2 | 🔲 |
| /api/v1/staff/auth/login | POST | StaffLoginCommand | Public (rate-limited) | 3 | 🔲 |
| /api/v1/staff/customers/search | GET | StaffCustomerSearchQuery | Staff token | 3 | 🔲 |
| /api/v1/staff/customers/{id}/loyalty-summary | GET | GetLoyaltySummaryQuery | Staff token | 3 | 🔲 |
| /api/v1/staff/adjustments | POST | CreateAdjustmentCommand | Staff token | 3 | 🔲 |
| /api/v1/staff/adjustments/{id}/approve | PATCH | ApproveAdjustmentCommand | Manager token | 3 | 🔲 |
| /api/v1/tiers | GET | GetTiersQuery | Public | 3 | 🔲 |
| /api/v1/customers/me/tier | GET | GetCustomerTierQuery | Bearer token | 3 | 🔲 |
| /api/v1/admin/dashboard | GET | GetAdminDashboardQuery | Admin token | 3 | 🔲 |
| /api/v1/admin/config/rates | GET | GetRatesConfigQuery | Admin token | 3 | 🔲 |
| /api/v1/admin/config/rates | PUT | UpdateRatesConfigCommand | Admin token | 3 | 🔲 |
| /api/v1/admin/campaigns | GET | GetCampaignsQuery | Admin token | 3 | 🔲 |
| /api/v1/admin/campaigns | POST | CreateCampaignCommand | Admin token | 3 | 🔲 |
| /api/v1/admin/campaigns/{id} | PUT | UpdateCampaignCommand | Admin token | 3 | 🔲 |
| /api/v1/admin/audit | GET | GetAuditLogsQuery | Admin token | 3 | 🔲 |
| /api/v1/webhooks/ecommerce | POST | ProcessEcommerceWebhook | Webhook signature | 2 | 🔲 |
| /api/v1/webhooks/pos | POST | ProcessPosWebhook | API key | 2 | 🔲 |
| /api/v1/customers/me/qr-code | GET | GetCustomerQrCodeQuery | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/dashboard | GET | GetCustomerDashboardQuery | Bearer token | 2 | 🔲 |
| /api/v1/customers/me/accounts/link | POST | InitiateAccountLinkingCommand | Bearer token | 4 | 🔲 |
| /api/v1/customers/me/accounts/link/confirm | POST | ConfirmAccountLinkingCommand | Bearer token | 4 | 🔲 |
| /api/v1/customers/me/badges | GET | GetCustomerBadgesQuery | Bearer token | 4 | 🔲 |

**Total: 45 endpoints** (14 Sprint 1, 14 Sprint 2, 14 Sprint 3, 3 Sprint 4)

**API Spec:** `/api-spec.yaml` (OpenAPI 3.0.3, 2553 lines)

---

## BL5: Data Model (Tables)

| Table | Key Columns | PII Encrypted | Status |
|---|---|---|---|
| customers | id, email, phone, name, password_hash, status, tier_id, loyalty_id, created_at, updated_at, deleted_at | email, phone, name (AES-256) | ✅ Sprint 1 |
| otp_codes | id, customer_id, code_hash, purpose, expires_at, used_at, attempts, created_at | code_hash (bcrypt) | ✅ Sprint 1 |
| refresh_tokens | id, customer_id, token_hash, device_info, expires_at, revoked_at, created_at | token_hash (SHA-256) | ✅ Sprint 1 |
| social_accounts | id, customer_id, provider, provider_user_id, email, created_at | email (AES-256) | ✅ Sprint 1 |
| consents | id, customer_id, type, version, accepted, ip_address, accepted_at | ip_address (plain — not PII per ICO guidance) | ✅ Sprint 1 |
| notifications | id, customer_id, title, body, type, channel, read_at, delivered_at, created_at | — | ✅ Sprint 1 |
| push_tokens | id, customer_id, platform, token, active, created_at, updated_at | — | ✅ Sprint 1 |
| notification_templates | id, event_type, title_template, body_template, channels, active, updated_at | — | ✅ Sprint 1 |
| audit_logs | id, actor_type, actor_id, action, entity_type, entity_id, before_state, after_state, ip_address, correlation_id, created_at | — (append-only, no UPDATE/DELETE) | ✅ Sprint 1 |

**Sprint 2 tables (planned):**
| points_ledger | id, customer_id, type, points, reference_id, channel, campaign_id, balance_after, created_at | — | 🔲 Sprint 2 |
| tiers | id, name, threshold, multiplier, badge_color, benefits, sort_order, created_at, updated_at | — | 🔲 Sprint 3 |
| campaigns | id, name, type, value, start_date, end_date, eligibility_rules, max_budget, spent_budget, status, created_at, updated_at | — | 🔲 Sprint 3 |
| badges | id, name, description, icon_url, trigger_type, trigger_value, rarity, active, created_at | — | 🔲 Sprint 4 |
| customer_badges | id, customer_id, badge_id, earned_at | — | 🔲 Sprint 4 |
| adjustments | id, customer_id, staff_id, action, points, reason, notes, status, approver_id, approved_at, created_at | — | 🔲 Sprint 3 |
| account_links | id, primary_customer_id, secondary_customer_id, status, merged_at, created_at | — | 🔲 Sprint 4 |

**Total: 16 tables** (9 Sprint 1, 1 Sprint 2, 3 Sprint 3, 3 Sprint 4)

**Architecture docs:** `architecture/hld.md` (483 lines), `architecture/lld.md` (832 lines)

---

## BL6: Integration Inventory

| System | Protocol | Purpose | Circuit Breaker | Status |
|---|---|---|---|---|
| Email Provider (SendGrid/SES) | REST API | OTP delivery, password reset, welcome email | 5 failures/30s → 60s break | ✅ Sprint 1 |
| Firebase Cloud Messaging (FCM) | REST API | Android push notifications | 5 failures/30s → 60s break | ✅ Sprint 1 |
| Apple Push Notification Service (APNs) | HTTP/2 | iOS push notifications | 5 failures/30s → 60s break | ✅ Sprint 1 |
| Google OAuth | OAuth 2.0/OIDC | Social login — Google | 3 failures/30s → 30s break | ✅ Sprint 1 |
| Apple Sign In | OAuth 2.0/OIDC | Social login — Apple | 3 failures/30s → 30s break | ✅ Sprint 1 |
| Kafka (Event Bus) | Kafka protocol | Domain events (customer.registered, notification.send) | N/A (async) | ✅ Sprint 1 |

**Sprint 2 integrations (planned):**
| E-commerce Platform (Webhook) | HTTPS webhook | Purchase events for points accrual | 🔲 Sprint 2 |
| POS System | REST API / file polling | In-store transaction events | 🔲 Sprint 2 |

---

## BL7: Known Limitations (Post Sprint 1)

| LIM ID | Description | Impact | Planned Resolution |
|---|---|---|---|
| LIM-01 | No points accrual or redemption yet | Customers can register but not earn/spend points | Sprint 2 (EP-03, EP-04) |
| LIM-02 | No loyalty dashboard content | Home screen is placeholder | Sprint 2 (EP-04 F-04.5) |
| LIM-03 | No POS integration | Store staff cannot access loyalty system | Sprint 2 (EP-03 F-03.4, F-03.7) + Sprint 3 (EP-05) |
| LIM-04 | No tier system | All customers are same level | Sprint 3 (EP-06 F-06.1) |
| LIM-05 | No admin portal | Configuration requires code changes | Sprint 3 (EP-06 F-06.4) |
| LIM-06 | No offline mode for mobile app | App non-functional without network | Sprint 4 (EP-08 F-08.4) |
| LIM-07 | No fraud prevention rules | Only rate limiting protects against abuse | Sprint 4 (EP-08 F-08.3) |
| LIM-08 | Email-only OTP | No SMS OTP channel | Future enhancement |
| LIM-09 | No account deletion flow | GDPR right to erasure not yet implemented | Sprint 2 (EP-04 F-04.4) |
| LIM-10 | Notification templates hardcoded | Cannot edit templates without code deploy | Sprint 3 (EP-06 F-06.4 admin portal) |

---

## BL8: Event Schema (Domain Events)

| Event | Topic | Payload | Producer | Consumers |
|---|---|---|---|---|
| customer.registered | customer-events | {customer_id, email, channel, timestamp} | Auth Service | Notification Service (welcome email) |
| customer.verified | customer-events | {customer_id, timestamp} | Auth Service | — |
| customer.login | customer-events | {customer_id, method, device, timestamp} | Auth Service | Audit (logging) |
| notification.send | notification-commands | {customer_id, template_id, variables, channels} | Any service | Notification Service |
| notification.delivered | notification-events | {notification_id, channel, timestamp} | Notification Service | — |
| notification.failed | notification-events | {notification_id, channel, reason, retry_count} | Notification Service | DLQ handler |

**Sprint 2 events (planned):**
| points.earned | loyalty-events | {customer_id, points, reference_id, channel} | Loyalty Core | Notification Service, Analytics |
| points.redeemed | loyalty-events | {customer_id, points, order_id, discount} | Loyalty Core | Notification Service |

---

## BL9: Architecture Decisions Record

| ADR | Decision | Rationale | Sprint |
|---|---|---|---|
| ADR-01 | PostgreSQL as primary database | ACID for points ledger, pgvector for future personalisation | Sprint 1 |
| ADR-02 | JWT + Refresh Token auth | Stateless scaling, revocation via DB-stored refresh tokens | Sprint 1 |
| ADR-03 | Event-driven notifications | Decouple business logic from delivery, retry with backoff | Sprint 1 |
| ADR-04 | Monorepo with service boundaries | Fast iteration, shared tooling, clear module boundaries | Sprint 1 |
| ADR-05 | Redis for cache + rate limiting | Sub-ms rate limit checks, future balance cache | Sprint 1 |
| ADR-06 | Kafka for domain events | Durable log, replay capability, partition by customer_id | Sprint 1 |
| ADR-07 | React Native for mobile | Single codebase iOS+Android, shared design system with web | Sprint 1 |

---

## BL10: Infrastructure Configuration

| Component | Specification | Purpose |
|---|---|---|
| Kubernetes Cluster | 3+ nodes, auto-scaling (3-15 pods per service) | Container orchestration |
| PostgreSQL | Primary + synchronous standby, connection pooling (PgBouncer) | Relational data store |
| Redis Cluster | 3-node cluster with failover | Cache, rate limiting, sessions |
| Kafka | 3 brokers, replication factor 3 | Event streaming |
| Container Registry | Private registry with vulnerability scanning | Image storage |
| API Gateway | Rate limiting, OAuth2 validation, request logging | Entry point |
| CDN | Static assets (design system, app bundles) | Performance |
| Secrets Manager | All credentials, API keys, encryption keys | Security |

---

## BL11: Security Configuration

| Control | Implementation | Scope |
|---|---|---|
| Encryption at rest | AES-256 (database-level + application-level for PII columns) | All PII |
| Encryption in transit | TLS 1.2+ mandatory | All communications |
| Password hashing | bcrypt (work factor 12) | Customer passwords |
| Rate limiting | Sliding window (Redis): 5 reg/IP/hr, 3 OTP/10min, 10 login/IP/hr | Public endpoints |
| Account lockout | 5 failed attempts → 30 min lockout | Login, OTP verification |
| CORS | Whitelist: app domains only | API Gateway |
| CSP Headers | script-src 'self'; style-src 'self' 'unsafe-inline' | Web app |
| Input validation | Server-side validation on all inputs, parameterised queries | All endpoints |
| PII logging | PII fields excluded from application logs | All services |
| Audit logging | All state changes logged with actor, timestamp, before/after | All write operations |

---

## BL12: Deployment Configuration

| Environment | Purpose | Deploy Method | URL Pattern |
|---|---|---|---|
| Development | Local development | Docker Compose | localhost:* |
| Staging | Integration testing, QA | Auto-deploy on merge to main | staging.loyalty.next.co.uk |
| Production | Live customers | Manual approval gate | loyalty.next.co.uk |

| Service | Replicas (Prod) | CPU | Memory | Health Check |
|---|---|---|---|---|
| auth-service | 3 | 500m | 512Mi | /health (liveness), /health/ready (readiness) |
| notification-service | 2 | 250m | 256Mi | /health |
| api-gateway | 3 | 500m | 512Mi | /health |
| web-app | 2 (behind CDN) | 250m | 256Mi | / (200 OK) |

---

## BL13: Service Implementation

| Service | Port | Tech Stack | Files | Tests | Sprint |
|---------|------|-----------|-------|-------|--------|
| auth-service | 8081 | Spring Boot 3.3, Java 21, PostgreSQL, Redis, JWT | 88 | 14 (4 unit + 6 unit + 4 contract) | 1 |
| notification-service | 8082 | Spring Boot 3.3, Java 21, PostgreSQL, Kafka, Resilience4j | 29 | 7 (4 unit + 3 contract) | 1 |
| loyalty-core-service | 8083 | Spring Boot 3.3, Java 21, PostgreSQL, Redis, Kafka | 145 | 11 (4+4 unit + 3 contract) | 2 |
| admin-service | 8084 | Spring Boot 3.3, Java 21, PostgreSQL, Spring Security | 64 | 5 (2 unit + 3 contract) | 3 |

**Total: 326 files, 37 tests across 4 services**

**Repository structure:**
```
services/
├── auth-service/          (Sprint 1 — registration, login, OTP, social auth, password reset)
├── notification-service/  (Sprint 1 — event-driven push/email delivery, in-app center)
├── loyalty-core-service/  (Sprint 2 — points earn/redeem, tiers, webhooks, staff, QR)
└── admin-service/         (Sprint 3 — dashboard, config, campaigns, audit)
```

**Architecture pattern:** Hexagonal (Ports & Adapters)
- domain/ → model, port, event, exception
- application/ → command, query, dto, mapper
- infrastructure/ → persistence, client, messaging, cache, config
- api/ → controller, dto, filter

**Key implementation decisions:**
- CQRS: Custom CommandHandler<C,R> / QueryHandler<Q,R> interfaces
- Persistence: Spring Data JPA + Flyway migrations
- Caching: Redis (cache-aside, 2s TTL for balance)
- Messaging: Kafka (Spring Kafka)
- Security: Spring Security OAuth2 Resource Server (JWT)
- Resilience: Resilience4j (circuit breaker, retry)
- Testing: JUnit 5 + Mockito (unit), Spring MockMvc (contract)

**Data ownership model (pragmatic for small team):**
- Single PostgreSQL instance, shared across services (per ADR-04 monorepo decision)
- Schema-level ownership: each service owns its tables (writes only to owned tables)
- Cross-service reads allowed via shared DB (e.g., loyalty-core reads customers table)
- `campaigns` table: admin-service owns CRUD, loyalty-core reads for earn-time multiplier
- `audit_logs` table: each service writes to its own audit_logs (partitioned by service)
- Future: extract to separate databases when team grows (strangler fig pattern)

---

## BL14: UI Implementation

| App | Tech Stack | Files | Tests | Screens | Sprint |
|-----|-----------|-------|-------|---------|--------|
| web-app | React 18.3, TypeScript 5.5, Vite, Tailwind, React Query, Zustand | 28 | 23 (5 test files) | 9 routes | 1-2 |
| mobile-app | React Native 0.74, Expo 51, TypeScript, React Navigation, React Query | 26 | 3 (3 test files) | 8 screens | 1-2 |

**Total: 54 UI files, 26 tests**

**Repository structure:**
```
ui/
├── web-app/           (React SPA — next.co.uk/loyalty)
│   ├── src/
│   │   ├── components/ui/     (Button, Input, Toast — design system)
│   │   ├── components/layout/ (AppShell, AuthGuard)
│   │   ├── pages/auth/        (Register, Login, VerifyOtp, ResetPassword)
│   │   ├── pages/home/        (Dashboard)
│   │   ├── pages/notifications/
│   │   ├── pages/history/
│   │   ├── pages/profile/
│   │   ├── pages/qr/
│   │   ├── hooks/             (useAuth, usePoints, useNotifications)
│   │   └── lib/               (api-client, auth-store)
│   ├── tailwind.config.ts     (Next brand design tokens)
│   └── Dockerfile
└── mobile-app/        (React Native — iOS + Android)
    ├── src/
    │   ├── components/ui/     (Button, Input, QRCode)
    │   ├── screens/auth/      (Register, Login, VerifyOtp)
    │   ├── screens/home/      (Dashboard, QR)
    │   ├── screens/notifications/
    │   ├── screens/profile/
    │   ├── navigation/        (AppNavigator, AuthNavigator)
    │   ├── hooks/             (useAuth, usePushNotifications)
    │   ├── lib/               (api-client, auth-store)
    │   └── theme/             (tokens, theme)
    └── Dockerfile
```

**Design system alignment:**
- Colors: #000000 (primary-black), #FFFFFF (primary-white), #007A7A (accent-teal), #FF6A3B (accent-orange)
- Typography: NEXT Display Sans (UI), NEXT Serif (body)
- Spacing: 4px base scale (space-1 through space-24)
- Border radius: 4px buttons/inputs, 2px cards
- Shadows: minimal (editorial aesthetic)
- Touch targets: 44px minimum
- Focus ring: 2px teal, 2px offset
- WCAG 2.1 AA compliant

**Key patterns:**
- JWT token rotation via Axios interceptor (single-flight refresh)
- Zustand for auth state (persisted to localStorage/SecureStore)
- React Query for server state (caching, background refetch)
- Zod + React Hook Form for validation
- Deep linking: loyalty://home|history|qr|profile
- Offline-aware: cached balance display when offline
