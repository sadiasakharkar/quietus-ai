# Vision Agent Layer (Phase 2)

This service provides low-latency video chunk analysis for Quietus AI.

## Implemented in Phase 2
1. FastAPI service foundation
2. YOLO-based face detection from uploaded video chunks
3. Frame sampling pipeline (default: every 500ms)
4. Structured output for backend fusion pipeline

## Endpoints
1. `GET /health`
2. `GET /v1/models`
3. `POST /v1/vision/analyze` (multipart form)

Form fields for `/v1/vision/analyze`:
1. `session_id` (string)
2. `chunk_id` (string)
3. `frame_interval_ms` (int, optional)
4. `max_frames` (int, optional)
5. `video_file` (video file)

## Model
Set `VISION_YOLO_MODEL_PATH` to a YOLO face detection weight file, for example:
- `/app/models/yolov8n-face.pt`

The service fails inference if the model is missing or invalid.
