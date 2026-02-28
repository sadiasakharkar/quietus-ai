from dataclasses import dataclass
from typing import List

import cv2
import numpy as np

from app.pipeline.emotion_classifier import EmotionClassifier, EmotionPrediction, aggregate_probabilities
from app.pipeline.yolo_face_detector import YoloFaceDetector
from app.schemas.vision import FaceDetection, FaceEmotionPrediction


@dataclass
class VisionProcessingResult:
    frames_processed: int
    face_detections: List[FaceDetection]
    face_emotion_predictions: List[FaceEmotionPrediction]
    top_emotion: str
    top_emotion_confidence: float
    distress_score: float
    emotion_probabilities: dict[str, float]


class VideoProcessor:
    def __init__(self, detector: YoloFaceDetector, emotion_classifier: EmotionClassifier, distress_emotions: List[str]) -> None:
        self.detector = detector
        self.emotion_classifier = emotion_classifier
        self.distress_emotions = {label.lower() for label in distress_emotions}

    @property
    def model_version(self) -> str:
        return f"face={self.detector.model_version};emotion={self.emotion_classifier.model_version}"

    def analyze_video(self, file_path: str, frame_interval_ms: int, max_frames: int) -> VisionProcessingResult:
        capture = cv2.VideoCapture(file_path)
        if not capture.isOpened():
            raise ValueError("Unable to open video file")

        fps = capture.get(cv2.CAP_PROP_FPS)
        if fps <= 0:
            fps = 25.0

        frame_step = max(int((frame_interval_ms / 1000.0) * fps), 1)

        frame_index = 0
        sampled = 0
        detections: List[FaceDetection] = []
        emotion_predictions: List[FaceEmotionPrediction] = []

        while sampled < max_frames:
            ok, frame = capture.read()
            if not ok:
                break

            if frame_index % frame_step == 0:
                timestamp_ms = int((frame_index / fps) * 1000)
                frame_detections = self.detector.detect_faces(frame, frame_index=frame_index, timestamp_ms=timestamp_ms)
                detections.extend(frame_detections)

                for detection in frame_detections:
                    face_crop = self._crop_face(frame, detection)
                    emotion_result: EmotionPrediction = self.emotion_classifier.classify(face_crop)
                    emotion_predictions.append(
                        FaceEmotionPrediction(
                            frame_index=detection.frame_index,
                            timestamp_ms=detection.timestamp_ms,
                            face_confidence=detection.confidence,
                            bbox=detection.bbox,
                            emotion=emotion_result.emotion,
                            emotion_confidence=emotion_result.confidence,
                            emotion_probabilities=emotion_result.probabilities,
                        )
                    )

                sampled += 1

            frame_index += 1

        capture.release()

        aggregated_probs = aggregate_probabilities([prediction.emotion_probabilities for prediction in emotion_predictions])
        top_emotion = max(aggregated_probs, key=aggregated_probs.get) if aggregated_probs else "unknown"
        top_confidence = aggregated_probs.get(top_emotion, 0.0)
        distress_score = sum(
            score for label, score in aggregated_probs.items() if label.lower() in self.distress_emotions
        )

        return VisionProcessingResult(
            frames_processed=sampled,
            face_detections=detections,
            face_emotion_predictions=emotion_predictions,
            top_emotion=top_emotion,
            top_emotion_confidence=top_confidence,
            distress_score=min(max(distress_score, 0.0), 1.0),
            emotion_probabilities=aggregated_probs,
        )

    @staticmethod
    def _crop_face(frame: np.ndarray, detection: FaceDetection) -> np.ndarray:
        height, width = frame.shape[:2]

        x1 = max(int(detection.bbox.x1), 0)
        y1 = max(int(detection.bbox.y1), 0)
        x2 = min(int(detection.bbox.x2), width)
        y2 = min(int(detection.bbox.y2), height)

        if x2 <= x1 or y2 <= y1:
            return np.zeros((1, 1, 3), dtype=np.uint8)

        return frame[y1:y2, x1:x2]
