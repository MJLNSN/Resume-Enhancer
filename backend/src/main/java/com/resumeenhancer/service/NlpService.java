package com.resumeenhancer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class NlpService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NlpService(@Value("${app.nlp.service.base-url}") String nlpBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(nlpBaseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Object parseResume(String resumeText) {
        try {
            Map<String, String> request = Map.of("text", resumeText);
            
            String response = webClient.post()
                    .uri("/parse")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return objectMapper.readValue(response, Object.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse resume with NLP service: " + e.getMessage(), e);
        }
    }

    public boolean isServiceHealthy() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            return response != null && response.contains("healthy");
        } catch (Exception e) {
            return false;
        }
    }
}
