# Distributed Music Streaming API (Spotify-inspired)

**distributed systems project** to design and build a Spotify-like backend using **Java + Spring Boot**, **Docker**, and event-driven architecture.

This repository now includes a runnable Milestone 0 scaffold (multi-module Maven project, Docker Compose infra, and baseline `user-service` + `media-service`).

---

## Quick Start (Run It)

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop (with Compose)

### 1) Build once

```bash
mvn clean verify
```

### 2) Run full stack with Docker Compose

```bash
docker compose up --build
```

Services and ports:
- Web App (React): `http://localhost:8080`
- Legacy Demo UI (temporary): `http://localhost:8088`
- User Service: `http://localhost:8081`
- Media Service: `http://localhost:8082`
- PostgreSQL: `localhost:5432`
- pgAdmin: `http://localhost:5050`
- Redis: `localhost:6379`
- Redpanda (Kafka API): `localhost:9092`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`

### 3) Health checks

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### 4) Use the React web app (recommended)

Open `http://localhost:8080` and test:
- register user
- login and store JWT in page state
- OAuth-style login baseline
- browse discoverable users and follow/unfollow from the Following page
- upload an audio file and view saved track metadata

Local frontend development (without Docker):

```bash
cd apps/web
npm install
npm run dev
```

Vite dev URL: `http://localhost:5173`

Legacy fallback UI remains available temporarily at `http://localhost:8088` and will be retired.

If you need custom frontend origins, configure:
- `CORS_ALLOWED_ORIGINS` for `user-service`
- `CORS_ALLOWED_ORIGINS` for `media-service`

Default allowed origins:
- `http://localhost:8080`
- `http://127.0.0.1:8080`

### 5) Try current APIs via curl

Public endpoint (no auth):

```bash
curl -X POST http://localhost:8081/api/v1/public/users/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"pass123\",\"displayName\":\"Test User\"}"
```

Login to get JWT:

```bash
curl -X POST http://localhost:8081/api/v1/public/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"pass123\"}"
```

OAuth-style login/registration extension (provider identity link-or-create):

```bash
curl -X POST http://localhost:8081/api/v1/public/auth/oauth/login \
  -H "Content-Type: application/json" \
  -d "{\"provider\":\"google\",\"providerUserId\":\"google-123\",\"email\":\"test@example.com\",\"displayName\":\"Test User\"}"
```

Protected endpoint (Bearer token):

```bash
curl -X POST http://localhost:8082/api/v1/media/tracks \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "title=Song A" \
  -F "artistId=artist-1" \
  -F "artistName=Artist One" \
  -F "genre=Pop" \
  -F "file=@/absolute/path/to/song.mp3;type=audio/mpeg"
```

List uploaded tracks metadata:

```bash
curl http://localhost:8082/api/v1/media/tracks \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Current mock mode note:
- upload currently persists track metadata to DB and stores a configurable dummy `fileUrl`
- real per-upload persistent media storage can be re-enabled later
- uploaded file binary is currently validated but intentionally not persisted

Follow a user (protected):

```bash
curl -X POST http://localhost:8081/api/v1/users/me/follows/<TARGET_USER_ID> \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

List followed users (protected):

```bash
curl http://localhost:8081/api/v1/users/me/follows \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Discover users to follow (protected):

```bash
curl http://localhost:8081/api/v1/users/discover \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### 6) Stop stack

```bash
docker compose down
```

### 7) Inspect PostgreSQL with pgAdmin

- URL: `http://localhost:5050`
- Email: `admin@example.com`
- Password: `admin123`

Register server in pgAdmin:
- Host: `postgres`
- Port: `5432`
- Database: `music`
- Username: `music`
- Password: `music`

---

## 1) Vision

Build a microservices-based music streaming platform that demonstrates:

- real-time streaming workflows,
- event-driven communication,
- scalable data pipelines,
- and production engineering practices (clean code, observability, resilience, security).


---

## 2) Core Services

### 1. User Service

**Responsibilities**
- User registration and profile management
- Authentication & authorization (**JWT + OAuth2**)
- Follow/unfollow users
- Playlist CRUD
- User preferences (genres, language, personalized settings)

**Suggested stack**
- Spring Boot Web + Security
- Spring Authorization Server / OAuth2 Client
- PostgreSQL
- Redis (session/token cache where needed)

---

### 2. Media Service

**Responsibilities**
- Audio upload API (admin/artist-facing)
- Track metadata persistence (title, artist, album, duration, bitrate)
- Simulated transcoding queue (e.g., convert and create quality variants)
- Chunk manifest generation for streaming

**Suggested stack**
- Spring Boot Web
- Object storage (local MinIO for dev)
- PostgreSQL for metadata
- Kafka/RabbitMQ for async transcoding jobs

---

### 3. Streaming Service

**Responsibilities**
- Serve chunked audio streams
- Generate signed/secure stream URLs
- Track playback lifecycle events:
  - play/start
  - skip
  - pause/resume
  - completed
- Capture listen duration and session-level behavior

**Suggested stack**
- Spring Boot WebFlux (good fit for streaming)
- Redis cache for hot tracks/manifests
- Kafka producer for playback events

---

### 4. Recommendation Service

**Responsibilities**
- “Because you listened to X” recommendations
- Trending tracks by time window
- Basic collaborative filtering
- Consume Kafka playback events for model/input aggregation

**Suggested stack**
- Spring Boot
- Kafka consumers + stream processing
- PostgreSQL (or ClickHouse later for analytical workloads)

---

### 5. Analytics Service

**Responsibilities**
- Most skipped songs
- Trending artists
- Active users per minute
- Play-through rate
- Expose dashboard-ready API endpoints

**Suggested stack**
- Spring Boot
- Kafka consumer groups
- Materialized analytics tables
- Redis cache for highly queried metrics

---

## 3) System Design Principles

To keep this project senior-level, follow these engineering principles from day 1:

- **Clean architecture boundaries** (API, application, domain, infrastructure)
- **Idempotent event handling** for consumers
- **Schema versioning** for events (backward compatibility)
- **Observability first**: logs, metrics, traces
- **Backpressure-aware design** in event consumers
- **Resilience**: retries, dead-letter queues, circuit breakers
- **Security defaults**: auth on all non-public endpoints, principle of least privilege
- **Scalability**: stateless services, horizontal scaling, partition-aware event keys

---

## 4) Event-Driven Backbone (Kafka)

### Initial event topics
- `playback.events`
- `stream.events`
- `recommendation.inputs`
- `analytics.events`
- `media.transcoding.jobs`

### Event examples
- `TrackStarted`
- `TrackSkipped`
- `TrackCompleted`
- `ListenDurationRecorded`
- `TrackUploaded`

### Partition strategy (v1)
- Use `userId` as key for playback ordering per user.
- Use `trackId` for media/analytics events where track-local ordering matters.

---

## 5) Proposed Monorepo Layout

```text
/Distributed-Music-Streaming-API
  /services
    /user-service
    /media-service
    /streaming-service
    /recommendation-service
    /analytics-service
  /libs
    /common-events
    /common-security
    /common-observability
  /infra
    /docker
    /kafka
    /monitoring
  docker-compose.yml
  README.md
```

---

## 6) Docker & Local Development

### Phase 1 local stack (implemented)
Use `docker compose` to run:
- PostgreSQL
- Redis
- Redpanda (Kafka-compatible broker)
- MinIO
- User Service + Media Service

### Containerization pattern per service
- Multi-stage Dockerfile (build with Maven/Gradle, run on lightweight JRE image)
- Externalized config via env vars
- Healthcheck endpoints (`/actuator/health`)

---

## 7) Recommended Libraries & Tooling

### Spring ecosystem
- Spring Boot 3.x
- Spring Web / WebFlux
- Spring Security
- Spring Data JPA
- Spring for Apache Kafka
- Spring Boot Actuator
- Spring Cloud OpenFeign (if sync inter-service calls are needed)

### Supporting libraries
- Lombok (optional; use carefully)
- MapStruct (DTO ↔ domain mapping)
- Testcontainers (integration tests)
- Resilience4j (circuit breakers/retries)
- Micrometer + Prometheus
- OpenTelemetry (traces)
- Flyway (DB migrations)

### Build/tooling
- Java 21 (LTS)
- Maven (or Gradle)
- Checkstyle + SpotBugs + PMD
- JUnit 5 + Mockito + AssertJ
- JaCoCo for coverage

---

## 8) API & Contract Guidelines

- Use OpenAPI/Swagger for service contracts.
- Version external APIs (`/api/v1/...`).
- Use standardized error responses (`code`, `message`, `details`, `traceId`).
- Enforce request validation at the edge.
- Keep domain models isolated from transport DTOs.

---

## 9) Security Baseline

- JWT access tokens for API auth.
- OAuth2 for social login/federation expansion.
- Service-to-service auth token strategy (later phase).
- Encrypt secrets with environment-based secret management.
- Validate and sanitize all external inputs.

---

## 10) Suggested Delivery Roadmap

### Milestone 0 — Foundation
- Initialize monorepo modules
- Add common libraries (`common-events`, `common-security`)
- Add Docker Compose with infra dependencies
- Set up CI (build + test + static checks)

### Milestone 1 — User + Media
- Ship user auth and profile flows
- Ship track upload + metadata
- Emit `TrackUploaded` events

### Milestone 2 — Streaming + Events
- Build chunk streaming endpoints
- Emit playback lifecycle events
- Add Redis caching for hot manifests

### Milestone 3 — Recommendations + Analytics
- Consume playback events
- Build trending and because-you-listened APIs
- Build active users/minute + skip-rate analytics APIs

### Milestone 4 — Production hardening
- SLOs and dashboards
- Dead-letter queues and replay tools
- Load testing and partition tuning

---

## 11) Interview Talking Points This Project Enables

- Event-driven pipelines and consumer groups
- Stream processing and aggregation windows
- Partition-key tradeoffs and ordering guarantees
- Idempotency strategies in distributed systems
- Caching strategy for hot tracks with Redis
- Backpressure and retry/DLQ handling
- Horizontal scaling patterns to millions of users

---

## 12) Current Status + Next Step

Implemented now:
1. Root multi-module Maven project
2. `services/user-service` and `services/media-service`
3. Shared libs: `common-events`, `common-security`, `common-observability`
4. Root `docker-compose.yml` with Postgres/Redis/Redpanda/MinIO/pgAdmin and both services
5. DB-backed user registration (`users` table via Flyway migration)
6. JWT login endpoint and bearer-token auth for protected routes
7. Follow/unfollow user capability (FR-104) backed by `user_follows` table
8. Playlist CRUD capability (FR-105) backed by `playlists` table
9. Media track metadata workflow (upload + list) with DB persistence and dummy `fileUrl`
10. React app track pages (`/tracks`, `/tracks/upload`) for listing and uploading tracks
11. Same-origin API proxy in web container (`http://localhost:8080`) to reduce CORS issues
12. Upload hardening: multipart size limits + `413` API handling + frontend non-JSON-safe error parsing
13. Baseline API conventions (`/api/v1` + standard error contract) and CI workflow
14. Repo hygiene updates: `.mp3` ignored in Git and Docker build contexts
15. User discovery endpoint (`GET /api/v1/users/discover`) to support follow suggestions
16. Following page upgraded to show discoverable users with one-click follow/unfollow actions

Next step:
1. Implement FR-106 user preferences.
2. Add filtering/search and paging on discover users list for scale.
3. Improve Following page with richer user profile cards and follower counts.
4. Retire legacy demo UI (`http://localhost:8088`) once all smoke-test flows are covered in React app.
