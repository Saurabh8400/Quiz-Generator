package com.aiquiz.util;

import com.aiquiz.dto.LlmQuestionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses raw LLM JSON responses into structured LlmQuestionDto objects.
 * Implements defensive parsing to handle minor inconsistencies in LLM output,
 * improving data reliability and reducing AI response errors.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LlmResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Main entry point. Strips any markdown fences and parses JSON.
     */
    public List<LlmQuestionDto> parse(String rawResponse) {
        String cleanJson = sanitizeResponse(rawResponse);
        try {
            JsonNode root = objectMapper.readTree(cleanJson);
            JsonNode questionsNode = root.path("questions");

            if (questionsNode.isMissingNode() || !questionsNode.isArray()) {
                log.error("LLM response missing 'questions' array. Raw: {}", rawResponse);
                throw new RuntimeException("Invalid LLM response format: missing 'questions' array");
            }

            return parseQuestions(questionsNode);

        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse LLM quiz response: " + e.getMessage(), e);
        }
    }

    /**
     * Strips common LLM artifacts: markdown fences, leading/trailing whitespace.
     */
    private String sanitizeResponse(String raw) {
        if (raw == null) throw new RuntimeException("LLM returned null response");

        String cleaned = raw.trim();

        // Strip markdown code fences if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private List<LlmQuestionDto> parseQuestions(JsonNode questionsNode) {
        List<LlmQuestionDto> questions = new ArrayList<>();

        for (JsonNode qNode : questionsNode) {
            try {
                LlmQuestionDto question = LlmQuestionDto.builder()
                        .questionText(getTextSafely(qNode, "questionText"))
                        .type(getTextSafely(qNode, "type", "MCQ"))
                        .correctAnswer(getTextSafely(qNode, "correctAnswer"))
                        .explanation(getTextSafely(qNode, "explanation", ""))
                        .points(getIntSafely(qNode, "points", 1))
                        .options(parseOptions(qNode.path("options")))
                        .build();

                if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
                    log.warn("Skipping question with empty questionText");
                    continue;
                }

                questions.add(question);
            } catch (Exception e) {
                log.warn("Skipping malformed question node: {}", e.getMessage());
            }
        }

        return questions;
    }

    private List<LlmQuestionDto.LlmOptionDto> parseOptions(JsonNode optionsNode) {
        List<LlmQuestionDto.LlmOptionDto> options = new ArrayList<>();

        if (optionsNode.isArray()) {
            for (JsonNode optNode : optionsNode) {
                options.add(LlmQuestionDto.LlmOptionDto.builder()
                        .label(getTextSafely(optNode, "label", ""))
                        .text(getTextSafely(optNode, "text", ""))
                        .build());
            }
        }

        return options;
    }

    private String getTextSafely(JsonNode node, String field) {
        return getTextSafely(node, field, null);
    }

    private String getTextSafely(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asText();
    }

    private int getIntSafely(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asInt(defaultValue);
    }
}
