# Quietus AI Multimodal Architecture

## 1. Overview
Quietus AI is a real-time multimodal distress detection platform. It processes video, audio, and text in parallel and computes a fused risk score with an optional LLM explanation.

Core services:
1. Frontend (`React + WebRTC`)
2. Backend API Gateway (`Spring Boot`)
3. Vision processing (`vision-agent-layer`, FastAPI + Vision Agents SDK + YOLO + emotion model)
4. Audio processing (`audio-service`, FastAPI)
5. NLP processing (`nlp-service`, FastAPI)
6. PostgreSQL (`system of record`)

## 2. Service Communication
1. Frontend -> Backend: HTTPS + JWT
2. Backend -> Vision/Audio/NLP: internal async HTTP calls
3. Backend -> PostgreSQL: transactional persistence for session + predictions + fusion + explanation
4. Backend -> LLM provider (Gemini/OpenAI): reasoning only (post-fusion)

## 3. Real-Time Flow
1. User logs in and starts session.
2. Frontend captures webcam + mic + optional typed text.
3. Frontend sends 20-30 second chunk payload to backend.
4. Backend fan-outs requests in parallel to vision/audio/nlp services.
5. Backend runs weighted fusion from returned scores.
6. Backend optionally calls LLM for structured explanation.
7. Backend stores raw modality outputs + fused result + explanation.
8. Frontend receives result target within 5-8s after chunk upload completes.

## 4. Latency Strategy
1. Parallel modality processing.
2. Per-service timeout budget.
3. Best-effort fusion with missing-modality reweighting.
4. One retry for transient failures only.
