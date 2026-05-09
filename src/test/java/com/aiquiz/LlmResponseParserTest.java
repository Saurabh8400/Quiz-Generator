package com.aiquiz;

import com.aiquiz.dto.LlmQuestionDto;
import com.aiquiz.util.LlmResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LlmResponseParserTest {

    private LlmResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new LlmResponseParser(new ObjectMapper());
    }

    @Test
    void parse_ShouldHandleCleanJson() {
        String json = """
                {
                  "questions": [
                    {
                      "questionText": "What is the capital of France?",
                      "type": "MCQ",
                      "options": [
                        {"label": "A", "text": "Berlin"},
                        {"label": "B", "text": "Paris"},
                        {"label": "C", "text": "Rome"},
                        {"label": "D", "text": "Madrid"}
                      ],
                      "correctAnswer": "B",
                      "explanation": "Paris is the capital of France.",
                      "points": 1
                    }
                  ]
                }
                """;

        List<LlmQuestionDto> questions = parser.parse(json);

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getQuestionText()).isEqualTo("What is the capital of France?");
        assertThat(questions.get(0).getCorrectAnswer()).isEqualTo("B");
        assertThat(questions.get(0).getOptions()).hasSize(4);
    }

    @Test
    void parse_ShouldStripMarkdownFences() {
        String json = """
                ```json
                {
                  "questions": [
                    {
                      "questionText": "Is Java object-oriented?",
                      "type": "TRUE_FALSE",
                      "options": [
                        {"label": "True", "text": "True"},
                        {"label": "False", "text": "False"}
                      ],
                      "correctAnswer": "True",
                      "explanation": "Yes, Java is OOP.",
                      "points": 1
                    }
                  ]
                }
                ```
                """;

        List<LlmQuestionDto> questions = parser.parse(json);
        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getType()).isEqualTo("TRUE_FALSE");
    }

    @Test
    void parse_ShouldThrowException_WhenQuestionsArrayMissing() {
        String invalidJson = """
                { "data": "no questions here" }
                """;

        assertThatThrownBy(() -> parser.parse(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("missing 'questions' array");
    }

    @Test
    void parse_ShouldThrowException_WhenNullInput() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void parse_ShouldSkipMalformedQuestions_AndReturnValidOnes() {
        String json = """
                {
                  "questions": [
                    {
                      "questionText": "Valid question?",
                      "type": "MCQ",
                      "options": [{"label": "A", "text": "Yes"}],
                      "correctAnswer": "A",
                      "explanation": "Because yes.",
                      "points": 1
                    },
                    {
                      "questionText": "",
                      "type": "MCQ"
                    }
                  ]
                }
                """;

        List<LlmQuestionDto> questions = parser.parse(json);
        // Empty question text should be skipped
        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getQuestionText()).isEqualTo("Valid question?");
    }
}
