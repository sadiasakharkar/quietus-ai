from typing import List

import numpy as np
from ultralytics import YOLO

from app.schemas.vision import BoundingBox, FaceDetection


class YoloFaceDetector:
    def __init__(self, model_path: str) -> None:
        self.model_path = model_path
        self.model = YOLO(model_path)

    @property
    def model_version(self) -> str:
        return self.model_path

    def detect_faces(self, frame: np.ndarray, frame_index: int, timestamp_ms: int) -> List[FaceDetection]:
        results = self.model.predict(source=frame, verbose=False)
        detections: List[FaceDetection] = []

        for result in results:
            if result.boxes is None:
                continue

            for box in result.boxes:
                coords = box.xyxy[0].tolist()
                confidence = float(box.conf[0].item())
                detections.append(
                    FaceDetection(
                        frame_index=frame_index,
                        timestamp_ms=timestamp_ms,
                        confidence=confidence,
                        bbox=BoundingBox(
                            x1=float(coords[0]),
                            y1=float(coords[1]),
                            x2=float(coords[2]),
                            y2=float(coords[3]),
                        ),
                    )
                )

        return detections
