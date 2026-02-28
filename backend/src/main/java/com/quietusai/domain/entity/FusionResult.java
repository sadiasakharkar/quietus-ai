package com.quietusai.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "fusion_results", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fusion_session_chunk", columnNames = {"session_id", "chunk_id"})
})
public class FusionResult {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "final_risk", nullable = false, length = 16)
    private String finalRisk;

    @Column(name = "final_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal finalScore;

    @Column(name = "confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "modalities_used", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> modalitiesUsed;

    @Column(name = "computed_at", nullable = false)
    private OffsetDateTime computedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (computedAt == null) {
            computedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        computedAt = OffsetDateTime.now();
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

    public String getFinalRisk() {
        return finalRisk;
    }

    public void setFinalRisk(String finalRisk) {
        this.finalRisk = finalRisk;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public Map<String, Object> getModalitiesUsed() {
        return modalitiesUsed;
    }

    public void setModalitiesUsed(Map<String, Object> modalitiesUsed) {
        this.modalitiesUsed = modalitiesUsed;
    }

    public OffsetDateTime getComputedAt() {
        return computedAt;
    }
}
