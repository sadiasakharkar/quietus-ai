# Quietus AI Implementation Roadmap (Multimodal)

## Phase 1: Backend foundation + auth
1. Spring Boot layered foundation.
2. JWT auth and RBAC.
3. Session lifecycle APIs (`start`, `get`, `end`).
4. Multimodal-ready DB schema.

## Phase 2: Vision Agents integration with YOLO
1. Vision agent integration layer using Stream Vision Agents SDK.
2. Frame sampling and YOLO face detection.
3. Structured vision prediction output.

## Phase 3: Emotion model integration (Roboflow or Moondream)
1. Emotion classification on detected faces.
2. Session/chunk level aggregation.

## Phase 4: Audio emotion detection
1. Audio chunk pipeline.
2. MFCC feature extraction and SER model.

## Phase 5: Fusion engine
1. Weighted multimodal fusion.
2. Missing-modality fallback logic.
3. Persist fusion outputs per session chunk.

## Phase 6: LLM reasoning integration
1. Structured reasoning prompt.
2. JSON explanation output persistence.
3. Session chunk explain/get explanation APIs.

## Phase 7: Frontend UI + real-time capture
1. React webcam + mic capture.
2. Session controls and risk display.

## Phase 8: Dockerization and demo optimization
1. Final container orchestration.
2. Performance tuning to 5-8s response target.
