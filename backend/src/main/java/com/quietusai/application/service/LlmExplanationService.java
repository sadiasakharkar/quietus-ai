package com.quietusai.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quietusai.api.dto.LlmExplanationResponse;
import com.quietusai.config.AppProperties;
import com.quietusai.domain.entity.*;
import com.quietusai.domain.repository.*;
import com.quietusai.infrastructure.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class LlmExplanationService {

    private final SessionRepository sessionRepository;
    private final VisionPredictionRepository visionPredictionRepository;
    private final AudioPredictionRepository audioPredictionRepository;
    private final TextPredictionRepository textPredictionRepository;
    private final FusionResultRepository fusionResultRepository;
    private final LlmExplanationRepository llmExplanationRepository;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public LlmExplanationService(SessionRepository sessionRepository,
                                 VisionPredictionRepository visionPredictionRepository,
                                 AudioPredictionRepository audioPredictionRepository,
                                 TextPredictionRepository textPredictionRepository,
                                 FusionResultRepository fusionResultRepository,
                                 LlmExplanationRepository llmExplanationRepository,
                                 AppProperties appProperties,
                                 ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.visionPredictionRepository = visionPredictionRepository;
        this.audioPredictionRepository = audioPredictionRepository;
        this.textPredictionRepository = textPredictionRepository;
        this.fusionResultRepository = fusionResultRepository;
        this.llmExplanationRepository = llmExplanationRepository;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Transactional
    public LlmExplanationResponse generate(UUID userId, UUID sessionId, UUID chunkId) {
        Session session = ownedSession(userId, sessionId);

        FusionResult fusion = fusionResultRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "FUSION_REQUIRED", "Fusion result required before explanation"));

        Optional<VisionPrediction> vision = visionPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);
        Optional<AudioPrediction> audio = audioPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);
        Optional<TextPrediction> text = textPredictionRepository.findBySessionIdAndChunkId(sessionId, chunkId);

        String prompt = buildPrompt(sessionId, chunkId, fusion, vision.orElse(null), audio.orElse(null), text.orElse(null));
        ExplanationPayload payload = callProvider(prompt);

        LlmExplanation explanation = llmExplanationRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseGet(LlmExplanation::new);
        explanation.setSession(session);
        explanation.setChunkId(chunkId);
        explanation.setSummary(payload.summary());
        explanation.setKeySignals(payload.keySignals());
        explanation.setRecommendedActionLevel(payload.recommendedActionLevel());
        explanation.setExplanationConfidence(scale(payload.explanationConfidence()));

        LlmExplanation saved = llmExplanationRepository.save(explanation);
        return toResponse(saved, sessionId, chunkId);
    }

    @Transactional(readOnly = true)
    public LlmExplanationResponse get(UUID userId, UUID sessionId, UUID chunkId) {
        ownedSession(userId, sessionId);
        LlmExplanation explanation = llmExplanationRepository.findBySessionIdAndChunkId(sessionId, chunkId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EXPLANATION_NOT_FOUND", "Explanation not found for chunk"));

        return toResponse(explanation, sessionId, chunkId);
    }

    private LlmExplanationResponse toResponse(LlmExplanation explanation, UUID sessionId, UUID chunkId) {
        return new LlmExplanationResponse(
                sessionId,
                chunkId,
                explanation.getSummary(),
                explanation.getKeySignals(),
                explanation.getRecommendedActionLevel(),
                explanation.getExplanationConfidence(),
                explanation.getCreatedAt()
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

    private ExplanationPayload callProvider(String prompt) {
        String provider = Optional.ofNullable(appProperties.llm().provider()).orElse("openai").trim().toLowerCase();
        return switch (provider) {
            case "openai" -> callOpenAi(prompt);
            case "gemini" -> callGemini(prompt);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_LLM_PROVIDER", "Unsupported LLM provider: " + provider);
        };
    }

    private ExplanationPayload callOpenAi(String prompt) {
        String apiKey = required(appProperties.llm().openaiApiKey(), "OPENAI_API_KEY");

        Map<String, Object> body = new HashMap<>();
        body.put("model", appProperties.llm().openaiModel());
        body.put("temperature", 0.2);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemInstruction()),
                Map.of("role", "user", "content", prompt)
        ));

        try {
            JsonNode node = restClient.post()
                    .uri(appProperties.llm().openaiBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String content = node.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_EMPTY_RESPONSE", "OpenAI returned empty content");
            }
            return parseExplanation(content);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_CALL_FAILED", "OpenAI call failed");
        }
    }

    private ExplanationPayload callGemini(String prompt) {
        String apiKey = required(appProperties.llm().geminiApiKey(), "GEMINI_API_KEY");

        Map<String, Object> body = new HashMap<>();
        body.put("system_instruction", Map.of(
                "parts", List.of(Map.of("text", systemInstruction()))
        ));
        body.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
        )));
        body.put("generationConfig", Map.of(
                "temperature", 0.2,
                "responseMimeType", "application/json"
        ));

        String url = appProperties.llm().geminiBaseUrl() + "/models/" + appProperties.llm().geminiModel() + ":generateContent?key=" + apiKey;

        try {
            JsonNode node = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String content = node.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(null);
            if (content == null || content.isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_EMPTY_RESPONSE", "Gemini returned empty content");
            }
            return parseExplanation(content);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_CALL_FAILED", "Gemini call failed");
        }
    }

    private ExplanationPayload parseExplanation(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String summary = node.path("summary").asText("").trim();
            String action = node.path("recommendedActionLevel").asText("").trim();
            double confidenceDouble = node.path("explanationConfidence").asDouble(-1.0);

            List<String> signals = new ArrayList<>();
            JsonNode keySignalsNode = node.path("keySignals");
            if (keySignalsNode.isArray()) {
                for (JsonNode signal : keySignalsNode) {
                    String value = signal.asText("").trim();
                    if (!value.isEmpty()) {
                        signals.add(value);
                    }
                }
            }

            if (summary.isEmpty() || action.isEmpty() || confidenceDouble < 0.0 || confidenceDouble > 1.0 || signals.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_INVALID_JSON", "LLM response missing required explanation fields");
            }

            return new ExplanationPayload(summary, signals, action.toUpperCase(), BigDecimal.valueOf(confidenceDouble));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM_PARSE_FAILED", "Failed to parse LLM response as JSON");
        }
    }

    private String buildPrompt(UUID sessionId,
                               UUID chunkId,
                               FusionResult fusion,
                               VisionPrediction vision,
                               AudioPrediction audio,
                               TextPrediction text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId.toString());
        payload.put("chunkId", chunkId.toString());

        Map<String, Object> fusionNode = new LinkedHashMap<>();
        fusionNode.put("finalRisk", fusion.getFinalRisk());
        fusionNode.put("finalScore", fusion.getFinalScore());
        fusionNode.put("confidence", fusion.getConfidence());
        payload.put("fusion", fusionNode);

        if (vision != null) {
            payload.put("vision", Map.of(
                    "emotion", vision.getEmotion(),
                    "confidence", vision.getConfidence(),
                    "distressScore", vision.getDistressScore(),
                    "framesProcessed", vision.getFramesProcessed()
            ));
        }

        if (audio != null) {
            payload.put("audio", Map.of(
                    "emotion", audio.getEmotion(),
                    "confidence", audio.getConfidence(),
                    "stressScore", audio.getStressScore()
            ));
        }

        if (text != null) {
            payload.put("text", Map.of(
                    "riskLevel", text.getRiskLevel(),
                    "confidence", text.getConfidence(),
                    "distressProbability", text.getDistressProbability(),
                    "transcript", text.getTranscript()
            ));
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PROMPT_BUILD_FAILED", "Failed to build LLM prompt payload");
        }
    }

    private String systemInstruction() {
        return "You are a risk explanation engine for multimodal distress detection. " +
                "Use provided signals only. Respond strictly JSON with keys: " +
                "summary (string), keySignals (string array), recommendedActionLevel (string), explanationConfidence (0-1 number).";
    }

    private String required(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LLM_KEY_MISSING", key + " is not configured");
        }
        return value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private record ExplanationPayload(
            String summary,
            List<String> keySignals,
            String recommendedActionLevel,
            BigDecimal explanationConfidence
    ) {
    }
}
