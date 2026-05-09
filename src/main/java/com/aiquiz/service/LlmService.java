package com.aiquiz.service;

import com.aiquiz.dto.LlmQuestionDto;
import com.aiquiz.dto.QuizGenerateRequest;
import com.aiquiz.util.LlmResponseParser;
import com.aiquiz.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Handles all communication with the LLM (Language Model) API.
 * Orchestrates prompt construction, API calls, and response parsing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final WebClient llmWebClient;
    private final PromptBuilder promptBuilder;
    private final LlmResponseParser responseParser;

    @Value("${llm.api.model}")
    private String model;

    @Value("${llm.api.max-tokens}")
    private int maxTokens;

    @Value("${llm.api.temperature}")
    private double temperature;

    @Value("${llm.api.timeout-seconds:60}")
    private int timeoutSeconds;

    /**
     * Generates quiz questions by calling the LLM API.
     * Returns a list of structured LlmQuestionDto objects.
     */
    public List<LlmQuestionDto> generateQuestions(QuizGenerateRequest request) {
        log.info("Calling LLM API to generate {} questions on topic: {}",
                request.getNumQuestions(), request.getTopic());

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(request);

        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt);

        try {
            String rawResponse = llmWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .map(this::extractContent)
                    .block();

            log.debug("LLM raw response received, parsing...");
            List<LlmQuestionDto> questions = responseParser.parse(rawResponse);
            log.info("Successfully parsed {} questions from LLM response", questions.size());
            return questions;

        } catch (Exception e) {
            log.error("LLM API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate quiz questions from AI: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected LLM response structure: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt) {
        return Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
    }

    public String getModel() {
        return model;
    }
}
