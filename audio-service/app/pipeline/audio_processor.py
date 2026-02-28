from dataclasses import dataclass
from typing import Dict, List

import librosa
import numpy as np

from app.pipeline.audio_chunker import AudioSegment, chunk_audio
from app.pipeline.mfcc_features import extract_mfcc_summary
from app.pipeline.ser_classifier import SerClassifier, SerPrediction
from app.schemas.audio import AudioSegmentPrediction


@dataclass
class AudioProcessingResult:
    sample_rate: int
    duration_seconds: float
    segments_processed: int
    stress_score: float
    emotion: str
    confidence: float
    emotion_probabilities: Dict[str, float]
    mfcc_summary: Dict[str, float]
    segment_predictions: List[AudioSegmentPrediction]


class AudioProcessor:
    def __init__(
        self,
        classifier: SerClassifier,
        target_sample_rate: int,
        chunk_duration_ms: int,
        hop_ms: int,
        max_duration_seconds: int,
    ) -> None:
        self.classifier = classifier
        self.target_sample_rate = target_sample_rate
        self.chunk_duration_ms = chunk_duration_ms
        self.hop_ms = hop_ms
        self.max_duration_seconds = max_duration_seconds

    @property
    def model_version(self) -> str:
        return self.classifier.model_version

    def analyze_audio(self, file_path: str) -> AudioProcessingResult:
        waveform, sample_rate = librosa.load(file_path, sr=self.target_sample_rate, mono=True)

        max_samples = int(self.max_duration_seconds * sample_rate)
        if waveform.shape[0] > max_samples:
            waveform = waveform[:max_samples]

        duration_seconds = float(waveform.shape[0] / sample_rate) if sample_rate > 0 else 0.0
        mfcc_summary = extract_mfcc_summary(waveform, sample_rate)

        segments = chunk_audio(
            waveform,
            sample_rate=sample_rate,
            chunk_duration_ms=self.chunk_duration_ms,
            hop_ms=self.hop_ms,
        )

        ser_predictions: List[SerPrediction] = []
        segment_predictions: List[AudioSegmentPrediction] = []

        for segment in segments:
            prediction = self.classifier.predict(segment.waveform, sample_rate)
            ser_predictions.append(prediction)
            segment_predictions.append(self._segment_output(segment, prediction))

        aggregated = SerClassifier.aggregate_probabilities(ser_predictions)
        top_emotion = max(aggregated, key=aggregated.get) if aggregated else "unknown"
        top_confidence = aggregated.get(top_emotion, 0.0)
        stress_score = (
            float(np.mean([prediction.distress_score for prediction in ser_predictions]))
            if ser_predictions
            else 0.0
        )

        return AudioProcessingResult(
            sample_rate=sample_rate,
            duration_seconds=duration_seconds,
            segments_processed=len(segments),
            stress_score=min(max(stress_score, 0.0), 1.0),
            emotion=top_emotion,
            confidence=min(max(top_confidence, 0.0), 1.0),
            emotion_probabilities=aggregated,
            mfcc_summary=mfcc_summary,
            segment_predictions=segment_predictions,
        )

    @staticmethod
    def _segment_output(segment: AudioSegment, prediction: SerPrediction) -> AudioSegmentPrediction:
        return AudioSegmentPrediction(
            segment_index=segment.index,
            start_ms=segment.start_ms,
            end_ms=segment.end_ms,
            emotion=prediction.emotion,
            confidence=prediction.confidence,
            distress_score=prediction.distress_score,
            emotion_probabilities=prediction.probabilities,
        )
