# Quietus AI Implementation Roadmap

## Phase 1: Architecture Setup
1. Finalize contracts and schema (done in docs).
2. Create monorepo structure and environment contract.
3. Add baseline migration script.
4. Add Docker Compose service skeleton.

## Phase 2: Backend Base
1. Spring Boot project setup with layered package structure.
2. JWT auth (register/login), RBAC enforcement.
3. Submit text endpoint and persistence layer.
4. Global exception handler, validation, audit logging.

## Phase 3: AI Text Service
1. Dataset ingestion from public distress/mental-health dataset.
2. Preprocessing and train/eval pipeline.
3. Model serialization and `/v1/inference/predict` endpoint.
4. Health and model metadata endpoints.

## Phase 4: Integration (Text + Vision)
1. Backend integration with text AI service.
2. Vision Agent service integration with Stream.
3. Internal event ingestion pipeline for video signals.
4. End-to-end persistence validation.

## Phase 5: Frontend
1. Auth screens and token-aware routing.
2. Text submission and classification UI.
3. Live video session monitoring UI.
4. Admin analytics dashboard (charts from real API data).

## Phase 6: Deployment and Hardening
1. Dockerfiles and Compose finalization.
2. Health checks, startup ordering, retry policies.
3. Security hardening and env secret handling.
4. Final end-to-end demo run with real model and real DB data.
