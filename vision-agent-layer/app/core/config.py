from typing import List

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    vision_service_port: int = 8100
    vision_yolo_model_path: str = "/app/models/yolov8n-face.pt"
    vision_emotion_model_id: str = "trpakov/vit-face-expression"
    vision_distress_emotions: str = "sad,fear,angry,disgust"
    vision_default_frame_interval_ms: int = 500
    vision_max_frames: int = 64
    vision_max_file_size_mb: int = 100

    model_config = SettingsConfigDict(env_prefix="", case_sensitive=False)

    def distress_emotions_list(self) -> List[str]:
        return [emotion.strip().lower() for emotion in self.vision_distress_emotions.split(",") if emotion.strip()]


settings = Settings()
