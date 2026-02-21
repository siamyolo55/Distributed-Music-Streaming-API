# Distributed Music Streaming API (Spotify-inspired)

**distributed systems project** to design and build a Spotify-like backend using **Java + Spring Boot**, **Docker**, and event-driven architecture.

This repository starts with an architecture-first README so the implementation can evolve in a structured, production-minded way.

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
- Follow/unfollow artists
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

### Phase 1 local stack (recommended)
Use `docker-compose` to run:
- PostgreSQL
- Redis
- Kafka + Zookeeper (or Redpanda as alternative)
- MinIO
- One or more Spring Boot services

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

## 12) Immediate Next Step

Create the project skeleton with:
1. root `docker-compose.yml`
2. `/services/user-service` Spring Boot app
3. `/services/media-service` Spring Boot app
4. shared `common-events` library
5. first Kafka topic + event contract (`TrackStarted`)

Then iterate service by service.

---

If you want, I can generate the **entire initial scaffold** next (Spring Boot apps, Docker Compose, Kafka/Redis/Postgres config, and starter endpoints).
