# Integration Architecture — Dunelm Loyalty Program

**Version:** 1.0.0 | **Status:** Greenfield | **Date:** 2026-05-18

---

## 1. Integration Landscape Diagram

```
                          ┌─────────────────────────────────────────────────────┐
                          │              EXTERNAL SYSTEMS                        │
                          │                                                     │
                          │  ┌──────────┐  ┌─────┐  ┌──────┐  ┌────────────┐  │
                          │  │ SendGrid │  │ FCM │  │ APNs │  │Google OAuth│  │
                          │  │  / SES   │  │     │  │      │  │            │  │
                          │  └────┬─────┘  └──┬──┘  └──┬───┘  └─────┬──────┘  │
                          │       │           │        │             │          │
                          │  ┌────┴───┐  ┌───┴────────┴──┐   ┌─────┴──────┐   │
                          │  │Apple   │  │  E-commerce   │   │ POS System │   │
                          │  │Sign In │  │  Platform     │   │            │   │
                          │  └───┬────┘  └──────┬────────┘   └─────┬──────┘   │
                          └──────┼──────────────┼──────────────────┼───────────┘
                                 │              │                   │
                    ─────────────┼──────────────┼───────────────────┼───────────
                                 │              │                   │
                                 ▼              ▼                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY                                        │
│         (Rate limiting, OAuth2 validation, CORS, Correlation ID)             │
└───────────────────────────────────┬──────────────────────────────────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │   Auth Service   │  │  Loyalty Core    │  │  Notification    │
   │                  │  │                  │  │  Service          │
   │ • Registration   │  │ • Points earn    │  │ • Email (SG/SES) │
   │ • Login/OAuth    │  │ • Points redeem  │  │ • Push (FCM/APNs)│
   │ • OTP            │  │ • Balance        │  │ • Templates      │
   └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
            │                     │                     │
            └─────────────────────┼─────────────────────┘
                                  ▼
                    ┌──────────────────────────┐
                    │     KAFKA EVENT BUS      │
                    │  (3 brokers, RF=3)       │
                    │                          │
                    │  Topics:                 │
                    │  • customer-events       │
                    │  • loyalty-events        │
                    │  • notification-commands │
                    │  • notification-events   │
                    └──────────────────────────┘
```

**Protocol Legend:**
- `→` Outbound from Loyalty | `←` Inbound to Loyalty
- REST = HTTPS/JSON | HTTP/2 = APNs binary | Kafka = TCP/binary

---

## 2. External Integrations Table

| System | Protocol | Direction | Auth | Data Exchanged | SLA | Sprint |
|--------|----------|-----------|------|----------------|-----|--------|
| Email (SendGrid/SES) | REST API (HTTPS) | Outbound | API key (header) | OTP codes, welcome email, points notifications | 99.9%, <2s delivery | Sprint 1 |
| Firebase Cloud Messaging | REST API (HTTPS) | Outbound | OAuth2 service account | Push notification payloads (title, body, data) | 99.95%, <1s | Sprint 1 |
| Apple Push Notification Service | HTTP/2 | Outbound | JWT (P8 key) | Push notification payloads (alert, badge, sound) | 99.95%, <1s | Sprint 1 |
| Google OAuth | OAuth 2.0/OIDC | Outbound | Client ID + Secret | ID token, profile (email, name) | 99.99%, <500ms | Sprint 1 |
| Apple Sign In | OAuth 2.0/OIDC | Outbound | Client ID + P8 key | ID token, profile (email, name) | 99.99%, <500ms | Sprint 1 |
| E-commerce Platform | HTTPS Webhook | Inbound | HMAC-SHA256 signature | Purchase events (order_id, items, total, customer_id) | Process <2s | Sprint 2 |
| POS System | REST API | Inbound | API key | In-store transactions (receipt_id, items, total, loyalty_id) | Process <2s | Sprint 2 |
| Kafka (Event Bus) | Kafka protocol (TCP) | Bidirectional | mTLS + SASL | Domain events (see §4) | 99.99%, <100ms | Sprint 1 |

---

## 3. Inbound Webhook Handling

### 3.1 `/api/v1/webhooks/ecommerce`

**Signature Verification:**
```
signature = HMAC-SHA256(webhook_secret, raw_request_body)
X-Webhook-Signature: sha256={signature}
```
- Reject if signature mismatch → HTTP 401
- Reject if timestamp > 5 min old → HTTP 401 (replay protection)

**Idempotency:**
- Dedup key: `SHA256(order_id + event_type)`
- Store in Redis with 72h TTL
- If duplicate → HTTP 200 (acknowledge, do not reprocess)

**Payload Schema:**
```json
{
  "event_type": "order.completed | order.refunded",
  "order_id": "string (unique)",
  "customer_id": "uuid",
  "total_amount": 99.99,
  "currency": "GBP",
  "items": [{"sku": "string", "qty": 1, "price": 49.99}],
  "channel": "web | app",
  "timestamp": "ISO-8601"
}
```

**Dead Letter Queue:** Failed events → `webhooks-dlq` Kafka topic (retain 7 days)

**Retry Policy:** Caller retries 3× with exponential backoff (1s, 5s, 30s). Platform returns HTTP 202 on accept, 5xx triggers caller retry.

---

### 3.2 `/api/v1/webhooks/pos`

**Authentication:** API key in `X-API-Key` header (per-store key rotation every 90 days)

**Idempotency:**
- Dedup key: `SHA256(receipt_id + store_id + event_type)`
- Store in Redis with 72h TTL

**Payload Schema:**
```json
{
  "event_type": "transaction.completed | transaction.refunded",
  "receipt_id": "string",
  "store_id": "string",
  "loyalty_id": "string (scanned QR/barcode)",
  "total_amount": 49.99,
  "currency": "GBP",
  "items": [{"sku": "string", "qty": 1, "price": 49.99}],
  "timestamp": "ISO-8601"
}
```

**Dead Letter Queue:** Same as e-commerce → `webhooks-dlq` topic

**Retry Policy:** POS retries 3× (2s, 10s, 60s). Offline fallback: batch file upload reconciliation nightly.

---

## 4. Event-Driven Architecture

**Kafka Configuration:** 3 brokers, replication factor 3, partition by `customer_id` (per-customer ordering)

| Topic | Event | Schema (key fields) | Publisher | Consumers | Ordering |
|-------|-------|---------------------|-----------|-----------|----------|
| customer-events | customer.registered | {customer_id, email, channel, timestamp} | Auth Service | Notification Service | customer_id |
| customer-events | customer.verified | {customer_id, timestamp} | Auth Service | — | customer_id |
| customer-events | customer.login | {customer_id, method, device, timestamp} | Auth Service | Audit | customer_id |
| notification-commands | notification.send | {customer_id, template_id, variables, channels} | Any service | Notification Service | customer_id |
| notification-events | notification.delivered | {notification_id, channel, timestamp} | Notification Service | Analytics | notification_id |
| notification-events | notification.failed | {notification_id, channel, reason, retry_count} | Notification Service | DLQ Handler | notification_id |
| loyalty-events | points.earned | {customer_id, points, reference_id, channel, campaign_id} | Loyalty Core | Notification Service, Analytics | customer_id |
| loyalty-events | points.redeemed | {customer_id, points, order_id, discount} | Loyalty Core | Notification Service | customer_id |
| loyalty-events | points.expired | {customer_id, points, expiry_date} | Loyalty Core (scheduler) | Notification Service | customer_id |
| loyalty-events | tier.changed | {customer_id, old_tier, new_tier, timestamp} | Loyalty Core | Notification Service, Marketing | customer_id |
| loyalty-events | campaign.triggered | {campaign_id, customer_id, multiplier, start, end} | Campaign Service | Loyalty Core, Notification Service | customer_id |

**Schema Format:** CloudEvents v1.0, JSON serialisation, backward-compatible schema evolution.

---

## 5. Circuit Breaker Configuration

| System | Failure Threshold | Break Duration | Fallback Behaviour |
|--------|-------------------|----------------|-------------------|
| Email (SendGrid/SES) | 5 failures / 30s | 60s | Queue to retry topic; log for manual send |
| FCM | 5 failures / 30s | 60s | Queue to retry topic; mark as pending |
| APNs | 5 failures / 30s | 60s | Queue to retry topic; mark as pending |
| Google OAuth | 3 failures / 30s | 30s | Return "social login temporarily unavailable" |
| Apple Sign In | 3 failures / 30s | 30s | Return "social login temporarily unavailable" |
| E-commerce Webhook (outbound calls) | N/A (inbound) | N/A | N/A |
| POS System (outbound calls) | N/A (inbound) | N/A | N/A |
| Kafka | 10 failures / 60s | 30s | Write to local disk buffer; replay on recovery |

**States:** CLOSED → OPEN (on threshold) → HALF-OPEN (after break duration, allow 1 probe) → CLOSED (on success)

---

## 6. Retry Policies

| System | Max Retries | Backoff Strategy | DLQ |
|--------|-------------|------------------|-----|
| Email (SendGrid/SES) | 5 | Exponential (1s, 2s, 4s, 8s, 16s) + jitter | `notifications-dlq` |
| FCM | 5 | Exponential (1s, 2s, 4s, 8s, 16s) + jitter | `notifications-dlq` |
| APNs | 5 | Exponential (1s, 2s, 4s, 8s, 16s) + jitter | `notifications-dlq` |
| Google OAuth | 2 | Fixed 1s | None (fail fast to user) |
| Apple Sign In | 2 | Fixed 1s | None (fail fast to user) |
| Kafka Produce | 3 | Exponential (100ms, 500ms, 2s) | Local disk buffer |
| Webhook Processing | 3 | Exponential (1s, 5s, 30s) | `webhooks-dlq` |
| Internal Service Calls | 2 | Fixed 500ms | None (return error to caller) |

---

## 7. Data Flow Diagrams

### 7.1 Points Accrual Flow

```
┌───────────┐    HTTPS     ┌───────────┐   Validate    ┌───────────┐
│E-commerce │───webhook───▶│  Webhook  │───signature──▶│  Dedup    │
│  / POS    │              │  Handler  │   + dedup     │  Check    │
└───────────┘              └───────────┘               └─────┬─────┘
                                                             │
                                                             ▼
┌───────────┐   Kafka      ┌───────────┐   Calculate   ┌───────────┐
│Notification│◀──event────│  Event     │◀──points─────│  Loyalty   │
│  Service  │  (points.   │  Publisher │   + write     │  Core     │
│           │   earned)   │            │   ledger      │           │
└───────────┘              └───────────┘               └───────────┘
```

### 7.2 Points Redemption Flow

```
┌──────────┐   REST    ┌─────┐   REST    ┌───────────┐   Lock     ┌──────────┐
│ Customer │──redeem──▶│ BFF │──validate─▶│  Loyalty  │──balance──▶│ Points   │
│ (App/Web)│           │     │           │  Core     │   deduct   │ Ledger   │
└──────────┘           └─────┘           └─────┬─────┘            └──────────┘
                                               │
                                               ▼ Kafka (points.redeemed)
                                         ┌───────────┐
                                         │Notification│
                                         │  Service   │
                                         └───────────┘
```

### 7.3 Notification Delivery Flow

```
┌───────────┐  Kafka    ┌───────────┐  Resolve   ┌───────────┐
│  Domain   │──event───▶│Notification│──template─▶│  Channel  │
│  Event    │           │  Consumer │            │  Router   │
└───────────┘           └───────────┘            └─────┬─────┘
                                                       │
                                          ┌────────────┼────────────┐
                                          ▼            ▼            ▼
                                    ┌──────────┐ ┌──────────┐ ┌──────────┐
                                    │  Email   │ │   FCM    │ │   APNs   │
                                    │(SG/SES) │ │(Android) │ │  (iOS)   │
                                    └────┬─────┘ └────┬─────┘ └────┬─────┘
                                         │            │            │
                                         ▼            ▼            ▼
                                    ┌──────────────────────────────────┐
                                    │  Delivery Tracking (Kafka event) │
                                    │  notification.delivered / .failed│
                                    └──────────────────────────────────┘
```

---

## 8. API Gateway Configuration

| Concern | Configuration |
|---------|--------------|
| Rate Limiting | 1000 req/min global; per-endpoint: 5 reg/IP/hr, 3 OTP/10min, 10 login/IP/hr |
| Auth Validation | JWT verification (RS256), API key validation, refresh token rotation |
| CORS | Whitelist: `loyalty.Dunelm.co.uk`, `staging.loyalty.Dunelm.co.uk`, mobile deep-link schemes |
| Request Logging | Structured JSON: method, path, status, latency, client_ip (PII excluded from body) |
| Correlation ID | Inject `X-Correlation-ID` (UUID v4) if not present; propagate to all downstream calls |
| Request Size | Max body: 1MB (webhooks), 256KB (standard API) |
| Timeout | 30s gateway timeout; 5s upstream timeout |
| TLS | TLS 1.2+ only; HSTS enabled |
| Versioning | URL path: `/api/v1/` |
| Error Format | `{error_code, message, details, trace_id}` |

---

## 9. Inter-Service Communication

| Caller | Callee | Protocol | Timeout | Circuit Breaker | Auth | Purpose |
|--------|--------|----------|---------|-----------------|------|---------|
| BFF | Auth Service | REST (internal) | 5s | Yes (3 failures/10s → 10s break) | Internal mTLS | Login, register, token refresh |
| BFF | Loyalty Core | REST (internal) | 5s | Yes (3 failures/10s → 10s break) | Internal mTLS | Balance, earn, redeem, transactions |
| BFF | Notification Service | REST (internal) | 5s | Yes (3 failures/10s → 10s break) | Internal mTLS | Get notifications, mark read |
| Loyalty Core | Auth Service | REST (internal) | 3s | Yes | Internal mTLS | Customer validation |
| Any Service | Kafka | Kafka protocol | 10s produce | Local buffer fallback | mTLS + SASL | Event publish/consume |

**Service Discovery:** Kubernetes DNS (`<service>.<namespace>.svc.cluster.local`)

**Retry:** 2 retries, 500ms fixed backoff, fail fast on 4xx (no retry).

---

*End of document.*
