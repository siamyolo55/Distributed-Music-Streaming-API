# Agent Context Memory — Distributed Music Streaming API

## Project stack snapshot
- Language/runtime: Java 21
- Framework: Spring Boot 3.x (Web, WebFlux, Security, Data JPA, Kafka, Actuator)
- Architecture: Microservices + event-driven design (Kafka backbone)
- Data/storage: PostgreSQL (OLTP), Redis (cache/session), MinIO (object storage for media)
- Infra: Docker Compose, service-level Dockerfiles, health checks
- Reliability/ops: Resilience4j, idempotent consumers, DLQ/retries, observability (Micrometer, Prometheus, OpenTelemetry)
- Tooling/testing: Maven/Gradle, Flyway, JUnit 5, Mockito, AssertJ, Testcontainers, JaCoCo, static analysis (Checkstyle/PMD/SpotBugs)

## One-by-one execution plan

### Phase A — Foundation (Milestone 0)
1. Create monorepo folders: `services`, `libs`, `infra`.
2. Initialize baseline services (`user-service`, `media-service`) and shared libs (`common-events`, `common-security`, `common-observability`).
3. Add root `docker-compose.yml` with PostgreSQL, Redis, Kafka(+Zookeeper or Redpanda), MinIO.
4. Add service Dockerfiles (multi-stage), env-driven configs, and `/actuator/health` probes.
5. Set up CI to run build, unit tests, static checks, and coverage report.
6. Define initial OpenAPI conventions, error response contract, and API versioning standards.

### Phase B — User + Media (Milestone 1)
7. Implement user registration/login/profile APIs.
8. Add JWT auth and authorization guards on non-public endpoints.
9. Implement follow/unfollow, playlist CRUD, and user preferences APIs.
10. Implement media upload endpoint for admin/artist roles.
11. Persist track metadata and object storage references.
12. Publish `TrackUploaded` event to Kafka with schema/version metadata.

### Phase C — Streaming + Event Backbone (Milestone 2)
13. Implement manifest/chunk retrieval endpoints in `streaming-service` (WebFlux).
14. Generate secure signed streaming URLs.
15. Add Redis caching for hot manifests and frequently streamed assets.
16. Emit playback lifecycle events: started, paused/resumed, skipped, completed.
17. Record listen duration/session behavior and publish to event topics.

### Phase D — Recommendations + Analytics (Milestone 3)
18. Implement recommendation consumers for playback events.
19. Build recommendation APIs: "because you listened to X" and trending by time window.
20. Implement analytics consumers/materialized tables.
21. Build analytics APIs: skip rates, active users/minute, trending artists, play-through rates.
22. Add cache layer for high-QPS analytics reads.

### Phase E — Production Hardening (Milestone 4)
23. Add resilience patterns (retry, circuit breaker, DLQ) for all async integrations.
24. Enforce idempotency for every event consumer using deduplication strategy.
25. Add tracing/metrics/log correlation and dashboards with SLO-focused views.
26. Validate partition strategy and load-test critical paths.
27. Add replay/backfill tooling for operational recovery.

## Functional requirements checklist

### Platform-wide (cross-cutting)
- FR-001: All external APIs are versioned (`/api/v1/...`).
- FR-002: Standardized error payload (`code`, `message`, `details`, `traceId`).
- FR-003: Request validation and input sanitization at API boundaries.
- FR-004: Auth enforced for all non-public routes.
- FR-005: Service health endpoints exposed for liveness/readiness.
- FR-006: Event schemas are versioned and backward compatible.
- FR-007: Event consumers are idempotent.
- FR-008: Observability baseline: structured logs, metrics, traces.

### User Service
- FR-101: User registration and profile management.
- FR-102: JWT-based login/session flow.
- FR-103: OAuth2-compatible auth extension path.
- FR-104: Follow/unfollow user endpoints.
- FR-105: Playlist create/read/update/delete.
- FR-106: User preference management.

### Media Service
- FR-201: Upload track binary (admin/artist authorized).
- FR-202: Store metadata (title, artist, album, duration, bitrate, storage path).
- FR-203: Trigger async transcoding workflow via messaging.
- FR-204: Produce quality-variant/chunk manifests.
- FR-205: Emit `TrackUploaded` events.

### Streaming Service
- FR-301: Serve chunked audio stream endpoints.
- FR-302: Generate signed stream URLs.
- FR-303: Publish playback lifecycle events.
- FR-304: Record listen duration and session behavior.
- FR-305: Cache hot manifests/tracks in Redis.

### Recommendation Service
- FR-401: Consume playback events and aggregate user-track affinity.
- FR-402: Provide "because you listened to X" recommendations.
- FR-403: Provide trending tracks within configurable windows.
- FR-404: Support baseline collaborative-filtering strategy.

### Analytics Service
- FR-501: Compute top skipped tracks.
- FR-502: Compute trending artists.
- FR-503: Compute active users per minute.
- FR-504: Compute play-through rate metrics.
- FR-505: Expose dashboard-ready analytics APIs.

## Delivery order to execute requirements
1. Complete FR-001..FR-008 framework-wide defaults.
2. Complete User Service FR-101..FR-106.
3. Complete Media Service FR-201..FR-205.
4. Complete Streaming Service FR-301..FR-305.
5. Complete Recommendation Service FR-401..FR-404.
6. Complete Analytics Service FR-501..FR-505.
7. Finish resilience/performance hardening + load validation.

## Working agreement for next iterations
- Implement in small PRs (one service capability at a time).
- Every PR includes tests, OpenAPI updates, and event-contract updates when applicable.
- No feature considered done without observability + security checks.

## Execution Tracker (maintained by agent)

### Milestone status
- [x] M0.1 Create monorepo folders (`services`, `libs`, `infra`)
- [x] M0.2 Initialize baseline services (`user-service`, `media-service`) and shared libs (`common-events`, `common-security`, `common-observability`)
- [x] M0.3 Add root `docker-compose.yml` with PostgreSQL, Redis, Kafka-compatible broker (Redpanda), MinIO
- [x] M0.4 Add service Dockerfiles, env-driven configs, and `/actuator/health` readiness/liveness probes
- [x] M0.5 Add CI workflow for build, tests, static checks, coverage report
- [x] M0.6 Define OpenAPI conventions, standardized error contract, and API versioning baseline

### Functional requirements progress
- [x] FR-001: All external APIs are versioned (`/api/v1/...`) (foundation baseline)
- [x] FR-002: Standardized error payload (`code`, `message`, `details`, `traceId`) (foundation baseline)
- [x] FR-003: Request validation and input sanitization at API boundaries (foundation baseline)
- [x] FR-004: Auth enforced for all non-public routes (JWT resource server with bearer token validation)
- [x] FR-005: Service health endpoints exposed for liveness/readiness (actuator probes configured)
- [x] FR-006: Event schemas are versioned and backward compatible (envelope + version fields baseline)
- [ ] FR-007: Event consumers are idempotent (pending consumer implementation)
- [ ] FR-008: Observability baseline: structured logs, metrics, traces (partial baseline; tracing/log correlation pending)
- [x] FR-101: User registration and profile management (registration with PostgreSQL persistence baseline)
- [x] FR-102: JWT-based login/session flow (login endpoint issuing JWT access token)
- [x] FR-103: OAuth2-compatible auth extension path (baseline OAuth identity linking + OAuth-style login endpoint)
- [x] FR-104: Follow/unfollow user endpoints (`POST/DELETE/GET /api/v1/users/me/follows/{targetUserId}`)
- [x] FR-105: Playlist create/read/update/delete (`/api/v1/users/me/playlists`)
- [ ] FR-106: User preference management (pending)

### Recently completed implementation details
- User registration now persists to PostgreSQL `users` table using JPA + Flyway migration (`V1__create_users_table.sql`).
- Login endpoint implemented at `/api/v1/public/auth/login`, issuing JWT bearer tokens.
- Shared security moved to JWT resource-server validation for non-public routes.
- Added `pgadmin` service in `docker-compose.yml` for DB inspection (`http://localhost:5050`).
- Follow/unfollow now models user-to-user relationships with `user_follows` (`V3__create_user_follows_table.sql`) and ownership-safe operations.
- Playlist CRUD added with persistence in `playlists` (`V4__create_playlists_table.sql`) and endpoints under `/api/v1/users/me/playlists`.
- Media upload now supports multipart audio ingestion with pluggable storage (`MediaObjectStorage`) and a local filesystem-backed server (`/local-media/**`) for dev.
- Added minimal demo frontend (`infra/demo-ui`, served on `http://localhost:8080` via Docker Compose) to exercise register/login/follow/upload flows visually.
- Added shared CORS configuration in `common-security` with configurable allowed origins (`security.cors.allowed-origins`) and defaults for local frontend development.
- Added OAuth extension baseline with `user_oauth_accounts` persistence (`V5__create_user_oauth_accounts_table.sql`) and `/api/v1/public/auth/oauth/login` endpoint for provider identity link-or-create and JWT issuance.
- Added new React app scaffold under `apps/web` (Vite + TypeScript) and wired it in Docker Compose at `http://localhost:8080`.
- Moved legacy `infra/demo-ui` to fallback port `http://localhost:8088` for gradual retirement.

### Next item in queue
1. Implement FR-106 user preference management.
2. Expand playlist API with track-item management (`playlist_tracks`) and contract docs.
3. Dependency note: implement playlist track membership only after media track creation/persistence is finalized (FR-201 + FR-202) so playlist items can reference stable `trackId`.
4. Progress media FR-202 persistence details (track metadata table + stable track identifiers) before playlist track linking.
5. Retire `infra/demo-ui` fully after React app covers all smoke-test flows.
