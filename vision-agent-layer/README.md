# Vision Agent Layer (Phase 2 + Phase 3)

This service provides low-latency video chunk analysis for Quietus AI.

## Implemented
1. FastAPI service foundation
2. YOLO-based face detection from uploaded video chunks
3. Frame sampling pipeline (default: every 500ms)
4. Emotion classification on detected face crops (open-source local model)
5. Distress-oriented emotion aggregation

## Endpoints
1. `GET /health`
2. `GET /v1/models`
3. `POST /v1/vision/analyze` (multipart form)

## Output includes
1. Face detections
2. Per-face emotion predictions
3. Aggregated emotion probabilities
4. Distress score

## Environment
1. `VISION_YOLO_MODEL_PATH`
2. `VISION_EMOTION_MODEL_ID`
3. `VISION_DISTRESS_EMOTIONS`
