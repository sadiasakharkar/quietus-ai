from typing import List

import cv2

from app.pipeline.yolo_face_detector import YoloFaceDetector
from app.schemas.vision import FaceDetection


class VideoProcessor:
    def __init__(self, detector: YoloFaceDetector) -> None:
        self.detector = detector

    def analyze_video(self, file_path: str, frame_interval_ms: int, max_frames: int) -> tuple[int, List[FaceDetection]]:
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

        while sampled < max_frames:
            ok, frame = capture.read()
            if not ok:
                break

            if frame_index % frame_step == 0:
                timestamp_ms = int((frame_index / fps) * 1000)
                detections.extend(self.detector.detect_faces(frame, frame_index=frame_index, timestamp_ms=timestamp_ms))
                sampled += 1

            frame_index += 1

        capture.release()
        return sampled, detections
