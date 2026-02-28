package com.quietusai.domain.repository;

import com.quietusai.domain.entity.FusionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FusionResultRepository extends JpaRepository<FusionResult, UUID> {
    Optional<FusionResult> findBySessionIdAndChunkId(UUID sessionId, UUID chunkId);
}
