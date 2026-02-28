package com.quietusai.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audio_predictions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_audio_session_chunk", columnNames = {"session_id", "chunk_id"})
})
public class AudioPrediction {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "emotion", nullable = false, length = 32)
    private String emotion;

    @Column(name = "stress_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal stressScore;

    @Column(name = "confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public BigDecimal getStressScore() {
        return stressScore;
    }

    public void setStressScore(BigDecimal stressScore) {
        this.stressScore = stressScore;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
}
