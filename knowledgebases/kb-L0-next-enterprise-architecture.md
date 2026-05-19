# Dunelm plc — Enterprise Architecture Knowledge Base
## kb-L0-Dunelm-enterprise-architecture v1.0.0

**Purpose:** Reference architecture for engineering teams building on Dunelm's platform
**Classification:** Internal
**Confidence:** ⚠️ Inferred from business context, public information, and standard retail enterprise patterns. Dunelm does not publicly disclose its full technology stack. Sections marked [Confirmed] are from the project context document; sections marked [Inferred] are based on standard patterns for UK retailers of this scale.

---

## 1. Architecture Principles

| # | Principle | Rationale |
|---|-----------|-----------|
| AP-1 | Channel-agnostic services | Business logic must not be tied to a single channel. Services serve web, app, POS, and third-party brands equally. |
| AP-2 | Customer-centric data model | Single customer identity across all channels. Solve the data silo problem (§5.8 of domain KB). |
| AP-3 | API-first design | All capabilities exposed as APIs. Enables Total Platform (third-party brands consume same APIs as Dunelm's own channels). |
| AP-4 | Platform thinking | Build once, deploy across Dunelm own-brand and Total Platform brands. Shared infrastructure, brand-specific configuration. |
| AP-5 | Progressive enhancement | Core functionality works everywhere; enhanced experiences for modern browsers/devices. |
| AP-6 | Security by design | PII encrypted, RBAC enforced, audit trails on all customer data access. UK GDPR compliance built-in. |
| AP-7 | Resilience over perfection | Graceful degradation preferred over hard failures. Retail cannot afford downtime during peak trading. |

---

## 2. System Landscape

### 2.1 Core Systems [Inferred from business context]

| System Domain | Purpose | Key Integrations |
|--------------|---------|-----------------|
| **E-commerce Platform** | Dunelm.co.uk — product catalogue, search, checkout, account management | Inventory, Payments, Customer, CMS |
| **Mobile App** | Native iOS/Android — personalised experience, barcode scanning, Click & Collect | E-commerce APIs, Push notifications, Customer |
| **POS System** | In-store transactions, Click & Collect fulfilment, returns processing | Inventory, Payments, Customer, Loyalty |
| **Order Management System (OMS)** | Order lifecycle: placement → fulfilment → delivery → returns | Inventory, Warehouse, Logistics, Customer |
| **Inventory Management** | Stock levels across 450+ stores, warehouses, and online | OMS, POS, E-commerce, Supply Chain |
| **Customer Data Platform (CDP)** | Unified customer profiles (currently siloed — key challenge) | All customer-facing systems |
| **Dunelm Finance System** | Credit account management, payments, statements | Customer, E-commerce, POS |
| **Total Platform** | Multi-tenant infrastructure for third-party brands | E-commerce, OMS, Warehouse, Logistics |
| **Content Management System (CMS)** | Product content, editorial, style inspiration | E-commerce, Mobile App |
| **Warehouse Management System (WMS)** | Picking, packing, dispatch across distribution centres | OMS, Inventory, Logistics |
| **Marketing & CRM** | Email campaigns, personalisation, customer segmentation | CDP, E-commerce, Mobile App |

### 2.2 System Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CUSTOMER CHANNELS                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  │
│  │ Website  │  │Mobile App│  │  Stores  │  │ Third-Party Brands│  │
│  │(Dunelm.co.uk)│ │(iOS/And) │  │  (POS)   │  │ (Total Platform) │  │
│  └─────┬────┘  └─────┬────┘  └─────┬────┘  └────────┬──────────┘  │
└────────┼──────────────┼─────────────┼────────────────┼──────────────┘
         │              │             │                │
         ▼              ▼             ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY / BFF                             │
│         (Channel-specific backends-for-frontends)                     │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Commerce Core  │  │  Customer Core  │  │  Operations Core│
│                 │  │                 │  │                 │
│ • Catalogue     │  │ • Identity      │  │ • OMS           │
│ • Search        │  │ • Profiles      │  │ • Inventory     │
│ • Pricing       │  │ • Preferences   │  │ • Warehouse     │
│ • Checkout      │  │ • Dunelm Finance  │  │ • Logistics     │
│ • Promotions    │  │ • Loyalty ←NEW  │  │ • Returns       │
│ • Basket        │  │ • Consent       │  │ • Click&Collect │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DATA & ANALYTICS PLATFORM                        │
│  • Data Warehouse  • Customer 360  • Personalisation Engine          │
│  • Reporting       • ML Models     • Real-time Events                │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack [Inferred]

### 3.1 Frontend

| Layer | Technology | Confidence |
|-------|-----------|------------|
| Website | Modern SPA or SSR framework (likely React/Dunelm.js given scale) | ⚠️ Inferred |
| Mobile App | Native (iOS: Swift, Android: Kotlin) or cross-platform | ⚠️ Inferred |
| POS | Proprietary or vendor POS software | ⚠️ Inferred |
| Design System | Shared component library across web and app | ⚠️ Inferred |

### 3.2 Backend

| Layer | Technology | Confidence |
|-------|-----------|------------|
| API Style | RESTful APIs (likely GraphQL for BFF layer) | ⚠️ Inferred |
| Languages | Java/Kotlin, Node.js, Python (ML) | ⚠️ Inferred |
| Architecture | Microservices (required for Total Platform multi-tenancy) | ⚠️ Inferred |
| Messaging | Event-driven (Kafka or cloud-native equivalent) | ⚠️ Inferred |
| Database | Relational (PostgreSQL/Oracle) + NoSQL (for catalogue/sessions) | ⚠️ Inferred |
| Caching | Redis/Memcached (product pages, sessions, inventory counts) | ⚠️ Inferred |
| Search | Elasticsearch/Solr (product search, autocomplete) | ⚠️ Inferred |

### 3.3 Infrastructure

| Layer | Technology | Confidence |
|-------|-----------|------------|
| Cloud | AWS or Azure (UK retailers typically use one of these) | ⚠️ Inferred |
| Container Orchestration | Kubernetes (required for Total Platform scale) | ⚠️ Inferred |
| CDN | CloudFront/Akamai (product images, static assets) | ⚠️ Inferred |
| CI/CD | Jenkins/GitHub Actions/GitLab CI | ⚠️ Inferred |
| IaC | Terraform or CloudFormation | ⚠️ Inferred |
| Monitoring | Datadog/New Relic/Dynatrace | ⚠️ Inferred |

### 3.4 Third-Party Services [Inferred]

| Service | Purpose |
|---------|---------|
| Payment Gateway | Card processing (Adyen/Worldpay/Stripe) |
| Delivery Partners | Royal Mail, DPD, Hermes/Evri for home delivery |
| CRA (Credit Reference) | Experian/Equifax for Dunelm Finance credit checks |
| Email/SMS | Transactional and marketing communications |
| Push Notifications | FCM (Android), APNs (iOS) |
| Analytics | Google Analytics, Adobe Analytics |
| Personalisation | Recommendation engine (internal or vendor like Dynamic Yield) |

---

## 4. Integration Patterns

### 4.1 Synchronous (Request/Response)

| Pattern | Use Case | Protocol |
|---------|----------|----------|
| REST API | Channel → Service communication | HTTPS/JSON |
| GraphQL BFF | Mobile app → aggregated data queries | HTTPS/GraphQL |
| gRPC | Internal service-to-service (high performance) | HTTP/2 + Protobuf |

### 4.2 Asynchronous (Event-Driven)

| Pattern | Use Case | Technology |
|---------|----------|-----------|
| Domain Events | Order placed, payment received, stock updated | Kafka/SQS/EventBridge |
| CQRS | Separate read/write models for high-traffic reads (product pages) | Event sourcing + read replicas |
| Webhooks | Third-party brand notifications (Total Platform) | HTTPS callbacks |
| Batch | Nightly inventory reconciliation, financial reporting | Scheduled jobs |

### 4.3 Integration with Loyalty System (New)

| Integration Point | Direction | Pattern | Data |
|-------------------|-----------|---------|------|
| E-commerce checkout | → Loyalty | Sync API | Earn points on purchase |
| POS transaction | → Loyalty | Sync API | Earn points in-store |
| Loyalty → Checkout | ← Loyalty | Sync API | Redeem points as discount |
| Customer registration | → Loyalty | Event | New loyalty account |
| Returns/refunds | → Loyalty | Event | Reverse points |
| Dunelm Finance payment | → Loyalty | Event | Earn on credit purchase |
| Marketing campaigns | ← Loyalty | Event | Tier changes, milestones |
| Store associate POS | ← Loyalty | Sync API | Customer lookup, status |

---

## 5. Security Architecture

### 5.1 Security Layers

| Layer | Implementation |
|-------|---------------|
| Network | WAF, DDoS protection, private subnets for backend services |
| Identity (Customer) | OAuth 2.0 / OpenID Connect, MFA for account changes |
| Identity (Staff) | SSO via corporate directory (Active Directory/Okta) |
| Identity (Total Platform) | API keys + OAuth2 client credentials for brand integrations |
| Data at Rest | AES-256 encryption for PII and financial data |
| Data in Transit | TLS 1.2+ for all communications |
| Application | Input validation, parameterised queries, CORS, CSP headers |
| PCI DSS | Card data handled by payment gateway (SAQ-A or SAQ-A-EP) |
| Audit | All access to customer PII logged with actor, timestamp, purpose |

### 5.2 Authentication Patterns

| Actor | Method | Session |
|-------|--------|---------|
| Customer (web) | Email/password + optional MFA | JWT, 7-day refresh |
| Customer (app) | Email/password + biometric | JWT, 30-day refresh |
| Customer (social) | Google/Apple/Facebook OAuth | JWT, linked to account |
| Store associate | Employee ID + SSO | Short-lived (shift-based) |
| Total Platform brand | API key + client credentials | Per-request token |
| Admin/ops | SSO + MFA mandatory | Short-lived, role-based |

### 5.3 Data Classification

| Classification | Examples | Controls |
|---------------|----------|----------|
| Restricted | Payment card data, Dunelm Finance credit data, passwords | PCI DSS, HSM, never stored in app |
| Confidential | Name, email, phone, address, purchase history, loyalty balance | Encrypted at rest, RBAC, audit logged |
| Internal | Product data, pricing rules, inventory levels, staff data | Access controlled, not public |
| Public | Published product catalogue, store locations, T&Cs | No restrictions |

---

## 6. Data Architecture

### 6.1 Current State (Data Silos) [Confirmed]

The project context explicitly states data silos as the root cause of experience gaps:

| Silo | Data | Owner Team |
|------|------|-----------|
| Customer accounts | Registration, login, preferences | Digital Product |
| Transaction history | Online orders, in-store purchases | Separate per channel |
| Credit accounts | Dunelm Finance balances, payments, credit limits | Dunelm Finance |
| Inventory | Stock levels per location | Supply Chain |
| CRM/Marketing | Segments, campaign history, email engagement | Marketing |

**Problem:** No unified customer view. Store associates cannot see online activity. Online cannot see in-store purchases. Dunelm Finance data isolated.

### 6.2 Target State (Unified Customer Platform)

```
┌─────────────────────────────────────────────────┐
│           UNIFIED CUSTOMER PLATFORM              │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Identity │  │ Profile  │  │ Preferences  │  │
│  │ (single  │  │ (merged  │  │ (channel,    │  │
│  │  ID)     │  │  history)│  │  comms, etc) │  │
│  └──────────┘  └──────────┘  └──────────────┘  │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Loyalty  │  │  Dunelm    │  │  Consent &   │  │
│  │ (points, │  │ Finance  │  │  Privacy     │  │
│  │  tiers)  │  │ (credit) │  │  Management  │  │
│  └──────────┘  └──────────┘  └──────────────┘  │
└─────────────────────────────────────────────────┘
         │              │              │
         ▼              ▼              ▼
    All channels access unified customer data
    via Customer Core APIs
```

### 6.3 Key Data Entities

| Entity | Key Attributes | Source of Truth |
|--------|---------------|-----------------|
| Customer | id, name, email, phone, addresses, preferences | Customer Core |
| Loyalty Account | customer_id, points_balance, tier, member_since | Loyalty Service (NEW) |
| Transaction | id, customer_id, items, total, channel, store_id, timestamp | OMS |
| Product | sku, name, category, price, images, stock_levels | Commerce Core |
| Order | id, customer_id, items, status, delivery_method, tracking | OMS |
| Credit Account | customer_id, balance, limit, payment_history | Dunelm Finance |
| Consent | customer_id, purpose, status, timestamp, version | Consent Service |

---

## 7. Scalability & Performance

### 7.1 Traffic Patterns [Inferred from scale]

| Metric | Estimate | Peak |
|--------|----------|------|
| Daily active users (web + app) | 2-5M | 10M+ (Black Friday, Boxing Day) |
| Orders per day | 200K-500K | 1M+ (peak trading) |
| Product page views per day | 50M-100M | 200M+ (peak) |
| API calls per second (steady) | 5,000-10,000 | 50,000+ (peak) |
| Store transactions per day | 500K-1M (450+ stores) | 2M+ (Christmas) |

### 7.2 Performance Targets

| Operation | Target | Rationale |
|-----------|--------|-----------|
| Product page load | < 2s (LCP) | SEO + conversion |
| Search results | < 500ms | User expectation |
| Checkout completion | < 3s | Cart abandonment |
| Loyalty balance query | < 500ms | Real-time display |
| Points accrual | < 2s | Customer confidence |
| POS loyalty lookup | < 2s | Queue time |
| App cold start | < 3s | Engagement |

### 7.3 Availability Targets

| System | Target | Justification |
|--------|--------|---------------|
| E-commerce (web + app) | 99.95% | Revenue-critical |
| POS | 99.99% | Stores cannot operate without POS |
| Loyalty Service | 99.9% | Important but not revenue-blocking |
| Total Platform | 99.95% | SLA with third-party brands |
| Dunelm Finance | 99.9% | Financial services requirement |

---

## 8. Total Platform Architecture [Confirmed concept, inferred detail]

Total Platform is Dunelm's infrastructure-as-a-service offering for third-party brands:

### 8.1 Multi-Tenancy Model

```
┌─────────────────────────────────────────────┐
│           TOTAL PLATFORM                     │
│                                              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │  Dunelm   │  │  Reiss  │  │ FatFace │... │
│  │ (tenant)│  │ (tenant)│  │ (tenant)│    │
│  └────┬────┘  └────┬────┘  └────┬────┘    │
│       │             │             │          │
│       ▼             ▼             ▼          │
│  ┌──────────────────────────────────────┐   │
│  │     SHARED SERVICES LAYER            │   │
│  │  • E-commerce engine                 │   │
│  │  • Order management                  │   │
│  │  • Warehouse & fulfilment            │   │
│  │  • Payment processing                │   │
│  │  • Customer service tools            │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### 8.2 Loyalty Implications for Total Platform
- Should customers earn loyalty points on third-party brand purchases? (design decision)
- If yes: Total Platform checkout must call Loyalty API
- Brand-specific earn rates possible (e.g., 1 point/£1 for Dunelm, 0.5 points/£1 for third-party)
- Customer identity must span Dunelm own-brand and Total Platform brands

---

## 9. Key Architectural Decisions for Loyalty Service

### 9.1 Where Loyalty Fits

The loyalty service is a **new bounded context** within Customer Core:

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Deployment | Separate microservice(s) | Independent scaling, independent release cycle |
| Data store | Own database (PostgreSQL) | Financial ledger requires ACID; no shared DB |
| Integration | Event-driven + sync APIs | Events for earn (async OK), sync for redeem (must be real-time) |
| Identity | Uses existing customer_id | No separate loyalty ID — customer is already identified |
| Multi-channel | Single API serves all channels | Web, app, POS, Total Platform all use same loyalty APIs |

### 9.2 API Design Standards

| Standard | Specification |
|----------|--------------|
| Style | RESTful, resource-oriented |
| Versioning | URL path (/v1/, /v2/) |
| Authentication | OAuth 2.0 Bearer tokens (customer), API keys (POS/Total Platform) |
| Rate limiting | Per-client, configurable |
| Error format | {error_code, message, details, trace_id} |
| Pagination | Cursor-based for lists |
| Idempotency | Idempotency-Key header for write operations |
| Documentation | OpenAPI 3.0 spec |

### 9.3 Event Schema Standards

| Standard | Specification |
|----------|--------------|
| Format | CloudEvents v1.0 |
| Serialisation | JSON |
| Schema registry | Versioned schemas, backward compatible |
| Naming | {domain}.{entity}.{action} (e.g., loyalty.points.earned) |
| Ordering | Partition by customer_id (ensures per-customer ordering) |

---

## 10. Observability Standards

| Aspect | Standard |
|--------|----------|
| Logging | Structured JSON, correlation_id on every log line |
| Metrics | RED method (Rate, Errors, Duration) for all services |
| Tracing | Distributed tracing with W3C Trace Context propagation |
| Alerting | SLO-based (error budget alerts, not threshold alerts) |
| Dashboards | Per-service health + business metrics (orders, points, sign-ups) |
| Retention | Logs: 90 days hot, 1 year cold. Metrics: 13 months. Traces: 7 days. |

---

## 11. Compliance & Governance

### 11.1 Regulatory Landscape

| Regulation | Scope | Impact on Architecture |
|-----------|-------|----------------------|
| UK GDPR / DPA 2018 | All customer PII | Consent management, right to erasure, data portability, breach notification |
| PECR | Marketing communications | Opt-in for promotional push/email/SMS |
| Consumer Rights Act 2015 | Loyalty T&Cs | Fair terms, transparent points value |
| PCI DSS | Payment card data | Tokenisation, SAQ compliance, no card data in loyalty system |
| FCA (if Dunelm Finance linked) | Credit-related loyalty incentives | Must not encourage irresponsible borrowing |
| Equality Act 2010 | Accessibility | WCAG 2.1 AA for all customer-facing interfaces |
| Modern Slavery Act 2015 | Supply chain | Not directly relevant to loyalty but applies to Dunelm overall |

### 11.2 Data Retention Policy

| Data Type | Retention | Basis |
|-----------|-----------|-------|
| Customer PII | Active account + 3 years post-closure | Legitimate interest |
| Transaction history | 7 years | Tax/accounting requirements |
| Loyalty points ledger | Active account + 7 years | Financial audit |
| Consent records | Indefinite (proof of consent) | UK GDPR Art. 7 |
| Audit logs | 7 years | Compliance evidence |
| Marketing preferences | Until withdrawn | PECR |

---

## 12. DevOps & Engineering Practices [Inferred]

| Practice | Standard |
|----------|----------|
| Source control | Git (GitHub/GitLab) |
| Branching | Trunk-based development or GitFlow |
| CI/CD | Automated build → test → deploy pipeline |
| Testing | Unit (>80%), integration, contract, E2E, performance |
| Code review | Mandatory peer review before merge |
| Environments | Dev → Staging → Pre-prod → Production |
| Feature flags | Progressive rollout for new features |
| Incident management | On-call rotation, runbooks, post-incident reviews |
| Change management | CAB for production changes during peak trading freeze |

### 12.1 Peak Trading Considerations
- **Code freeze:** Typically 2-4 weeks before Black Friday through January sales
- **Change advisory:** All production changes require approval during peak
- **Capacity planning:** Infrastructure scaled up 2 weeks before peak events
- **War room:** Dedicated incident response during peak trading days

---

## 13. Architectural Asymmetry & Technical Debt [Confirmed]

### 13.1 Two-Speed Architecture

The project context explicitly states: *"The Total Platform infrastructure is sophisticated for third-party brands but the internal customer experience systems have not always kept pace."*

This reveals a **two-speed architecture**:

| Layer | Maturity | Evidence |
|-------|----------|----------|
| Total Platform (third-party brands) | Modern, API-driven, multi-tenant | Supports Reiss, FatFace, Joules at scale |
| Internal customer systems | Legacy, siloed, inconsistent | Data silos, no cross-channel recognition |
| E-commerce (Dunelm.co.uk) | Mature, high-traffic | 60%+ of sales, millions of users |
| Store systems (POS) | Functional but disconnected | Cannot access online customer context |
| Mobile app | Exists but underutilised | Customers default to mobile web |

### 13.2 Organisational Misalignment

*"Systems are not always aligned across these teams"* — this is an architectural constraint:

- **Digital Product** owns web/app but not store systems
- **Retail Operations** owns stores but has no access to digital data
- **Dunelm Finance** operates independently with its own customer data
- **Total Platform** has modern infrastructure but serves external brands, not internal needs

**Implication for loyalty:** The loyalty service must bridge these organisational boundaries. It cannot be owned by a single team — it touches Digital Product (app/web), Retail Operations (POS), Dunelm Finance (credit), and Marketing (campaigns).

### 13.3 Legacy System Indicators

| System | Legacy Signal | Impact |
|--------|-------------|--------|
| Dunelm Directory | "Home shopping catalogue" — predates modern e-commerce | May have separate customer database, separate order system |
| POS | Store staff "lack tools to access customer context" | POS likely cannot call modern APIs without middleware |
| Customer data | "Sit in separate systems" | No unified customer ID across all systems today |
| Inventory | Separate from customer/transaction data | Real-time stock visibility may be limited |

---

## 14. Key Operational Flows

### 14.1 Click & Collect Flow

```
Customer places order online (selects Click & Collect)
    │
    ▼
OMS routes order to selected store
    │
    ▼
Store receives pick list → associate picks items
    │
    ▼
Customer notified: "Ready for collection"
    │
    ▼
Customer visits store → identifies self (order number/email)
    │
    ▼
Associate hands over items → order marked as collected
    │
    ▼
[Opportunity: customer browses store, makes additional purchase]
```

**Loyalty integration points:**
- Points earned on original online order (at order completion or collection?)
- Bonus points for choosing Click & Collect (incentivise store footfall)
- Store associate sees loyalty status during collection handover
- Additional in-store purchase earns points via POS

### 14.2 Cross-Channel Return Flow

```
Customer purchases online → receives item via delivery
    │
    ▼
Customer visits store to return (faster than postal)
    │
    ▼
Store associate processes return at POS
    │
    ▼
Refund issued to original payment method
    │
    ▼
[Loyalty: points earned on original purchase must be reversed]
```

### 14.3 Delivery Orchestration

| Method | Speed | Integration |
|--------|-------|-------------|
| Standard delivery | 3-5 days | Carrier API (Royal Mail/Evri) |
| Dunelm-day delivery | Dunelm working day | Premium carrier API |
| Same-day delivery | Same day (limited areas) | Local courier/gig economy API |
| Click & Collect | 1-2 days to store | OMS → Store allocation |
| Locker pickup | 1-2 days to locker | Locker network API (InPost/Amazon) |

### 14.4 In-App Barcode Scanning

Mobile app feature allowing customers to scan product barcodes in-store:
- Scans barcode → calls Product API with SKU
- Returns: price, available sizes, colours, stock in this store, online stock
- Enables: price check, size availability, "buy online for delivery" if not in stock
- **Integration:** App → Product/Inventory API (real-time)

---

## 15. Cross-Channel Data Entities (Currently Siloed)

| Entity | Where It Lives Today | Who Needs It | Gap |
|--------|---------------------|-------------|-----|
| Wishlist | Website/app only | Store associates | Staff cannot see what customer saved online |
| Browsing history | Website analytics | App, store associates | Not shared across channels |
| Purchase history | Separate per channel (online vs POS) | All channels | No unified view |
| Credit account | Dunelm Finance system | Website, POS, loyalty | Isolated from other customer data |
| Loyalty status | Does not exist yet | All channels | NEW — the loyalty program creates this |
| Communication preferences | Marketing CRM | All channels | May not be consistent |
| Delivery preferences | Order system | Checkout, app | Not personalised across channels |

**Architectural implication:** The loyalty program is an opportunity to create the **first truly cross-channel customer identifier**. If designed correctly, it becomes the foundation for solving the broader data silo problem — not just for loyalty, but for wishlist sharing, browsing history, and personalisation.

---

## 16. Grounding & Confidence

| Section | Confidence | Basis |
|---------|-----------|-------|
| Architecture principles | ⚠️ Inferred | Standard for UK retailers at this scale with platform business |
| System landscape | ⚠️ Inferred | Derived from stated channels, challenges, and Total Platform |
| Data silos (current state) | ✅ Confirmed | Explicitly stated in project context document |
| Total Platform concept | ✅ Confirmed | Explicitly stated — leases infrastructure to third-party brands |
| Channels (web, app, stores, C&C) | ✅ Confirmed | Explicitly stated in project context |
| Internal teams | ✅ Confirmed | Explicitly stated in project context |
| Technology stack specifics | ⚠️ Inferred | Based on standard patterns; Dunelm does not publicly disclose |
| Security patterns | ⚠️ Inferred | Standard for UK retail handling PII and payments |
| Performance targets | ⚠️ Inferred | Industry standard for retail at this scale |
| Regulatory landscape | ✅ Confirmed (regulations exist) | Application to Dunelm inferred |
