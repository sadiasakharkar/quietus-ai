# Quietus AI

Quietus AI is a production-structured, end-to-end intelligent distress detection and response platform.
It combines NLP-based text risk analysis with real-time video intelligence using Stream Vision Agents, stores all outcomes in PostgreSQL, and serves dynamic analytics through secure admin APIs.

## Vision
Build a real, demo-ready, scalable system that detects emotional distress from user input (text + live video), classifies risk with confidence, and powers operational insights from real database records.

## Core Principles
- No dummy data or placeholder analytics.
- No mock AI outputs in production flow.
- Clean architecture and layered boundaries.
- Security-first (JWT, RBAC, service-to-service protection).
- Docker-first local reproducibility.

## System Architecture

### Services
1. `frontend` - React + Vite + Tailwind (user/admin UI)
2. `backend-api` - Spring Boot REST API (auth, orchestration, persistence, analytics)
3. `ai-service` - FastAPI (text model training/inference)
4. `vision-agent-service` - Stream Vision Agents SDK (real-time video inference)
5. `postgres` - PostgreSQL (system of record)

### Communication
- Frontend -> Backend: HTTPS + JWT
- Backend -> AI Service: internal HTTP + service token
- Vision Agent Service -> Backend: internal event ingestion endpoint
- Backend -> PostgreSQL: transactional persistence + analytics queries

## Technology Stack
- Frontend: React, Vite, Tailwind CSS
- Backend: Java, Spring Boot, Spring Security, Flyway
- AI Text Service: Python, FastAPI, NLP/ML pipeline
- Vision Service: Stream Vision Agents SDK + compatible vision/LLM models
- Database: PostgreSQL
- Infra: Docker, Docker Compose

## Current Repository Status
This repository is currently in **Phase 1 (Architecture Setup)**.

Completed:
- High-level architecture and service boundaries
- Stable API contracts (`v1`)
- Database schema and indexing strategy
- Initial migration script
- Docker Compose and environment skeleton

## Repository Structure

```text
quietus-ai/
├── README.md
├── docs/
│   ├── architecture.md
│   ├── api-contracts.md
│   ├── database-schema.md
│   └── implementation-roadmap.md
├── backend/
│   └── src/main/resources/db/migration/
│       └── V1__initial_schema.sql
├── ai-service/
├── vision-agent-service/
├── frontend/
└── infra/
    └── docker/
        ├── docker-compose.yml
        └── .env.example
```

## Source of Truth Documents
- Architecture: [`docs/architecture.md`](docs/architecture.md)
- API contracts: [`docs/api-contracts.md`](docs/api-contracts.md)
- Database schema: [`docs/database-schema.md`](docs/database-schema.md)
- Delivery plan: [`docs/implementation-roadmap.md`](docs/implementation-roadmap.md)

## Data & AI Policy
- All analytics must be generated from persisted `analysis_records` and `video_analysis_events`.
- Text model must be trained on a real public distress/mental-health dataset.
- Model outputs must include confidence and model version metadata.
- No hardcoded responses for analysis or dashboards.

## Security Model
- JWT-based stateless authentication
- Password hashing (bcrypt/argon2)
- Role-based access control (`USER`, `ADMIN`)
- Internal service token for AI and Vision service communication
- Backend-owned authorization boundaries (frontend never trusted for roles)

## Local Development (Planned Runtime)

### Prerequisites
- Docker Desktop (latest)
- Docker Compose v2+

### Environment
1. Copy `infra/docker/.env.example` to `infra/docker/.env`
2. Set secure values for:
   - `JWT_SECRET`
   - `INTERNAL_SERVICE_TOKEN`
   - `STREAM_API_KEY`, `STREAM_API_SECRET`
   - `OPENAI_API_KEY` and/or `GOOGLE_API_KEY`

### Startup (once services are implemented)
Run from `infra/docker`:
```bash
docker compose up --build
```

## API Surface (v1)
- Auth: register/login
- Text analysis: submit + history
- Video sessions: create/end
- Internal ingestion: vision events
- Admin analytics: overview, distributions, trends

See full contract definitions in [`docs/api-contracts.md`](docs/api-contracts.md).

## Roadmap
1. Phase 1: Architecture setup (completed)
2. Phase 2: Backend base (JWT, RBAC, core APIs)
3. Phase 3: AI text pipeline + inference service
4. Phase 4: Text + Vision integration
5. Phase 5: Frontend experiences + admin dashboard
6. Phase 6: Deployment hardening and E2E validation

## Engineering Standards
- Layered architecture and modular boundaries
- Strict request validation + centralized error responses
- Migration-driven database changes only
- No unnecessary dependencies
- Production-oriented logging and observability hooks

## Contribution Workflow
1. Create a branch prefixed with `codex/` or `feature/`
2. Implement one phase task at a time
3. Keep API/schema changes synchronized with `docs/`
4. Open PR with architecture impact notes and test evidence

## License
License will be added in a dedicated `LICENSE` file.
