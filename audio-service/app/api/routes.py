import os
import tempfile

from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.core.config import settings
from app.dependencies import get_audio_processor
from app.schemas.audio import AudioAnalyzeResponse

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "audio-service"}


@router.get("/v1/models")
def models() -> dict:
    processor = get_audio_processor()
    return {
        "audio": {
            "serModel": processor.model_version,
            "chunkDurationMs": settings.audio_chunk_duration_ms,
            "chunkHopMs": settings.audio_chunk_hop_ms,
            "distressEmotions": settings.distress_emotions_list(),
        }
    }


@router.post("/v1/audio/analyze", response_model=AudioAnalyzeResponse)
async def analyze_audio(
    session_id: str = Form(...),
    chunk_id: str = Form(...),
    audio_file: UploadFile = File(...),
) -> AudioAnalyzeResponse:
    if audio_file.content_type is None or not audio_file.content_type.startswith("audio/"):
        raise HTTPException(status_code=400, detail="audio_file must be a valid audio MIME type")

    contents = await audio_file.read()
    size_mb = len(contents) / (1024 * 1024)
    if size_mb > settings.audio_max_file_size_mb:
        raise HTTPException(status_code=413, detail="Uploaded audio exceeds size limit")

    processor = get_audio_processor()

    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_file:
        temp_file.write(contents)
        temp_path = temp_file.name

    try:
        result = processor.analyze_audio(temp_path)
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(status_code=400, detail=f"Audio processing failed: {exc}") from exc
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

    return AudioAnalyzeResponse(
        session_id=session_id,
        chunk_id=chunk_id,
        model_version=processor.model_version,
        sample_rate=result.sample_rate,
        duration_seconds=result.duration_seconds,
        segments_processed=result.segments_processed,
        stress_score=result.stress_score,
        emotion=result.emotion,
        confidence=result.confidence,
        emotion_probabilities=result.emotion_probabilities,
        mfcc_summary=result.mfcc_summary,
        segment_predictions=result.segment_predictions,
    )
