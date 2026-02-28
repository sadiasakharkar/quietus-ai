# Quietus AI API Contracts (Multimodal v1)

## 1. Authentication

### POST /api/v1/auth/register
Request:
```json
{
  "email": "user@example.com",
  "password": "StrongPass!234",
  "fullName": "Alex Doe"
}
```

### POST /api/v1/auth/login
Request:
```json
{
  "email": "user@example.com",
  "password": "StrongPass!234"
}
```
Response:
```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "uuid",
    "email": "user@example.com",
    "roles": ["USER"]
  }
}
```

## 2. Session APIs

### POST /api/v1/sessions
Auth: USER/ADMIN
Response:
```json
{
  "sessionId": "uuid",
  "status": "ACTIVE",
  "startedAt": "2026-02-28T11:00:00Z"
}
```

### POST /api/v1/sessions/{sessionId}/end
Auth: USER/ADMIN
Response:
```json
{
  "sessionId": "uuid",
  "userId": "uuid",
  "status": "ENDED",
  "startedAt": "2026-02-28T11:00:00Z",
  "endedAt": "2026-02-28T11:01:00Z"
}
```

### GET /api/v1/sessions/{sessionId}
Auth: USER/ADMIN

## 3. Future Phase Contracts (Not implemented in Phase 1)
1. `/api/v1/sessions/{sessionId}/chunks` upload endpoint.
2. `/api/v1/admin/analytics/*` multimodal dashboards.

## 4. Phase 5 Contracts (Implemented)

### POST /api/v1/sessions/{sessionId}/chunks/{chunkId}/vision
Auth: USER/ADMIN
```json
{
  "emotion": "sad",
  "confidence": 0.83,
  "distressScore": 0.76,
  "framesProcessed": 40
}
```

### POST /api/v1/sessions/{sessionId}/chunks/{chunkId}/audio
Auth: USER/ADMIN
```json
{
  "emotion": "distress",
  "stressScore": 0.78,
  "confidence": 0.81
}
```

### POST /api/v1/sessions/{sessionId}/chunks/{chunkId}/text
Auth: USER/ADMIN
```json
{
  "transcript": "I feel overwhelmed and tired.",
  "riskLevel": "HIGH",
  "distressProbability": 0.89,
  "confidence": 0.91
}
```

### POST /api/v1/sessions/{sessionId}/chunks/{chunkId}/fuse
Auth: USER/ADMIN

### GET /api/v1/sessions/{sessionId}/chunks/{chunkId}/fusion
Auth: USER/ADMIN

### POST /api/v1/sessions/{sessionId}/chunks/{chunkId}/explain
Auth: USER/ADMIN
Response:
```json
{
  "sessionId": "uuid",
  "chunkId": "uuid",
  "summary": "Consistent distress signals across available modalities.",
  "keySignals": ["High distress score in text", "Sustained negative affect in vision"],
  "recommendedActionLevel": "PRIORITY_REVIEW",
  "explanationConfidence": 0.84,
  "createdAt": "2026-02-28T11:10:00Z"
}
```

### GET /api/v1/sessions/{sessionId}/chunks/{chunkId}/explanation
Auth: USER/ADMIN

## 5. Frontend Runtime Service Calls (Phase 7)
1. `POST /vision-api/v1/vision/analyze` (Vite proxy to vision-agent-layer)
2. `POST /audio-api/v1/audio/analyze` (Vite proxy to audio-service)

## 6. Error Contract
```json
{
  "timestamp": "2026-02-28T11:10:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/v1/sessions",
  "requestId": "uuid"
}
```
