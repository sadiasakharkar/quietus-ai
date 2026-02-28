from typing import Dict

import librosa
import numpy as np


def extract_mfcc_summary(waveform: np.ndarray, sample_rate: int) -> Dict[str, float]:
    if waveform.size == 0:
        return {
            "mfcc_mean": 0.0,
            "mfcc_std": 0.0,
            "rms_mean": 0.0,
            "zcr_mean": 0.0,
        }

    mfcc = librosa.feature.mfcc(y=waveform, sr=sample_rate, n_mfcc=13)
    rms = librosa.feature.rms(y=waveform)
    zcr = librosa.feature.zero_crossing_rate(y=waveform)

    return {
        "mfcc_mean": float(np.mean(mfcc)),
        "mfcc_std": float(np.std(mfcc)),
        "rms_mean": float(np.mean(rms)),
        "zcr_mean": float(np.mean(zcr)),
    }
