package com.aiquiz.dto;

import com.aiquiz.entity.Quiz;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private String topic;
    private Quiz.Difficulty difficulty;
    private int numQuestions;
    private Integer timeLimitMinutes;
    private boolean isPublic;
    private String llmModel;
    private Long userId;
    private String username;
    private List<QuestionResponse> questions;
    private LocalDateTime createdAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String type;
        private Integer points;
        private String explanation;
        private Integer orderIndex;
        private List<OptionResponse> options;
        // correctAnswer intentionally excluded for security — only revealed in results
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class OptionResponse {
        private Long id;
        private String optionText;
        private String optionLabel;
    }
}
