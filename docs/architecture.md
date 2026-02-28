# Quietus AI Architecture

## 1. System Overview
Quietus AI is a multi-service distress detection platform with both text and real-time video intelligence.

Services:
1. Frontend (`React + Vite + Tailwind`)
2. Backend API (`Spring Boot`)
3. Text AI Service (`FastAPI`)
4. Vision Agent Service (`Stream Vision Agents SDK`)
5. PostgreSQL (`system of record`)

## 2. Service Communication
1. Frontend -> Backend API over HTTPS (JWT)
2. Backend -> Text AI over private network HTTP
3. Backend -> Vision Agent service over private network HTTP/WebSocket integration boundary
4. Vision Agent -> Backend internal ingestion endpoint for event writes
5. Backend -> PostgreSQL for all persistent data

All analytics are generated from persisted PostgreSQL records.

## 3. Why This Architecture
1. Separation of concerns enables parallel team execution.
2. Text and video inference workloads scale independently.
3. Backend remains source of business truth and security enforcement.
4. Real-time events are ingested as durable records for reproducible analytics.

## 4. Request/Response Path Summary
1. User authenticates and gets JWT from Backend.
2. User submits text or starts video monitoring session.
3. Backend orchestrates AI calls and stores results.
4. Admin dashboard reads only backend analytics APIs.
