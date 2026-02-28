# Audio Service (Phase 4)

Real audio emotion/stress analysis service for Quietus AI.

## Implemented
1. Audio chunking (2s windows, 1s hop)
2. MFCC feature summary extraction
3. Pretrained speech emotion recognition inference
4. Distress-oriented stress score aggregation

## Endpoints
1. `GET /health`
2. `GET /v1/models`
3. `POST /v1/audio/analyze`

## Analyze Request
Multipart form:
1. `session_id`
2. `chunk_id`
3. `audio_file`

## Analyze Response
1. Segment-level emotion predictions
2. Aggregated emotion probabilities
3. `stress_score`, `emotion`, and `confidence`
4. MFCC summary for monitoring/debug
