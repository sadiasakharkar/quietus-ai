package com.quietusai.domain.repository;

import com.quietusai.domain.entity.TextPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TextPredictionRepository extends JpaRepository<TextPrediction, UUID> {
    Optional<TextPrediction> findBySessionIdAndChunkId(UUID sessionId, UUID chunkId);
}
