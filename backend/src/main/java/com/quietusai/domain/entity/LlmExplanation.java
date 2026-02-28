package com.quietusai.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "llm_explanations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_llm_session_chunk", columnNames = {"session_id", "chunk_id"})
})
public class LlmExplanation {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_signals", nullable = false, columnDefinition = "jsonb")
    private List<String> keySignals;

    @Column(name = "recommended_action_level", nullable = false, length = 32)
    private String recommendedActionLevel;

    @Column(name = "explanation_confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal explanationConfidence;

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

    @PreUpdate
    public void preUpdate() {
        createdAt = OffsetDateTime.now();
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeySignals() {
        return keySignals;
    }

    public void setKeySignals(List<String> keySignals) {
        this.keySignals = keySignals;
    }

    public String getRecommendedActionLevel() {
        return recommendedActionLevel;
    }

    public void setRecommendedActionLevel(String recommendedActionLevel) {
        this.recommendedActionLevel = recommendedActionLevel;
    }

    public BigDecimal getExplanationConfidence() {
        return explanationConfidence;
    }

    public void setExplanationConfidence(BigDecimal explanationConfidence) {
        this.explanationConfidence = explanationConfidence;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
