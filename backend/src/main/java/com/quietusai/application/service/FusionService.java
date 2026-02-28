package com.quietusai.application.service;

import com.quietusai.api.dto.*;
import com.quietusai.domain.entity.*;
import com.quietusai.domain.repository.*;
import com.quietusai.infrastructure.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class FusionService {

    private static final BigDecimal WEIGHT_VISION = BigDecimal.valueOf(0.40);
    private static final BigDecimal WEIGHT_AUDIO = BigDecimal.valueOf(0.30);
    private static final BigDecimal WEIGHT_TEXT = BigDecimal.valueOf(0.30);

    private final SessionRepository sessionRepository;
    private final VisionPredictionRepository visionPredictionRepository;
    private final AudioPredictionRepository audioPredictionRepository;
    private final TextPredictionRepository textPredictionRepository;
    private final FusionResultRepository fusionResultRepository;

    public FusionService(SessionRepository sessionRepository,
                         VisionPredictionRepository visionPredictionRepository,
                         AudioPredictionRepository audioPredictionRepository,
                         TextPredictionRepository textPredictionRepository,
                         FusionResultRepository fusionResultRepository) {
        this.sessionRepository = sessionRepository;
        this.visionPredictionRepository = visionPredictionRepository;
        this.audioPredictionRepository = audioPredictionRepository;
        this.textPredictionRepository = textPredictionRepository;
        this.fusionResultRepository = fusionResultRepository;
    }

    @Transactional
    public PredictionAckResponse upsertVision(UUID userId, UUID sessionId, UUID chunkId, UpsertVisionPredictionRequest request) {
        Session session = ownedSession(userId, sessionId);
        VisionPrediction prediction = visionPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseGet(VisionPrediction::new);

        prediction.setSession(session);
        prediction.setChunkId(chunkId);
        prediction.setEmotion(request.emotion().trim().toLowerCase());
        prediction.setConfidence(scale(request.confidence()));
        prediction.setDistressScore(scale(request.distressScore()));
        prediction.setFramesProcessed(request.framesProcessed());

        visionPredictionRepository.save(prediction);
        return new PredictionAckResponse(sessionId, chunkId, "VISION", "STORED");
    }

    @Transactional
    public PredictionAckResponse upsertAudio(UUID userId, UUID sessionId, UUID chunkId, UpsertAudioPredictionRequest request) {
        Session session = ownedSession(userId, sessionId);
        AudioPrediction prediction = audioPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseGet(AudioPrediction::new);

        prediction.setSession(session);
        prediction.setChunkId(chunkId);
        prediction.setEmotion(request.emotion().trim().toLowerCase());
        prediction.setStressScore(scale(request.stressScore()));
        prediction.setConfidence(scale(request.confidence()));

        audioPredictionRepository.save(prediction);
        return new PredictionAckResponse(sessionId, chunkId, "AUDIO", "STORED");
    }

    @Transactional
    public PredictionAckResponse upsertText(UUID userId, UUID sessionId, UUID chunkId, UpsertTextPredictionRequest request) {
        Session session = ownedSession(userId, sessionId);
        TextPrediction prediction = textPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseGet(TextPrediction::new);

        prediction.setSession(session);
        prediction.setChunkId(chunkId);
        prediction.setTranscript(request.transcript().trim());
        prediction.setRiskLevel(request.riskLevel().trim().toUpperCase());
        prediction.setDistressProbability(scale(request.distressProbability()));
        prediction.setConfidence(scale(request.confidence()));

        textPredictionRepository.save(prediction);
        return new PredictionAckResponse(sessionId, chunkId, "TEXT", "STORED");
    }

    @Transactional
    public FusionResultResponse fuse(UUID userId, UUID sessionId, UUID chunkId) {
        Session session = ownedSession(userId, sessionId);

        Optional<VisionPrediction> visionOpt = visionPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);
        Optional<AudioPrediction> audioOpt = audioPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);
        Optional<TextPrediction> textOpt = textPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);

        if (visionOpt.isEmpty() && audioOpt.isEmpty() && textOpt.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_MODALITY_DATA", "No modality predictions available for this chunk");
        }

        Map<String, BigDecimal> activeWeights = activeWeights(visionOpt.isPresent(), audioOpt.isPresent(), textOpt.isPresent());

        BigDecimal score = BigDecimal.ZERO;
        BigDecimal confidence = BigDecimal.ZERO;
        List<String> modalitiesUsed = new ArrayList<>();

        if (visionOpt.isPresent()) {
            VisionPrediction v = visionOpt.get();
            score = score.add(activeWeights.get("vision").multiply(v.getDistressScore()));
            confidence = confidence.add(activeWeights.get("vision").multiply(v.getConfidence()));
            modalitiesUsed.add("VISION");
        }
        if (audioOpt.isPresent()) {
            AudioPrediction a = audioOpt.get();
            score = score.add(activeWeights.get("audio").multiply(a.getStressScore()));
            confidence = confidence.add(activeWeights.get("audio").multiply(a.getConfidence()));
            modalitiesUsed.add("AUDIO");
        }
        if (textOpt.isPresent()) {
            TextPrediction t = textOpt.get();
            score = score.add(activeWeights.get("text").multiply(t.getDistressProbability()));
            confidence = confidence.add(activeWeights.get("text").multiply(t.getConfidence()));
            modalitiesUsed.add("TEXT");
        }

        score = scale(score);
        confidence = scale(confidence);

        String finalRisk = riskLevel(score);
        Map<String, Object> modalityJson = new LinkedHashMap<>();
        modalityJson.put("vision", modalitiesUsed.contains("VISION"));
        modalityJson.put("audio", modalitiesUsed.contains("AUDIO"));
        modalityJson.put("text", modalitiesUsed.contains("TEXT"));

        FusionResult fusionResult = fusionResultRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseGet(FusionResult::new);
        fusionResult.setSession(session);
        fusionResult.setChunkId(chunkId);
        fusionResult.setFinalRisk(finalRisk);
        fusionResult.setFinalScore(score);
        fusionResult.setConfidence(confidence);
        fusionResult.setModalitiesUsed(modalityJson);

        FusionResult saved = fusionResultRepository.save(fusionResult);

        return new FusionResultResponse(
                sessionId,
                chunkId,
                saved.getFinalRisk(),
                saved.getFinalScore(),
                saved.getConfidence(),
                modalitiesUsed,
                saved.getComputedAt()
        );
    }

    @Transactional(readOnly = true)
    public FusionResultResponse getFusion(UUID userId, UUID sessionId, UUID chunkId) {
        ownedSession(userId, sessionId);

        FusionResult fusion = fusionResultRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FUSION_NOT_FOUND", "Fusion result not found for this chunk"));

        List<String> modalities = new ArrayList<>();
        Object visionUsed = fusion.getModalitiesUsed().get("vision");
        Object audioUsed = fusion.getModalitiesUsed().get("audio");
        Object textUsed = fusion.getModalitiesUsed().get("text");
        if (Boolean.TRUE.equals(visionUsed)) {
            modalities.add("VISION");
        }
        if (Boolean.TRUE.equals(audioUsed)) {
            modalities.add("AUDIO");
        }
        if (Boolean.TRUE.equals(textUsed)) {
            modalities.add("TEXT");
        }

        return new FusionResultResponse(
                sessionId,
                chunkId,
                fusion.getFinalRisk(),
                fusion.getFinalScore(),
                fusion.getConfidence(),
                modalities,
                fusion.getComputedAt()
        );
    }

    private Session ownedSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SESSION_ACCESS_DENIED", "Session does not belong to user");
        }
        return session;
    }

    private Map<String, BigDecimal> activeWeights(boolean hasVision, boolean hasAudio, boolean hasText) {
        BigDecimal sum = BigDecimal.ZERO;
        if (hasVision) {
            sum = sum.add(WEIGHT_VISION);
        }
        if (hasAudio) {
            sum = sum.add(WEIGHT_AUDIO);
        }
        if (hasText) {
            sum = sum.add(WEIGHT_TEXT);
        }

        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put("vision", hasVision ? WEIGHT_VISION.divide(sum, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        weights.put("audio", hasAudio ? WEIGHT_AUDIO.divide(sum, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        weights.put("text", hasText ? WEIGHT_TEXT.divide(sum, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return weights;
    }

    private String riskLevel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(0.70)) >= 0) {
            return "HIGH";
        }
        if (score.compareTo(BigDecimal.valueOf(0.40)) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
