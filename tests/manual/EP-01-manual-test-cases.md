# EP-01: Platform Foundation & Design System — Manual Integration Test Cases

**Epic:** EP-01 — Platform Foundation & Design System
**Total Stories:** 14 (US-001 to US-014)
**Created:** 2026-05-18
**Test Environment:** Staging
**Test Personas:** Clara (customer), Nathan (customer), Sana (store associate)

---

## US-001: Cloud Infrastructure Provisioning

### TC-01: Kubernetes cluster provisioning and accessibility [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer (kubectl access configured)
- Data setup: Terraform configuration files committed to main branch

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Apply Terraform configuration | `terraform apply -auto-approve` | Apply completes without errors |
| 2 | Verify cluster node count | `kubectl get nodes` | 3+ nodes in Ready state |
| 3 | Verify cluster API accessibility | `kubectl cluster-info` | Cluster endpoint responds with control plane URLs |
| 4 | Deploy a test pod | `kubectl run test-pod --image=nginx --restart=Never` | Pod reaches Running state within 60s |
| 5 | Delete test pod | `kubectl delete pod test-pod` | Pod removed successfully |

**Postconditions:** Kubernetes cluster running with 3+ healthy nodes

---

### TC-02: PostgreSQL database provisioning with encryption [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer (DB admin credentials)
- Data setup: Database provisioning scripts available

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Run database provisioning script | `./scripts/provision-db.sh staging` | Script completes without errors |
| 2 | Connect to PostgreSQL instance | `psql -h $DB_HOST -U admin -d loyalty` | Connection established successfully |
| 3 | Verify customers table exists | `\dt customers` | Table listed with correct schema |
| 4 | Verify points_ledger table exists | `\dt points_ledger` | Table listed with correct schema |
| 5 | Verify tiers table exists | `\dt tiers` | Table listed with correct schema |
| 6 | Verify encryption at rest | Check Cloud Console > SQL > Instance > Encryption | AES-256 encryption enabled |

**Postconditions:** PostgreSQL available with all required tables, encrypted at rest

---

### TC-03: Redis cache cluster with failover [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Redis provisioning completed via IaC

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Verify Redis cluster is running | `kubectl get pods -l app=redis` | Redis pods in Running state |
| 2 | Connect to Redis from within cluster | `kubectl exec -it test-pod -- redis-cli -h redis-master PING` | Returns PONG |
| 3 | Write a test key | `SET test:key "hello" EX 60` | OK |
| 4 | Read the test key | `GET test:key` | Returns "hello" |
| 5 | Simulate primary failure | Kill primary Redis pod | Failover completes, replica promoted within 30s |
| 6 | Verify data accessible after failover | `GET test:key` | Returns "hello" from new primary |

**Postconditions:** Redis cluster accessible with failover confirmed

---

### TC-04: Event streaming topics provisioned [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Kafka/Pub-Sub provisioning completed

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | List available topics | `kafka-topics --list` or `gcloud pubsub topics list` | Topic list returned |
| 2 | Verify points.earned topic exists | Check topic list | points.earned topic present |
| 3 | Verify points.redeemed topic exists | Check topic list | points.redeemed topic present |
| 4 | Verify points.expired topic exists | Check topic list | points.expired topic present |
| 5 | Publish test message to points.earned | `{"customer_id": "test-001", "points": 100}` | Message published successfully |
| 6 | Consume test message from points.earned | Subscribe and read | Message received with correct payload |

**Postconditions:** All event streaming topics operational

---

### TC-05: Security — Infrastructure secrets not exposed [P1] [@regression @security]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Infrastructure fully provisioned

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Verify DB credentials stored in secret manager | Check GCP Secret Manager / AWS Secrets Manager | Connection strings stored as secrets, not plaintext |
| 2 | Verify Kubernetes secrets are encrypted | `kubectl get secrets -o yaml` | Secrets are opaque, not base64-visible in etcd |
| 3 | Verify Terraform state does not contain plaintext secrets | Inspect remote state | Sensitive values marked as sensitive |
| 4 | Attempt to access DB without credentials | `psql -h $DB_HOST -U anonymous -d loyalty` | Connection refused / authentication failed |

**Postconditions:** All infrastructure secrets properly secured

---

## US-002: API Gateway Setup with Authentication & Rate Limiting

### TC-01: Unauthenticated request rejected [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Unauthenticated client
- Data setup: API gateway deployed and running

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send GET request without auth token | `GET /v1/loyalty/points` with no Authorization header | HTTP 401 Unauthorized |
| 2 | Verify response body has no data leakage | Inspect response JSON | Body contains only error message, no internal details or stack traces |
| 3 | Send request with expired token | `Authorization: Bearer expired_token_abc123` | HTTP 401 Unauthorized |
| 4 | Send request with malformed token | `Authorization: Bearer not-a-valid-jwt` | HTTP 401 Unauthorized |

**Postconditions:** No data exposed to unauthenticated clients

---

### TC-02: Rate limiting enforced at 1000 req/min [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Authenticated API client (valid OAuth2 token)
- Data setup: Rate limit configured at 1000 req/min

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send 1000 requests within 1 minute | `GET /v1/health` with valid token, looped 1000 times | All return HTTP 200 |
| 2 | Send the 1001st request | Same endpoint, same minute window | HTTP 429 Too Many Requests |
| 3 | Verify Retry-After header present | Inspect response headers | `Retry-After` header present with seconds value |
| 4 | Wait for rate limit window to reset | Wait until Retry-After period elapses | Next request returns HTTP 200 |

**Postconditions:** Rate limiting correctly enforced and recoverable

---

### TC-03: Request/response logging with correlation ID [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Authenticated API client
- Data setup: Logging infrastructure (US-004) deployed

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send authenticated request | `GET /v1/health` with valid OAuth2 token | HTTP 200 |
| 2 | Extract correlation_id from response headers | Check `X-Correlation-ID` header | UUID-format correlation ID present |
| 3 | Search logs for the correlation_id | Query logging system with correlation_id | Log entry found |
| 4 | Verify log fields | Inspect log entry | Contains: correlation_id, timestamp, method, path, status_code, latency |
| 5 | Verify no request body logged for Confidential endpoints | Send POST to a Confidential endpoint, check logs | Request body NOT present in log entry |

**Postconditions:** All requests logged with required fields

---

### TC-04: Health endpoint responds within 100ms [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Any (health endpoint may be public)
- Data setup: API gateway running

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send GET /health request | `GET /health` | HTTP 200 |
| 2 | Verify response contains service status | Inspect response body | JSON with service status information |
| 3 | Measure response latency | Record time from request to response | Latency < 100ms |
| 4 | Repeat 10 times | Send 10 sequential requests | All respond 200 within 100ms |

**Postconditions:** Health endpoint consistently responsive

---

## US-003: API Versioning & Standardized Error Responses

### TC-01: API versioning routes correctly [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Authenticated API client
- Data setup: At least one service deployed behind gateway

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send request with /v1/ prefix | `GET /v1/health` | HTTP 200, routes to current service version |
| 2 | Verify response is from correct version | Check response payload or version header | Response from v1 handler |
| 3 | Send request without version prefix | `GET /health` | Either redirects to /v1/ or returns 404 with standard error |

**Postconditions:** Versioned routing operational

---

### TC-02: Standardized error response format [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Authenticated API client
- Data setup: API gateway and services running

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger a validation error | `POST /v1/loyalty/points` with invalid payload `{"amount": "abc"}` | HTTP 400 |
| 2 | Verify error response format | Inspect response body | Contains: error_code, message, details, trace_id |
| 3 | Trigger a server error (if test endpoint available) | `GET /v1/test/error` | HTTP 500 with same error format |
| 4 | Verify trace_id is a valid identifier | Check trace_id field | Non-empty string, UUID or similar format |

**Postconditions:** All errors follow standardized format

---

### TC-03: Non-existent endpoint returns standard 404 [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Authenticated API client
- Data setup: API gateway running

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Send request to non-existent endpoint | `GET /v1/this-does-not-exist` | HTTP 404 |
| 2 | Verify response is NOT a raw framework error | Inspect response body | No HTML error page, no framework stack trace |
| 3 | Verify standard error format | Check JSON structure | Contains: error_code, message, details, trace_id |
| 4 | Verify error_code is meaningful | Check error_code field | e.g., "NOT_FOUND" or "RESOURCE_NOT_FOUND" |

**Postconditions:** No raw framework errors exposed to clients

---

## US-004: Structured Logging & Metrics Collection

### TC-01: Structured JSON logging with required fields [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: At least one service deployed with logging middleware

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger a service action | `GET /v1/health` | HTTP 200 |
| 2 | Query logging system for recent entries | Search by service name and last 1 minute | Log entries found |
| 3 | Verify log format is JSON | Inspect raw log entry | Valid JSON structure |
| 4 | Verify timestamp field | Check log entry | ISO-8601 timestamp present |
| 5 | Verify level field | Check log entry | Level field present (info/warn/error) |
| 6 | Verify service field | Check log entry | Service name present |
| 7 | Verify correlation_id field | Check log entry | correlation_id present and matches request |
| 8 | Verify message field | Check log entry | Human-readable message present |

**Postconditions:** All logs in structured JSON format with required fields

---

### TC-02: Log retention for 90 days [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Logging system configured with retention policy

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Check log retention policy configuration | Cloud Logging > Log Router > Retention settings | Retention set to 90 days |
| 2 | Query for logs from 89 days ago (if available) | Filter by date range | Logs still accessible |
| 3 | Verify retention policy is applied to all log buckets | Check all configured sinks | All sinks have 90-day retention |

**Postconditions:** Log retention policy confirmed at 90 days

---

### TC-03: Metrics collection via Prometheus/CloudWatch [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Metrics exporter deployed, Prometheus scraping configured

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Generate traffic to a service | Send 50 requests to `GET /v1/health` | All return 200 |
| 2 | Query transaction_count metric | `transaction_count{service="api-gateway"}` | Metric exists with value ≥ 50 |
| 3 | Query error_count metric | `error_count{service="api-gateway"}` | Metric exists (value ≥ 0) |
| 4 | Query latency_histogram metric | `latency_histogram{service="api-gateway"}` | Histogram buckets populated |
| 5 | Verify metrics are scrapeable | `GET /metrics` on service pod | Prometheus-format metrics returned |

**Postconditions:** All required metrics being collected

---

### TC-04: Real-time dashboard displays key metrics [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Grafana/CloudWatch dashboard configured

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Open service dashboard | Navigate to Grafana/CloudWatch dashboard URL | Dashboard loads without errors |
| 2 | Verify request rate panel | Inspect request rate widget | Shows current request rate in real time |
| 3 | Verify error rate panel | Inspect error rate widget | Shows current error rate (may be 0%) |
| 4 | Verify p95 latency panel | Inspect latency widget | Shows p95 latency value in real time |
| 5 | Generate traffic and observe update | Send 20 requests, refresh dashboard | Metrics update within 30 seconds |

**Postconditions:** Dashboard operational with real-time data

---

## US-005: Automated Alerting on SLA Breaches

### TC-01: Warning alert on p95 latency exceeding 2s [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Alert rules configured, on-call channel accessible, ability to simulate latency

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Simulate high latency on a service | Inject 3-second delay on test endpoint for 1+ minute | Latency exceeds 2s threshold |
| 2 | Wait for alert evaluation window | Wait 1 minute | Alert rule evaluates |
| 3 | Check on-call channel for alert | Monitor Slack/PagerDuty/email channel | Warning alert received |
| 4 | Verify alert contains service name | Inspect alert payload | Affected service identified |
| 5 | Remove latency injection | Restore normal service behavior | Latency returns to normal |
| 6 | Verify alert resolves | Wait for evaluation window | Alert marked as resolved |

**Postconditions:** Latency alert fires and auto-resolves

---

### TC-02: Critical alert on error rate exceeding 1% [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Alert rules configured, ability to simulate errors

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Simulate elevated error rate | Inject 500 errors on >1% of requests for 2+ minutes | Error rate exceeds 1% |
| 2 | Wait for alert evaluation window | Wait 2 minutes | Alert rule evaluates |
| 3 | Check incident channel for critical alert | Monitor alert channel | Critical alert received |
| 4 | Verify alert contains service name | Inspect alert payload | Affected service name present |
| 5 | Verify alert contains error details | Inspect alert payload | Error type/details included |
| 6 | Stop error injection | Restore normal behavior | Error rate drops below 1% |

**Postconditions:** Error rate alert fires with actionable details

---

### TC-03: Critical alert on availability below 99.9% [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Alert rules configured, ability to simulate downtime

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Simulate service unavailability | Take service offline or return 503 for >0.1% of rolling 1-hour window | Availability drops below 99.9% |
| 2 | Wait for alert evaluation | Wait for rolling window calculation | Alert rule evaluates |
| 3 | Check incident channel | Monitor alert channel | Critical alert received |
| 4 | Verify alert identifies the service | Inspect alert payload | Service name and availability percentage included |
| 5 | Restore service | Bring service back online | Service recovers |

**Postconditions:** Availability alert fires on SLA breach

---

## US-006: Design System Core Components

### TC-01: Button component renders in all variants with correct touch target [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging (Storybook or test harness)
- User role: Frontend Developer / QA
- Data setup: Component library imported in test app

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Render Button in primary variant | `<Button variant="primary">Submit</Button>` | Button renders with primary styling |
| 2 | Render Button in secondary variant | `<Button variant="secondary">Cancel</Button>` | Button renders with secondary styling |
| 3 | Render Button in ghost variant | `<Button variant="ghost">Skip</Button>` | Button renders with ghost styling |
| 4 | Measure touch target size | Inspect computed styles | Minimum 44px height and 44px width |
| 5 | Verify contrast ratio | Use axe-core or Colour Contrast Analyser | Text-to-background ratio ≥ 4.5:1 for all variants |

**Postconditions:** All button variants accessible and correctly sized

---

### TC-02: Form input error state with accessibility [P1] [@regression]

**Preconditions:**
- Environment: staging (Storybook or test harness)
- User role: Frontend Developer / QA
- Data setup: Form input component available

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Render form input in default state | `<Input label="Email" />` | Input renders with label |
| 2 | Trigger error state | `<Input label="Email" error="Invalid email format" />` | Error state displayed |
| 3 | Verify inline validation message | Inspect rendered output | "Invalid email format" message visible below input |
| 4 | Verify red border on error | Inspect computed border-color | Border is red/error color |
| 5 | Verify aria-invalid attribute | Inspect DOM | `aria-invalid="true"` present on input element |
| 6 | Verify aria-describedby links to error | Inspect DOM | `aria-describedby` points to error message element ID |
| 7 | Test with screen reader | Activate VoiceOver/NVDA | Error message announced when input is focused |

**Postconditions:** Error states fully accessible

---

### TC-03: Keyboard navigation with visible focus [P1] [@regression]

**Preconditions:**
- Environment: staging (test harness with multiple interactive components)
- User role: QA Tester
- Data setup: Page with Button, Input, Checkbox, and Link components

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Press Tab key from page start | Keyboard Tab | First interactive element receives focus |
| 2 | Verify focus indicator visible | Inspect visually | 2px outline visible around focused element |
| 3 | Continue tabbing through all elements | Press Tab repeatedly | Focus moves in logical DOM order |
| 4 | Press Enter on focused Button | Keyboard Enter | Button action triggered |
| 5 | Press Space on focused Checkbox | Keyboard Space | Checkbox toggles state |
| 6 | Shift+Tab to move backwards | Keyboard Shift+Tab | Focus moves to previous element |

**Postconditions:** All interactive components keyboard-accessible with visible focus

---

### TC-04: Components reflow on 320px viewport [P1] [@regression]

**Preconditions:**
- Environment: staging (browser or device emulator)
- User role: QA Tester
- Data setup: Page with all core components rendered

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Set viewport to 320px width | Browser DevTools > Responsive > 320px | Viewport resized |
| 2 | Verify no horizontal scrollbar | Check page overflow | No horizontal scroll present |
| 3 | Verify buttons are full-width or properly sized | Inspect button layout | Buttons fit within viewport, no overflow |
| 4 | Verify form inputs fit viewport | Inspect input layout | Inputs 100% width, no clipping |
| 5 | Verify text does not overflow | Check all text elements | Text wraps correctly, no truncation without ellipsis |

**Postconditions:** All components responsive at minimum viewport

---

## US-007: Design System Extended Components (Cards, Modals, Navigation)

### TC-01: Loyalty card component displays tier and points [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging (Storybook or test harness)
- User role: QA Tester
- Data setup: Loyalty card component with test data

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Render loyalty card with Silver tier | `<LoyaltyCard points={2500} tier="Silver" />` | Card renders with points balance "2,500" |
| 2 | Verify tier badge color for Silver | Inspect badge element | Silver-specific badge color displayed |
| 3 | Render loyalty card with Gold tier | `<LoyaltyCard points={8000} tier="Gold" />` | Gold-specific badge color displayed |
| 4 | Verify screen reader accessibility | Activate VoiceOver | Card content announced: tier, points balance |
| 5 | Verify aria-label or role | Inspect DOM | Appropriate ARIA attributes for card semantics |

**Postconditions:** Loyalty card renders correctly for all tiers

---

### TC-02: Confirmation modal focus trap and dismiss [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging (test harness)
- User role: QA Tester
- Data setup: Page with a button that triggers a confirmation modal

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Click trigger button to open modal | Click "Delete Account" button | Modal appears with overlay |
| 2 | Verify focus moves into modal | Check active element | Focus is on first focusable element inside modal |
| 3 | Press Tab repeatedly | Keyboard Tab | Focus cycles within modal only (trapped) |
| 4 | Verify focus does not escape to background | Tab through all modal elements | Focus wraps back to first modal element |
| 5 | Press Escape key | Keyboard Escape | Modal closes |
| 6 | Verify focus returns to trigger | Check active element | Focus is back on "Delete Account" button |

**Postconditions:** Modal meets WCAG focus management requirements

---

### TC-03: Skeleton loading component matches content layout [P2] [@regression]

**Preconditions:**
- Environment: staging (test harness)
- User role: QA Tester
- Data setup: Component with simulated loading state

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Render component in loading state | `<LoyaltyCard loading={true} />` | Skeleton placeholder displayed |
| 2 | Verify animated placeholder shapes | Inspect visually | Shapes pulse/shimmer to indicate loading |
| 3 | Verify skeleton matches expected layout | Compare skeleton to loaded state | Placeholder shapes match card dimensions |
| 4 | Transition to loaded state | Set `loading={false}` with data | Content replaces skeleton smoothly |
| 5 | Verify no layout shift | Measure CLS | No visible content jump on load |

**Postconditions:** Loading states provide appropriate visual feedback

---

## US-008: CI/CD Pipeline with Security Scanning

### TC-01: Feature branch pipeline executes lint, tests, SAST, and builds image [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging (CI/CD system — Cloud Build / GitHub Actions)
- User role: Developer
- Data setup: Feature branch with passing code (coverage ≥ 80%)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Push code to feature branch | `git push origin feature/test-pipeline` | Pipeline triggered automatically |
| 2 | Verify lint stage executes | Check pipeline logs | Lint stage passes |
| 3 | Verify unit test stage executes | Check pipeline logs | Tests pass with coverage report |
| 4 | Verify coverage threshold enforced | Check coverage output | Coverage ≥ 80% reported |
| 5 | Verify SAST scan executes | Check pipeline logs | SAST scan completes |
| 6 | Verify Docker image built | Check Artifact Registry | Image tagged with commit SHA present |

**Postconditions:** Pipeline completes all stages for feature branch

---

### TC-02: Pipeline fails on coverage below 80% [P1] [@regression]

**Preconditions:**
- Environment: staging (CI/CD system)
- User role: Developer
- Data setup: Feature branch with code coverage < 80%

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Push code with low test coverage | `git push origin feature/low-coverage` (coverage at 60%) | Pipeline triggered |
| 2 | Verify lint stage passes | Check pipeline logs | Lint passes |
| 3 | Verify unit test stage fails pipeline | Check pipeline logs | Pipeline fails at test stage |
| 4 | Verify failure reason is coverage | Check error output | Message indicates coverage 60% < 80% threshold |
| 5 | Verify no Docker image built | Check Artifact Registry | No image for this commit |

**Postconditions:** Pipeline correctly gates on coverage threshold

---

### TC-03: Pipeline fails on critical/high SAST findings [P1] [@regression]

**Preconditions:**
- Environment: staging (CI/CD system)
- User role: Developer
- Data setup: Feature branch with known critical vulnerability (e.g., SQL injection pattern)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Push code with critical SAST finding | `git push origin feature/vuln-test` | Pipeline triggered |
| 2 | Verify SAST scan detects vulnerability | Check SAST report | Critical/High finding reported |
| 3 | Verify pipeline fails | Check pipeline status | Pipeline marked as failed |
| 4 | Verify no Docker image built | Check Artifact Registry | No image for this commit |

**Postconditions:** Security gate prevents vulnerable code from being built

---

### TC-04: Merge to main auto-deploys to staging [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Developer
- Data setup: Feature branch with passing pipeline, PR approved

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Merge PR to main branch | Merge via GitHub/GitLab UI | Merge completes |
| 2 | Verify deployment pipeline triggers | Check CI/CD dashboard | Deployment pipeline starts |
| 3 | Verify image deployed to staging | Check staging cluster | New image version running |
| 4 | Verify service is healthy in staging | `GET /v1/health` on staging | HTTP 200 returned |

**Postconditions:** Code on main automatically deployed to staging

---

### TC-05: Production deployment with smoke tests [P1] [@regression]

**Preconditions:**
- Environment: production (or production-like)
- User role: Developer with promotion approval
- Data setup: Staging deployment verified and stable

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Approve promotion to production | Click approve in deployment UI | Production deployment starts |
| 2 | Verify image deployed to production | Check production cluster | New image version running |
| 3 | Verify smoke test suite runs post-deploy | Check pipeline logs | Smoke tests execute automatically |
| 4 | Verify smoke tests pass | Check test results | All smoke tests green |
| 5 | Verify service healthy | `GET /v1/health` on production | HTTP 200 returned |

**Postconditions:** Production deployment verified by automated smoke tests

---

### TC-06: Production rollback completes within 5 minutes [P1] [@regression]

**Preconditions:**
- Environment: production (or production-like)
- User role: Developer / On-call Engineer
- Data setup: Failed deployment detected (smoke tests failing)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger rollback | Execute rollback command or click rollback in UI | Rollback initiated |
| 2 | Start timer | Record start time | Timer running |
| 3 | Verify previous version restored | Check running image version | Previous stable version deployed |
| 4 | Verify service healthy | `GET /v1/health` | HTTP 200 returned |
| 5 | Stop timer | Record end time | Total rollback time < 5 minutes |

**Postconditions:** Previous stable version restored within SLA

---

## US-009: Push Notification Service Integration

### TC-01: Push notification delivered on points.earned event [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, push token registered, device available)
- Data setup: Clara's customer_id = `cust-clara-001`, push token registered, notification templates configured

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Publish points.earned event | `POST /v1/events/publish` with `{"topic": "points.earned", "customer_id": "cust-clara-001", "points": 150, "balance": 2650, "tier": "Silver"}` | Event published successfully (HTTP 202) |
| 2 | Wait for notification delivery | Wait up to 30 seconds | Push notification appears on Clara's device |
| 3 | Verify notification content | Inspect push notification | Contains points earned and updated balance |
| 4 | Verify delivery time | Check timestamp difference | Delivered within 30 seconds of event publish |

**Postconditions:** Push notification delivered within SLA

---

### TC-02: Retry with exponential backoff on delivery failure [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Clara's push token set to an invalid/expired token to simulate failure

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Publish points.earned event | `POST /v1/events/publish` with `{"topic": "points.earned", "customer_id": "cust-clara-001", "points": 50}` | Event published (HTTP 202) |
| 2 | Verify first retry at ~1s | Check notification service logs | Retry attempt 1 logged at ~1s after initial failure |
| 3 | Verify second retry at ~5s | Check logs | Retry attempt 2 logged at ~5s after attempt 1 |
| 4 | Verify third retry at ~30s | Check logs | Retry attempt 3 logged at ~30s after attempt 2 |
| 5 | Verify marked as failed after 3 retries | Check notification status | Status = "failed" after all retries exhausted |

**Postconditions:** Retry logic follows exponential backoff pattern

---

### TC-03: Fallback to email when no push token registered [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Nathan (customer, NO push token registered, email = nathan.test@example.com)
- Data setup: Nathan's customer_id = `cust-nathan-002`, no push token, email configured

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Publish points.earned event for Nathan | `POST /v1/events/publish` with `{"topic": "points.earned", "customer_id": "cust-nathan-002", "points": 200}` | Event published (HTTP 202) |
| 2 | Verify no push notification attempted | Check notification service logs | Log indicates no push token found |
| 3 | Verify email fallback triggered | Check email provider logs/inbox | Email sent to nathan.test@example.com |
| 4 | Verify email content | Inspect email | Contains points earned information |

**Postconditions:** Email fallback works when push unavailable

---

### TC-04: Template variable substitution in notification [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer)
- Data setup: Notification template with variables: "You earned {points} points! Balance: {balance}. Tier: {tier}"

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Publish event with specific values | `{"topic": "points.earned", "customer_id": "cust-clara-001", "points": 300, "balance": 3000, "tier": "Gold"}` | Event published |
| 2 | Verify notification content | Inspect delivered notification | "You earned 300 points! Balance: 3000. Tier: Gold" |
| 3 | Verify no raw template variables | Check notification text | No `{points}`, `{balance}`, or `{tier}` literals present |

**Postconditions:** All template variables correctly substituted

---

### TC-05: Security — PII not logged in notification payloads [P1] [@regression @security]

**Preconditions:**
- Environment: staging
- User role: Platform Engineer
- Data setup: Notification service processing events

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger a notification delivery | Publish points.earned event | Notification processed |
| 2 | Search logs for customer PII | Search for customer email, name, or full token in logs | No PII found in log entries |
| 3 | Verify payload is redacted in logs | Check notification service logs | Notification body/payload not logged in full |
| 4 | Verify push tokens are masked | Check logs for token references | Tokens shown as masked (e.g., `****abc123`) |

**Postconditions:** No PII exposure in notification service logs

---

## US-010: In-App Notification Center Storage

### TC-01: Notification persisted to notification_center table [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer)
- Data setup: Clara's customer_id = `cust-clara-001`, notification service running

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger a notification | Publish points.earned event for Clara | Notification delivered |
| 2 | Query notification_center table | `SELECT * FROM notification_center WHERE customer_id = 'cust-clara-001' ORDER BY created_at DESC LIMIT 1` | Record found |
| 3 | Verify id field | Check record | Non-null UUID |
| 4 | Verify customer_id field | Check record | `cust-clara-001` |
| 5 | Verify title field | Check record | Non-empty notification title |
| 6 | Verify body field | Check record | Non-empty notification body |
| 7 | Verify type field | Check record | e.g., "points_earned" |
| 8 | Verify read_status field | Check record | `false` (unread) |
| 9 | Verify created_at field | Check record | Timestamp within last minute |

**Postconditions:** Notification correctly persisted with all required fields

---

### TC-02: Notification center displays newest-first with unread badge [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated in mobile app)
- Data setup: Clara has 5+ notifications, at least 3 unread

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Open mobile app as Clara | Login with Clara's credentials | App loads to home screen |
| 2 | Observe notification icon in navigation | Check nav bar | Unread count badge shows "3" (or current unread count) |
| 3 | Tap notification center icon | Navigate to notification center | Notification list loads |
| 4 | Verify sort order | Check first notification | Most recent notification appears first |
| 5 | Verify second notification is older | Compare timestamps | Second item has earlier timestamp than first |
| 6 | Call API directly to verify | `GET /v1/notifications?customer_id=cust-clara-001` | Response ordered by created_at DESC |

**Postconditions:** Notification center displays correctly ordered with badge

---

### TC-03: Tapping notification marks as read and decrements badge [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: Clara has at least 2 unread notifications

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Note current unread count | Observe badge on nav icon | e.g., badge shows "3" |
| 2 | Open notification center | Tap notification icon | List loads with unread items highlighted |
| 3 | Tap first unread notification | Tap notification item | Notification detail/content opens |
| 4 | Verify notification marked as read | Check visual state | Notification no longer styled as unread |
| 5 | Navigate back to notification list | Press back | List view shown |
| 6 | Verify unread badge decremented | Check nav icon badge | Badge shows "2" (decremented by 1) |
| 7 | Verify via API | `GET /v1/notifications/{id}` | `read_status: true` |

**Postconditions:** Read status correctly tracked and badge updated

---

### TC-04: Notification center screen reader accessibility [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, VoiceOver/TalkBack enabled)
- Data setup: Clara has mix of read and unread notifications

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Navigate to notification center via screen reader | Swipe/Tab to notification icon, activate | Notification center opens |
| 2 | Focus on first notification item | Screen reader focuses first item | Title announced |
| 3 | Verify time is announced | Listen to announcement | Relative time (e.g., "2 hours ago") announced |
| 4 | Verify read/unread status announced | Listen to announcement | "Unread" or "Read" status announced |
| 5 | Move to next notification | Swipe right / Tab | Next notification announced with same detail |

**Postconditions:** Notification center fully accessible via screen reader

---

### TC-05: Security — Cross-customer notification access prevented [P1] [@regression @security]

**Preconditions:**
- Environment: staging
- User role: Nathan (customer, authenticated)
- Data setup: Clara has notifications, Nathan should not see them

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Authenticate as Nathan | Login with Nathan's credentials | Token for cust-nathan-002 |
| 2 | Attempt to access Clara's notifications via API | `GET /v1/notifications?customer_id=cust-clara-001` with Nathan's token | HTTP 403 Forbidden |
| 3 | Verify no data returned | Inspect response body | No notification data from Clara's account |
| 4 | Access Nathan's own notifications | `GET /v1/notifications?customer_id=cust-nathan-002` with Nathan's token | HTTP 200 with Nathan's notifications only |

**Postconditions:** Customer data isolation enforced

---

## US-011: Mobile App Shell with Navigation

### TC-01: App launches within 3 seconds with splash screen [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, app installed)
- Data setup: App installed on test device with 4G connection

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Force-close the app | Kill app process | App fully closed |
| 2 | Launch the app (cold start) | Tap app icon | Splash screen appears immediately |
| 3 | Start timer on tap | Record launch time | Timer running |
| 4 | Wait for main screen to load | Observe screen transition | Home screen displayed |
| 5 | Stop timer | Record load complete time | Total time ≤ 3 seconds |
| 6 | Verify splash screen was shown during load | Visual observation | Branded splash screen visible during initialization |

**Postconditions:** App cold start within 3-second SLA on 4G

---

### TC-02: Bottom navigation shows 4 tabs with icons and labels [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: App loaded to home screen

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Observe bottom navigation bar | Visual inspection | Navigation bar visible at bottom |
| 2 | Verify Home tab | Check first tab | Icon + label "Home" present |
| 3 | Verify History tab | Check second tab | Icon + label "History" present |
| 4 | Verify QR Code tab | Check third tab | Icon + label "QR Code" present |
| 5 | Verify Profile tab | Check fourth tab | Icon + label "Profile" present |
| 6 | Verify Home tab is selected by default | Check active state | Home tab highlighted/selected |

**Postconditions:** All 4 navigation tabs visible and correctly labeled

---

### TC-03: Tab transitions complete within 300ms [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: App on home screen

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Tap History tab | Tap "History" in bottom nav | Screen transition begins |
| 2 | Measure transition time | Performance profiler or visual timing | Transition completes within 300ms |
| 3 | Verify no jank | Observe animation smoothness | No dropped frames or stuttering |
| 4 | Tap QR Code tab | Tap "QR Code" in bottom nav | Transition completes within 300ms |
| 5 | Tap Profile tab | Tap "Profile" in bottom nav | Transition completes within 300ms |
| 6 | Tap Home tab | Tap "Home" in bottom nav | Transition completes within 300ms |

**Postconditions:** All tab transitions performant

---

### TC-04: Deep link navigates to correct screen [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, app installed and authenticated)
- Data setup: Deep link scheme `loyalty://` configured

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Open deep link to history | Open URL `loyalty://history` | App launches/foregrounds |
| 2 | Verify correct screen displayed | Check current screen | Transaction history screen shown |
| 3 | Verify History tab is selected | Check bottom nav state | History tab highlighted |
| 4 | Open deep link to profile | Open URL `loyalty://profile` | Profile screen shown |
| 5 | Open invalid deep link | Open URL `loyalty://nonexistent` | App opens to home screen (graceful fallback) |

**Postconditions:** Deep links route correctly with graceful fallback

---

### TC-05: Bottom navigation accessible via VoiceOver/TalkBack [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, screen reader enabled)
- Data setup: App loaded, VoiceOver (iOS) or TalkBack (Android) active

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Navigate to bottom tab bar via screen reader | Swipe to reach tab bar | Tab bar region announced |
| 2 | Focus on Home tab | Screen reader focuses Home | "Home, tab, selected" announced |
| 3 | Move to History tab | Swipe right | "History, tab" announced (not selected) |
| 4 | Activate History tab | Double-tap | "History, tab, selected" announced, screen changes |
| 5 | Move through remaining tabs | Swipe right | Each tab announced with label and state |

**Postconditions:** Navigation fully accessible via assistive technology

---

## US-012: Push Token Registration & Network Detection

### TC-01: Permission dialog explains notification purpose [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, fresh app install, notification permission not yet granted)
- Data setup: App installed but never launched (or permissions reset)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Launch app for the first time | Tap app icon | App loads |
| 2 | Observe notification permission dialog | Wait for system dialog | Permission dialog appears |
| 3 | Verify explanatory text | Read pre-permission screen or dialog text | Message includes "Get instant updates on your points and rewards" |
| 4 | Verify dialog has allow/deny options | Check dialog buttons | Allow and Don't Allow (or equivalent) buttons present |

**Postconditions:** User informed about notification purpose before granting permission

---

### TC-02: Push token registered on permission grant [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated, permission dialog shown)
- Data setup: Clara's customer_id = `cust-clara-001`

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Grant notification permission | Tap "Allow" on permission dialog | Permission granted |
| 2 | Verify token sent to server | Check network traffic or server logs | `POST /v1/devices/register` called with push token |
| 3 | Verify token associated with customer | `GET /v1/devices?customer_id=cust-clara-001` (internal) | Token record exists for Clara |
| 4 | Verify token is valid format | Inspect stored token | FCM/APNs token format (non-empty string) |

**Postconditions:** Push token registered and associated with customer

---

### TC-03: Offline banner appears on network loss [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated, app open)
- Data setup: Device connected to network, app on home screen

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Verify app is online | Check app state | No offline indicator visible |
| 2 | Disable network connectivity | Turn on Airplane Mode or disable WiFi/cellular | Network disconnected |
| 3 | Verify offline banner appears | Observe top of screen | Banner: "You're offline — some features may be limited" |
| 4 | Verify banner is subtle (not blocking) | Check banner style | Non-intrusive banner at top, content still visible below |
| 5 | Attempt to navigate within app | Tap different tabs | App remains usable (cached content shown) |

**Postconditions:** Offline state clearly communicated to user

---

### TC-04: Offline banner disappears and data refreshes on reconnection [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, app showing offline banner)
- Data setup: Device in airplane mode, offline banner visible

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Verify offline banner is showing | Observe screen | "You're offline" banner visible |
| 2 | Restore network connectivity | Turn off Airplane Mode | Network reconnected |
| 3 | Verify offline banner disappears | Observe top of screen | Banner removed automatically |
| 4 | Verify data refreshes silently | Observe content area | Content updates without user action |
| 5 | Verify no loading spinner shown | Observe UI | Refresh happens in background (no blocking UI) |

**Postconditions:** App recovers gracefully from offline state

---

## US-013: Web App Scaffold with Responsive Layout

### TC-01: Web app renders with header, content area, and footer [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: Web app deployed, Clara logged in

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Navigate to loyalty web app | Open `https://loyalty.staging.example.com` | Page loads |
| 2 | Verify header present | Inspect top of page | Header with logo, nav links, and profile icon visible |
| 3 | Verify logo in header | Check header left area | Brand logo displayed |
| 4 | Verify navigation links | Check header nav | Links to main sections present |
| 5 | Verify profile icon | Check header right area | Profile icon/avatar visible |
| 6 | Verify main content area | Check page body | Content area rendered between header and footer |
| 7 | Verify footer present | Scroll to bottom | Footer with expected links/info visible |

**Postconditions:** Web app layout renders correctly with all sections

---

### TC-02: Responsive layout on mobile viewport [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: Web app loaded in browser

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Resize browser to mobile viewport | Set viewport width to 375px (< 768px) | Layout adjusts |
| 2 | Verify navigation collapses to hamburger | Check header | Hamburger menu icon replaces nav links |
| 3 | Tap hamburger menu | Click hamburger icon | Navigation menu expands/slides in |
| 4 | Verify nav links accessible in menu | Check expanded menu | All navigation links present |
| 5 | Verify content is single-column | Check main content area | Content stacks vertically, no side-by-side columns |
| 6 | Verify no horizontal scrolling | Check page overflow | No horizontal scrollbar |
| 7 | Close hamburger menu | Tap close/outside | Menu collapses |

**Postconditions:** Mobile responsive layout functional

---

### TC-03: Unauthenticated user redirected to login with return URL [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Unauthenticated visitor
- Data setup: No active session/token

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Open protected route directly | Navigate to `https://loyalty.staging.example.com/dashboard` | Redirect occurs |
| 2 | Verify redirected to login page | Check current URL | URL is login page |
| 3 | Verify return URL preserved | Check URL parameters | `?returnUrl=/dashboard` (or equivalent) in URL |
| 4 | Login with valid credentials | Enter Clara's credentials, submit | Authentication succeeds |
| 5 | Verify redirected back to original page | Check current URL | Navigated to `/dashboard` |

**Postconditions:** Auth guard protects routes and preserves navigation intent

---

### TC-04: Unauthenticated access to login page itself works [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Unauthenticated visitor
- Data setup: No active session

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Navigate to login page | Open `https://loyalty.staging.example.com/login` | Login page renders |
| 2 | Verify no redirect loop | Check page state | Login form displayed, no infinite redirect |
| 3 | Verify login form is functional | Check form elements | Email/password fields and submit button present |

**Postconditions:** Login page accessible without authentication

---

### TC-05: Keyboard navigation through all interactive elements [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: Web app loaded on dashboard page

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Press Tab from page top | Keyboard Tab | First interactive element (skip link or logo link) receives focus |
| 2 | Verify focus indicator visible | Observe focused element | Clear visual focus ring/outline |
| 3 | Tab through header elements | Press Tab repeatedly | Focus moves through logo, nav links, profile icon in order |
| 4 | Tab into main content | Continue tabbing | Focus enters main content area |
| 5 | Tab through content interactive elements | Continue tabbing | All buttons, links, inputs reachable |
| 6 | Tab into footer | Continue tabbing | Focus reaches footer links |
| 7 | Verify no focus traps | Tab through entire page | Focus eventually cycles or reaches end without getting stuck |

**Postconditions:** Full keyboard accessibility confirmed

---

## US-014: Global Error Boundary & Version Check

### TC-01: Error boundary shows friendly error screen [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer, authenticated)
- Data setup: App with test mechanism to trigger unhandled error (e.g., debug menu or test endpoint)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger an unhandled error | Navigate to test screen that throws unhandled exception | Error occurs |
| 2 | Verify friendly error screen shown | Observe screen | "Something went wrong — tap to retry" message displayed |
| 3 | Verify no crash/stack trace shown | Check screen content | No technical error details visible to user |
| 4 | Verify error reported to monitoring | Check error monitoring tool (Sentry/Crashlytics) | Error event logged with stack trace and context |
| 5 | Tap retry button | Tap "tap to retry" | App attempts to recover/reload the screen |

**Postconditions:** Errors handled gracefully with monitoring capture

---

### TC-02: Error boundary — negative path (app does not crash) [P1] [@regression]

**Preconditions:**
- Environment: staging
- User role: Nathan (customer, authenticated)
- Data setup: App running normally

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Trigger error in one screen | Force error on History screen | Error boundary catches it |
| 2 | Verify other screens still work | Navigate to Home tab | Home screen loads normally |
| 3 | Verify app did not fully crash | Check app process | App still running, no force-close |
| 4 | Navigate back to errored screen | Tap History tab | Error screen or recovered screen shown (not crash) |

**Postconditions:** Error isolation prevents full app crash

---

### TC-03: Critical update shows non-dismissible modal [P1] [@smoke @regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer)
- Data setup: Version check API configured to return critical update required (app version < minimum required)

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Configure version check to require update | Set minimum version higher than installed app version via `GET /v1/app/version-check` | Config updated |
| 2 | Launch the app | Open app | App starts initialization |
| 3 | Verify non-dismissible modal appears | Observe screen | Modal with update prompt displayed |
| 4 | Verify modal cannot be dismissed | Tap outside modal, press back button | Modal remains visible |
| 5 | Verify app store link present | Check modal content | "Update" button links to correct app store URL |
| 6 | Verify app is not usable behind modal | Attempt to interact with app | No interaction possible with underlying app |

**Postconditions:** Critical updates enforced, app unusable until updated

---

### TC-04: Non-critical update shows dismissible banner [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Clara (customer)
- Data setup: Version check API configured to suggest (not require) update

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Configure version check for optional update | Set recommended version higher than installed, minimum version equal or lower | Config updated |
| 2 | Launch the app | Open app | App loads |
| 3 | Verify dismissible banner appears | Observe screen | Banner suggesting update displayed |
| 4 | Verify app is usable | Interact with app behind/below banner | App functions normally |
| 5 | Dismiss the banner | Tap dismiss/close button on banner | Banner disappears |
| 6 | Verify app continues normally | Navigate through app | Full functionality available |

**Postconditions:** Non-critical updates suggested but not enforced

---

### TC-05: Version check API failure does not block app [P2] [@regression]

**Preconditions:**
- Environment: staging
- User role: Nathan (customer)
- Data setup: Version check endpoint returning 500 or timing out

**Steps:**
| # | Action | Data | Expected Result |
|---|--------|------|-----------------|
| 1 | Simulate version check API failure | Configure `GET /v1/app/version-check` to return 500 | Endpoint failing |
| 2 | Launch the app | Open app | App starts |
| 3 | Verify app loads normally | Observe home screen | App loads without blocking on version check |
| 4 | Verify no error shown to user | Check for error messages | No error related to version check visible |

**Postconditions:** Version check failure handled gracefully, app remains usable

---

## Test Summary

| Story | Test Cases | Smoke | Regression | Security |
|-------|-----------|-------|------------|----------|
| US-001 | 5 | 1 | 5 | 1 |
| US-002 | 4 | 1 | 4 | 0 |
| US-003 | 3 | 1 | 3 | 0 |
| US-004 | 4 | 1 | 4 | 0 |
| US-005 | 3 | 2 | 3 | 0 |
| US-006 | 4 | 1 | 4 | 0 |
| US-007 | 3 | 2 | 3 | 0 |
| US-008 | 6 | 2 | 6 | 0 |
| US-009 | 5 | 1 | 5 | 1 |
| US-010 | 5 | 2 | 5 | 1 |
| US-011 | 5 | 2 | 5 | 0 |
| US-012 | 4 | 2 | 4 | 0 |
| US-013 | 5 | 2 | 5 | 0 |
| US-014 | 5 | 2 | 5 | 0 |
| **Total** | **61** | **22** | **61** | **3** |
