package com.quietusai.domain.repository;

import com.quietusai.domain.entity.VisionPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VisionPredictionRepository extends JpaRepository<VisionPrediction, UUID> {
    Optional<VisionPrediction> findBySessionIdAndChunkId(UUID sessionId, UUID chunkId);
}
