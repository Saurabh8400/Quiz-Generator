package com.aiquiz.dto;

import com.aiquiz.entity.Quiz;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuizGenerateRequest {

    @NotBlank(message = "Topic is required")
    @Size(max = 100, message = "Topic must be under 100 characters")
    private String topic;

    @Min(value = 1, message = "Minimum 1 question")
    @Max(value = 50, message = "Maximum 50 questions")
    @Builder.Default
    private int numQuestions = 10;

    @Builder.Default
    private Quiz.Difficulty difficulty = Quiz.Difficulty.MEDIUM;

    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;

    private Integer timeLimitMinutes;

    @Builder.Default
    private boolean isPublic = false;

    @Size(max = 500)
    private String additionalInstructions;
}
