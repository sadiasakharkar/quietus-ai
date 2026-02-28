from typing import List

from pydantic import BaseModel, Field


class BoundingBox(BaseModel):
    x1: float
    y1: float
    x2: float
    y2: float


class FaceDetection(BaseModel):
    frame_index: int
    timestamp_ms: int
    confidence: float = Field(ge=0.0, le=1.0)
    bbox: BoundingBox


class VisionAnalyzeResponse(BaseModel):
    session_id: str
    chunk_id: str
    model_version: str
    frames_processed: int
    frames_with_faces: int
    faces_detected_ratio: float = Field(ge=0.0, le=1.0)
    max_face_confidence: float = Field(ge=0.0, le=1.0)
    face_detections: List[FaceDetection]
