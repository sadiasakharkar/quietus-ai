package com.quietusai.domain.repository;

import com.quietusai.domain.entity.LlmExplanation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LlmExplanationRepository extends JpaRepository<LlmExplanation, UUID> {
    Optional<LlmExplanation> findBySessionIdAndChunkId(UUID sessionId, UUID chunkId);
}
