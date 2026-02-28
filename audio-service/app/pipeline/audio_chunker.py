from dataclasses import dataclass
from typing import List

import numpy as np


@dataclass
class AudioSegment:
    index: int
    start_ms: int
    end_ms: int
    waveform: np.ndarray


def chunk_audio(waveform: np.ndarray, sample_rate: int, chunk_duration_ms: int, hop_ms: int) -> List[AudioSegment]:
    chunk_samples = max(int((chunk_duration_ms / 1000.0) * sample_rate), 1)
    hop_samples = max(int((hop_ms / 1000.0) * sample_rate), 1)

    if waveform.size == 0:
        return []

    segments: List[AudioSegment] = []
    start = 0
    index = 0

    while start < waveform.shape[0]:
        end = min(start + chunk_samples, waveform.shape[0])
        segment = waveform[start:end]

        if segment.size == 0:
            break

        segments.append(
            AudioSegment(
                index=index,
                start_ms=int((start / sample_rate) * 1000),
                end_ms=int((end / sample_rate) * 1000),
                waveform=segment,
            )
        )

        if end == waveform.shape[0]:
            break

        start += hop_samples
        index += 1

    return segments
