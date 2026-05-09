package com.aiquiz.dto;

import lombok.*;

import java.util.List;

/**
 * Internal DTO representing a question parsed from the LLM (Language Model) JSON response.
 * Used during prompt engineering and response parsing pipelines.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LlmQuestionDto {

    private String questionText;
    private String type;               // MCQ, TRUE_FALSE, SHORT_ANSWER
    private List<LlmOptionDto> options;
    private String correctAnswer;
    private String explanation;
    private Integer points;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class LlmOptionDto {
        private String label;   // A, B, C, D
        private String text;
    }
}
