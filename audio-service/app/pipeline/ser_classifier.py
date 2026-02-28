from dataclasses import dataclass
from typing import Dict, List

import numpy as np
from transformers import pipeline


@dataclass
class SerPrediction:
    emotion: str
    confidence: float
    probabilities: Dict[str, float]
    distress_score: float


class SerClassifier:
    def __init__(self, model_id: str, distress_emotions: List[str]) -> None:
        self.model_id = model_id
        self.distress_emotions = {emotion.lower() for emotion in distress_emotions}
        self.classifier = pipeline("audio-classification", model=model_id)

    @property
    def model_version(self) -> str:
        return self.model_id

    def predict(self, waveform: np.ndarray, sample_rate: int) -> SerPrediction:
        if waveform.size == 0:
            return SerPrediction(emotion="unknown", confidence=0.0, probabilities={}, distress_score=0.0)

        scores = self.classifier({"array": waveform.astype(np.float32), "sampling_rate": sample_rate}, top_k=None)
        probabilities: Dict[str, float] = {}

        for item in scores:
            label = self._normalize_label(str(item.get("label", "unknown")))
            score = float(item.get("score", 0.0))
            probabilities[label] = max(probabilities.get(label, 0.0), score)

        if not probabilities:
            return SerPrediction(emotion="unknown", confidence=0.0, probabilities={}, distress_score=0.0)

        emotion = max(probabilities, key=probabilities.get)
        confidence = probabilities[emotion]
        distress_score = sum(score for label, score in probabilities.items() if label in self.distress_emotions)
        distress_score = min(max(distress_score, 0.0), 1.0)

        return SerPrediction(
            emotion=emotion,
            confidence=confidence,
            probabilities=probabilities,
            distress_score=distress_score,
        )

    @staticmethod
    def aggregate_probabilities(predictions: List[SerPrediction]) -> Dict[str, float]:
        if not predictions:
            return {}

        totals: Dict[str, float] = {}
        for prediction in predictions:
            for label, score in prediction.probabilities.items():
                totals[label] = totals.get(label, 0.0) + score

        count = len(predictions)
        return {label: score / count for label, score in totals.items()}

    @staticmethod
    def _normalize_label(label: str) -> str:
        label = label.strip().lower()
        mapping = {
            "ang": "angry",
            "hap": "happy",
            "neu": "neutral",
            "sad": "sad",
            "fearful": "fear",
            "surprised": "surprise",
        }
        return mapping.get(label, label)
