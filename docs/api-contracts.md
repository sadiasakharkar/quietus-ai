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
2. Internal vision/audio/nlp inference callbacks.
3. `/api/v1/admin/analytics/*` multimodal dashboards.

## 4. Error Contract
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
