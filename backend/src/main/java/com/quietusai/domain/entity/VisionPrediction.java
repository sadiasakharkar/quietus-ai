package com.quietusai.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vision_predictions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vision_session_chunk", columnNames = {"session_id", "chunk_id"})
})
public class VisionPrediction {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "emotion", nullable = false, length = 32)
    private String emotion;

    @Column(name = "confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "distress_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal distressScore;

    @Column(name = "frames_processed", nullable = false)
    private Integer framesProcessed;

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

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public BigDecimal getDistressScore() {
        return distressScore;
    }

    public void setDistressScore(BigDecimal distressScore) {
        this.distressScore = distressScore;
    }

    public Integer getFramesProcessed() {
        return framesProcessed;
    }

    public void setFramesProcessed(Integer framesProcessed) {
        this.framesProcessed = framesProcessed;
    }
}
