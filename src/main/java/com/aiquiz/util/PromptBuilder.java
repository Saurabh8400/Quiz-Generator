package com.aiquiz.util;

import com.aiquiz.dto.QuizGenerateRequest;
import com.aiquiz.entity.Quiz;
import org.springframework.stereotype.Component;

/**
 * Handles prompt engineering for the LLM API.
 * Constructs structured prompts and system instructions
 * to ensure consistent, parseable JSON responses.
 */
@Component
public class PromptBuilder {

    /**
     * Builds the system prompt that instructs the LLM to return
     * strictly structured JSON — core to the response parsing pipeline.
     */
    public String buildSystemPrompt() {
        return """
                You are an expert quiz generator. Your task is to generate quiz questions on the given topic.
                
                CRITICAL RULES:
                1. Always respond with ONLY valid JSON — no markdown, no explanation, no preamble.
                2. Follow the exact schema provided.
                3. Ensure questions are factually accurate, clear, and appropriate for the difficulty level.
                4. For MCQ questions, always provide exactly 4 options labeled A, B, C, D.
                5. For TRUE_FALSE questions, provide exactly 2 options: "True" and "False".
                6. The correctAnswer field must exactly match one of the option labels (A, B, C, D) or "True"/"False".
                7. Include a concise explanation for each correct answer.
                """;
    }

    /**
     * Builds the user prompt based on quiz generation request.
     */
    public String buildUserPrompt(QuizGenerateRequest request) {
        String difficultyDesc = getDifficultyDescription(request.getDifficulty());

        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format(
                "Generate %d quiz questions about: \"%s\"\n\n",
                request.getNumQuestions(), request.getTopic()));

        prompt.append(String.format("Difficulty: %s (%s)\n\n", request.getDifficulty(), difficultyDesc));

        if (request.getAdditionalInstructions() != null && !request.getAdditionalInstructions().isBlank()) {
            prompt.append("Additional instructions: ").append(request.getAdditionalInstructions()).append("\n\n");
        }

        prompt.append("Return ONLY this JSON structure:\n");
        prompt.append(getJsonSchema(request.getNumQuestions()));

        return prompt.toString();
    }

    private String getDifficultyDescription(Quiz.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "basic concepts, simple recall, suitable for beginners";
            case MEDIUM -> "intermediate understanding, some analysis required";
            case HARD -> "advanced knowledge, critical thinking required";
            case EXPERT -> "expert-level, deep technical knowledge, edge cases";
        };
    }

    private String getJsonSchema(int numQuestions) {
        return """
                {
                  "questions": [
                    {
                      "questionText": "Your question here?",
                      "type": "MCQ",
                      "options": [
                        {"label": "A", "text": "Option A text"},
                        {"label": "B", "text": "Option B text"},
                        {"label": "C", "text": "Option C text"},
                        {"label": "D", "text": "Option D text"}
                      ],
                      "correctAnswer": "A",
                      "explanation": "Brief explanation of why A is correct.",
                      "points": 1
                    }
                  ]
                }
                
                Generate exactly """ + numQuestions + " questions. Mix MCQ and TRUE_FALSE types.";
    }
}
