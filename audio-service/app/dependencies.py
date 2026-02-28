from app.core.config import settings
from app.pipeline.audio_processor import AudioProcessor
from app.pipeline.ser_classifier import SerClassifier

_classifier = SerClassifier(settings.audio_ser_model_id, settings.distress_emotions_list())
_processor = AudioProcessor(
    classifier=_classifier,
    target_sample_rate=settings.audio_target_sample_rate,
    chunk_duration_ms=settings.audio_chunk_duration_ms,
    hop_ms=settings.audio_chunk_hop_ms,
    max_duration_seconds=settings.audio_max_duration_seconds,
)


def get_audio_processor() -> AudioProcessor:
    return _processor
