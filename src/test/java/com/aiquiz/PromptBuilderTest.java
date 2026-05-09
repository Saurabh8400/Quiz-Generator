package com.aiquiz;

import com.aiquiz.dto.QuizGenerateRequest;
import com.aiquiz.entity.Quiz;
import com.aiquiz.util.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void buildSystemPrompt_ShouldContainJsonInstruction() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertThat(prompt).contains("JSON");
        assertThat(prompt).contains("quiz");
    }

    @Test
    void buildUserPrompt_ShouldContainTopicAndCount() {
        QuizGenerateRequest request = QuizGenerateRequest.builder()
                .topic("Java Spring Boot")
                .numQuestions(5)
                .difficulty(Quiz.Difficulty.MEDIUM)
                .build();

        String prompt = promptBuilder.buildUserPrompt(request);

        assertThat(prompt).contains("Java Spring Boot");
        assertThat(prompt).contains("5");
        assertThat(prompt).contains("MEDIUM");
    }

    @Test
    void buildUserPrompt_ShouldIncludeAdditionalInstructions() {
        QuizGenerateRequest request = QuizGenerateRequest.builder()
                .topic("Python")
                .numQuestions(3)
                .difficulty(Quiz.Difficulty.EASY)
                .additionalInstructions("Focus on list comprehensions")
                .build();

        String prompt = promptBuilder.buildUserPrompt(request);

        assertThat(prompt).contains("Focus on list comprehensions");
    }

    @Test
    void buildUserPrompt_ShouldContainJsonSchema() {
        QuizGenerateRequest request = QuizGenerateRequest.builder()
                .topic("SQL")
                .numQuestions(10)
                .difficulty(Quiz.Difficulty.HARD)
                .build();

        String prompt = promptBuilder.buildUserPrompt(request);

        assertThat(prompt).contains("questions");
        assertThat(prompt).contains("correctAnswer");
        assertThat(prompt).contains("explanation");
    }
}
