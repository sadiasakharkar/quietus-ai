from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    vision_service_port: int = 8100
    vision_yolo_model_path: str = "/app/models/yolov8n-face.pt"
    vision_default_frame_interval_ms: int = 500
    vision_max_frames: int = 64
    vision_max_file_size_mb: int = 100

    model_config = SettingsConfigDict(env_prefix="", case_sensitive=False)


settings = Settings()
