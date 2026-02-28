# Quietus AI

Quietus AI is a real-time multimodal emotional distress detection platform.
It combines video intelligence (Vision Agents), audio emotion signals, and text distress classification into a fused risk score with optional LLM reasoning.

## Current Scope
The project is restarted with a multimodal-first roadmap.

Current implementation state:
1. Phase 1 backend foundation (Spring Boot)
2. JWT auth and RBAC
3. Session lifecycle APIs
4. Multimodal-ready PostgreSQL schema

## Architecture
Services:
1. `frontend` (React + WebRTC)
2. `backend` (Spring Boot API Gateway)
3. `vision-agent-layer` (FastAPI + Vision Agents SDK + YOLO + emotion model)
4. `audio-service` (FastAPI SER)
5. `nlp-service` (FastAPI transcription + distress classifier)
6. `postgres`

Flow:
1. Client starts session and uploads 20-30 second chunks.
2. Backend calls vision/audio/nlp services in parallel.
3. Backend fuses modality scores and stores final risk.
4. LLM generates structured explanation from modality outputs.

## Repository Structure
```text
quietus-ai/
├── README.md
├── docs/
├── backend/
├── vision-agent-layer/
├── audio-service/
├── nlp-service/
├── frontend/
└── infra/docker/
```

## Source of Truth Docs
1. `docs/architecture.md`
2. `docs/api-contracts.md`
3. `docs/database-schema.md`
4. `docs/implementation-roadmap.md`

## Local Runtime (after services are implemented)
1. Copy `infra/docker/.env.example` to `infra/docker/.env`
2. Configure secrets/keys
3. Run `docker compose up --build` from `infra/docker`
4. Place YOLO weights at `vision-agent-layer/models/yolov8n-face.pt`

## Constraints
1. No dummy data
2. No mock AI
3. Real open-source models only
4. LLM used only for reasoning/explanation, not raw detection

## Current Runtime Status
1. `backend`, `vision-agent-layer`, `audio-service`, `frontend` are implemented and runnable.
2. `nlp-service` is included as a runnable service skeleton (health + explicit `501` for analysis endpoint) to keep Docker Compose startup clean until full NLP phase implementation.
