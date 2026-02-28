from dataclasses import dataclass
from typing import Dict, List

import cv2
import numpy as np
import torch
from transformers import AutoImageProcessor, AutoModelForImageClassification


@dataclass
class EmotionPrediction:
    emotion: str
    confidence: float
    probabilities: Dict[str, float]


class EmotionClassifier:
    def __init__(self, model_id: str) -> None:
        self.model_id = model_id
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.processor = AutoImageProcessor.from_pretrained(model_id)
        self.model = AutoModelForImageClassification.from_pretrained(model_id).to(self.device)
        self.model.eval()

    @property
    def model_version(self) -> str:
        return self.model_id

    def classify(self, face_bgr_image: np.ndarray) -> EmotionPrediction:
        if face_bgr_image.size == 0:
            return EmotionPrediction(emotion="unknown", confidence=0.0, probabilities={})

        face_rgb = cv2.cvtColor(face_bgr_image, cv2.COLOR_BGR2RGB)
        inputs = self.processor(images=face_rgb, return_tensors="pt")
        inputs = {k: v.to(self.device) for k, v in inputs.items()}

        with torch.no_grad():
            logits = self.model(**inputs).logits
            probs = torch.softmax(logits, dim=-1)[0]

        id2label = self.model.config.id2label
        probabilities: Dict[str, float] = {}

        for idx, prob in enumerate(probs):
            label = str(id2label.get(idx, f"label_{idx}")).lower()
            probabilities[label] = float(prob.item())

        if not probabilities:
            return EmotionPrediction(emotion="unknown", confidence=0.0, probabilities={})

        emotion = max(probabilities, key=probabilities.get)
        confidence = probabilities[emotion]
        return EmotionPrediction(emotion=emotion, confidence=confidence, probabilities=probabilities)


def aggregate_probabilities(probabilities_list: List[Dict[str, float]]) -> Dict[str, float]:
    if not probabilities_list:
        return {}

    totals: Dict[str, float] = {}
    for probs in probabilities_list:
        for label, score in probs.items():
            totals[label] = totals.get(label, 0.0) + score

    count = len(probabilities_list)
    return {label: score / count for label, score in totals.items()}
