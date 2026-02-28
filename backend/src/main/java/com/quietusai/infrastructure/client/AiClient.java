package com.quietusai.infrastructure.client;

import com.quietusai.config.AppProperties;
import com.quietusai.infrastructure.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AiClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public AiClient(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = RestClient.builder().baseUrl(appProperties.ai().baseUrl()).build();
    }

    public AiPredictResponse predict(String text, String requestId) {
        try {
            AiPredictResponse response = restClient.post()
                    .uri("/v1/inference/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Service-Token", appProperties.ai().serviceToken())
                    .body(new AiPredictRequest(text, requestId))
                    .retrieve()
                    .body(AiPredictResponse.class);

            if (response == null || response.label() == null || response.confidence() == null || response.modelVersion() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI_SERVICE_INVALID_RESPONSE", "AI service returned an invalid response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI_SERVICE_UNAVAILABLE", "AI service unavailable");
        }
    }
}
