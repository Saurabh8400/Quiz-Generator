package com.aiquiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SubmitAttemptRequest {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    private List<AnswerRequest> answers;

    private Integer timeTakenSeconds;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        @NotNull
        private Long questionId;
        private String selectedAnswer;
    }
}
