package com.quietusai.domain.repository;

import com.quietusai.domain.entity.AudioPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AudioPredictionRepository extends JpaRepository<AudioPrediction, UUID> {
    Optional<AudioPrediction> findBySessionIdAndChunkId(UUID sessionId, UUID chunkId);
}
