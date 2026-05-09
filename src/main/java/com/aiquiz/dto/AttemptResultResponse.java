package com.aiquiz.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttemptResultResponse {

    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Long userId;
    private String username;

    private Integer totalPoints;
    private Integer earnedPoints;
    private Double percentage;
    private String status;
    private Integer timeTakenSeconds;

    private List<AnswerResultResponse> answers;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class AnswerResultResponse {
        private Long questionId;
        private String questionText;
        private String selectedAnswer;
        private String correctAnswer;
        private boolean correct;
        private Integer pointsEarned;
        private Integer totalPoints;
        private String explanation;
    }
}
