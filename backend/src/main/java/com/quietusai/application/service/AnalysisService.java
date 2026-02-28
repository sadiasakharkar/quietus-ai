package com.quietusai.application.service;

import com.quietusai.api.dto.SubmitAnalysisRequest;
import com.quietusai.api.dto.SubmitAnalysisResponse;
import com.quietusai.domain.entity.AnalysisRecord;
import com.quietusai.domain.entity.User;
import com.quietusai.domain.repository.AnalysisRecordRepository;
import com.quietusai.domain.repository.UserRepository;
import com.quietusai.infrastructure.client.AiClient;
import com.quietusai.infrastructure.client.AiPredictResponse;
import com.quietusai.infrastructure.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AnalysisService {

    private final AnalysisRecordRepository analysisRecordRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;

    public AnalysisService(AnalysisRecordRepository analysisRecordRepository,
                           UserRepository userRepository,
                           AiClient aiClient) {
        this.analysisRecordRepository = analysisRecordRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
    }

    @Transactional
    public SubmitAnalysisResponse analyze(UUID userId, SubmitAnalysisRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));

        UUID requestId = UUID.randomUUID();
        OffsetDateTime submittedAt = OffsetDateTime.now();
        AiPredictResponse aiResponse = aiClient.predict(request.text(), requestId.toString());
        OffsetDateTime analyzedAt = OffsetDateTime.now();

        AnalysisRecord record = new AnalysisRecord();
        record.setUser(user);
        record.setInputText(request.text());
        record.setSource(request.source());
        record.setPredictedLabel(aiResponse.label());
        record.setConfidence(aiResponse.confidence());
        record.setModelVersion(aiResponse.modelVersion());
        record.setAiProcessingMs(aiResponse.processingMs() == null ? 0 : aiResponse.processingMs());
        record.setSubmittedAt(submittedAt);
        record.setAnalyzedAt(analyzedAt);
        record.setRequestId(requestId);

        AnalysisRecord saved = analysisRecordRepository.save(record);

        return new SubmitAnalysisResponse(
                saved.getId(),
                saved.getPredictedLabel(),
                saved.getConfidence(),
                saved.getModelVersion(),
                saved.getAnalyzedAt()
        );
    }
}
