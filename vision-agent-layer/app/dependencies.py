from app.core.config import settings
from app.pipeline.emotion_classifier import EmotionClassifier
from app.pipeline.video_processor import VideoProcessor
from app.pipeline.yolo_face_detector import YoloFaceDetector

_detector = YoloFaceDetector(settings.vision_yolo_model_path)
_emotion_classifier = EmotionClassifier(settings.vision_emotion_model_id)
_processor = VideoProcessor(_detector, _emotion_classifier, settings.distress_emotions_list())


def get_video_processor() -> VideoProcessor:
    return _processor
