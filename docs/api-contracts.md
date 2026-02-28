# Quietus AI API Contracts (Stable v1)

## 1. Auth APIs

### POST /api/v1/auth/register
Request:
```json
{
  "email": "user@example.com",
  "password": "StrongPass!234",
  "fullName": "Alex Doe"
}
```
Response:
```json
{
  "userId": "8f7d9f1a-8f2f-4f67-bf14-8c5a1e8e0d2a",
  "email": "user@example.com",
  "roles": ["USER"],
  "createdAt": "2026-02-28T10:15:00Z"
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
    "userId": "8f7d9f1a-8f2f-4f67-bf14-8c5a1e8e0d2a",
    "email": "user@example.com",
    "roles": ["USER"]
  }
}
```

## 2. Text Analysis APIs

### POST /api/v1/analysis/submit
Auth: USER/ADMIN
Request:
```json
{
  "text": "I feel overwhelmed and hopeless lately.",
  "source": "WEB_APP"
}
```
Response:
```json
{
  "analysisId": "f1174cc9-9f84-4f1a-bf2a-cbf27c3f9f41",
  "label": "HIGH",
  "confidence": 0.91,
  "modelVersion": "distress-clf-1.0.0",
  "analyzedAt": "2026-02-28T10:20:00Z"
}
```

### GET /api/v1/analysis/history?limit=20&offset=0
Auth: USER/ADMIN
Response:
```json
{
  "items": [
    {
      "analysisId": "f1174cc9-9f84-4f1a-bf2a-cbf27c3f9f41",
      "text": "I feel overwhelmed and hopeless lately.",
      "label": "HIGH",
      "confidence": 0.91,
      "analyzedAt": "2026-02-28T10:20:00Z"
    }
  ],
  "total": 1
}
```

## 3. AI Service Internal APIs (Backend -> Text AI)

### POST /v1/inference/predict
Header: `X-Service-Token: <internal_token>`
Request:
```json
{
  "text": "I feel overwhelmed and hopeless lately.",
  "requestId": "f1174cc9-9f84-4f1a-bf2a-cbf27c3f9f41"
}
```
Response:
```json
{
  "label": "HIGH",
  "confidence": 0.91,
  "modelVersion": "distress-clf-1.0.0",
  "processingMs": 18
}
```

## 4. Video Session APIs

### POST /api/v1/video/sessions
Auth: USER/ADMIN
Request:
```json
{
  "sessionType": "DISTRESS_MONITORING",
  "clientPlatform": "WEB"
}
```
Response:
```json
{
  "sessionId": "0c56ffb0-21ba-4df7-92d9-f972f2ee8f3d",
  "streamSessionToken": "stream-token",
  "createdAt": "2026-02-28T11:00:00Z",
  "status": "ACTIVE"
}
```

### POST /api/v1/video/sessions/{sessionId}/end
Auth: USER/ADMIN
Response:
```json
{
  "sessionId": "0c56ffb0-21ba-4df7-92d9-f972f2ee8f3d",
  "status": "ENDED",
  "endedAt": "2026-02-28T11:30:00Z"
}
```

## 5. Vision Internal Event Ingestion (Vision -> Backend)

### POST /api/v1/internal/video/events
Header: `X-Service-Token: <internal_token>`
Request:
```json
{
  "eventId": "38b4f8bd-8a9f-4e8d-8f85-0dcdfdbbf1c4",
  "sessionId": "0c56ffb0-21ba-4df7-92d9-f972f2ee8f3d",
  "userId": "8f7d9f1a-8f2f-4f67-bf14-8c5a1e8e0d2a",
  "eventAt": "2026-02-28T11:05:10Z",
  "riskLevel": "MODERATE",
  "confidence": 0.82,
  "signals": ["agitation_pattern", "prolonged_head_down"],
  "modelVersion": "vision-stack-1.0.0",
  "latencyMs": 120
}
```
Response:
```json
{
  "accepted": true,
  "eventId": "38b4f8bd-8a9f-4e8d-8f85-0dcdfdbbf1c4",
  "storedAt": "2026-02-28T11:05:10Z"
}
```

## 6. Admin Analytics APIs

### GET /api/v1/admin/analytics/overview
Auth: ADMIN
Response:
```json
{
  "totalUsers": 1240,
  "totalTextAnalyses": 9843,
  "totalVideoEvents": 21654,
  "highRiskTextCount": 1321,
  "highRiskVideoEventCount": 2084,
  "avgTextConfidence": 0.84,
  "avgVideoConfidence": 0.79
}
```

### GET /api/v1/admin/analytics/text-risk-distribution?from=2026-02-01&to=2026-02-28
Auth: ADMIN

### GET /api/v1/admin/analytics/video-risk-distribution?from=2026-02-01&to=2026-02-28
Auth: ADMIN

### GET /api/v1/admin/analytics/risk-trend?granularity=day&from=2026-02-01&to=2026-02-28
Auth: ADMIN

## 7. Error Contract (All APIs)
```json
{
  "timestamp": "2026-02-28T11:10:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "text must be between 5 and 5000 characters",
  "path": "/api/v1/analysis/submit",
  "requestId": "f1174cc9-9f84-4f1a-bf2a-cbf27c3f9f41"
}
```
