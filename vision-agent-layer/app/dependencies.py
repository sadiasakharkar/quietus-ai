from app.core.config import settings
from app.pipeline.video_processor import VideoProcessor
from app.pipeline.yolo_face_detector import YoloFaceDetector

_detector = YoloFaceDetector(settings.vision_yolo_model_path)
_processor = VideoProcessor(_detector)


def get_video_processor() -> VideoProcessor:
    return _processor
