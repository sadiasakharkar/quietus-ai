from typing import List

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    audio_service_port: int = 8200
    audio_target_sample_rate: int = 16000
    audio_chunk_duration_ms: int = 2000
    audio_chunk_hop_ms: int = 1000
    audio_max_duration_seconds: int = 40
    audio_max_file_size_mb: int = 30
    audio_ser_model_id: str = "superb/hubert-large-superb-er"
    audio_distress_emotions: str = "angry,sad,fear,disgust,frustrated"

    model_config = SettingsConfigDict(env_prefix="", case_sensitive=False)

    def distress_emotions_list(self) -> List[str]:
        return [emotion.strip().lower() for emotion in self.audio_distress_emotions.split(",") if emotion.strip()]


settings = Settings()
