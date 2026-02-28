import os
import tempfile

from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.core.config import settings
from app.dependencies import get_video_processor
from app.schemas.vision import VisionAnalyzeResponse

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {
        "status": "ok",
        "service": "vision-agent-layer",
        "modelLoaded": get_video_processor.cache_info().currsize > 0,
    }


@router.get("/v1/models")
def models() -> dict:
    try:
        processor = get_video_processor()
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(status_code=503, detail=f"Vision models unavailable: {exc}") from exc
    return {
        "vision": {
            "faceDetector": "YOLO",
            "faceModelPath": settings.vision_yolo_model_path,
            "emotionModel": processor.emotion_classifier.model_version,
            "distressEmotions": settings.distress_emotions_list(),
        }
    }


@router.post("/v1/vision/analyze", response_model=VisionAnalyzeResponse)
async def analyze_video(
    session_id: str = Form(...),
    chunk_id: str = Form(...),
    frame_interval_ms: int = Form(default=settings.vision_default_frame_interval_ms),
    max_frames: int = Form(default=settings.vision_max_frames),
    video_file: UploadFile = File(...),
) -> VisionAnalyzeResponse:
    if video_file.content_type is None or not video_file.content_type.startswith("video/"):
        raise HTTPException(status_code=400, detail="video_file must be a valid video MIME type")

    contents = await video_file.read()
    file_size_mb = len(contents) / (1024 * 1024)
    if file_size_mb > settings.vision_max_file_size_mb:
        raise HTTPException(status_code=413, detail="Uploaded video exceeds size limit")

    try:
        processor = get_video_processor()
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(status_code=503, detail=f"Vision models unavailable: {exc}") from exc

    with tempfile.NamedTemporaryFile(delete=False, suffix=".mp4") as temp_file:
        temp_file.write(contents)
        temp_path = temp_file.name

    try:
        result = processor.analyze_video(
            temp_path,
            frame_interval_ms=frame_interval_ms,
            max_frames=max_frames,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

    frames_with_faces = len({d.frame_index for d in result.face_detections})
    ratio = (frames_with_faces / result.frames_processed) if result.frames_processed > 0 else 0.0
    max_conf = max((d.confidence for d in result.face_detections), default=0.0)

    return VisionAnalyzeResponse(
        session_id=session_id,
        chunk_id=chunk_id,
        model_version=processor.model_version,
        frames_processed=result.frames_processed,
        frames_with_faces=frames_with_faces,
        faces_detected_ratio=ratio,
        max_face_confidence=max_conf,
        top_emotion=result.top_emotion,
        top_emotion_confidence=result.top_emotion_confidence,
        distress_score=result.distress_score,
        emotion_probabilities=result.emotion_probabilities,
        face_detections=result.face_detections,
        face_emotion_predictions=result.face_emotion_predictions,
    )
