from typing import Dict, List

from pydantic import BaseModel, Field


class AudioSegmentPrediction(BaseModel):
    segment_index: int
    start_ms: int
    end_ms: int
    emotion: str
    confidence: float = Field(ge=0.0, le=1.0)
    distress_score: float = Field(ge=0.0, le=1.0)
    emotion_probabilities: Dict[str, float]


class AudioAnalyzeResponse(BaseModel):
    session_id: str
    chunk_id: str
    model_version: str
    sample_rate: int
    duration_seconds: float
    segments_processed: int
    stress_score: float = Field(ge=0.0, le=1.0)
    emotion: str
    confidence: float = Field(ge=0.0, le=1.0)
    emotion_probabilities: Dict[str, float]
    mfcc_summary: Dict[str, float]
    segment_predictions: List[AudioSegmentPrediction]
