from functools import lru_cache

from app.core.config import settings
from app.pipeline.emotion_classifier import EmotionClassifier
from app.pipeline.video_processor import VideoProcessor
from app.pipeline.yolo_face_detector import YoloFaceDetector


@lru_cache(maxsize=1)
def get_video_processor() -> VideoProcessor:
    detector = YoloFaceDetector(settings.vision_yolo_model_path)
    emotion_classifier = EmotionClassifier(settings.vision_emotion_model_id)
    return VideoProcessor(detector, emotion_classifier, settings.distress_emotions_list())
