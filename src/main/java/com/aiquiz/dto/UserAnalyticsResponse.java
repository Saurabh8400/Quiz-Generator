package com.aiquiz.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAnalyticsResponse {

    private Long userId;
    private String username;

    private long totalQuizzesCreated;
    private long totalAttemptsCompleted;
    private Double averageScore;
    private Double highestScore;
    private List<String> topicsExplored;
    private List<RecentAttemptSummary> recentAttempts;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class RecentAttemptSummary {
        private Long attemptId;
        private Long quizId;
        private String quizTitle;
        private String topic;
        private Double percentage;
        private String status;
        private String completedAt;
    }
}
