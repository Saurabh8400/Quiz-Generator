package com.aiquiz.service;

import com.aiquiz.dto.UserAnalyticsResponse;
import com.aiquiz.entity.User;
import com.aiquiz.entity.UserAttempt;
import com.aiquiz.repository.QuizRepository;
import com.aiquiz.repository.UserAttemptRepository;
import com.aiquiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserAttemptRepository attemptRepository;

    @Transactional(readOnly = true)
    public UserAnalyticsResponse getUserAnalytics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalQuizzes = quizRepository.countByUserId(userId);
        long totalAttempts = attemptRepository.countCompletedByUserId(userId);
        Double avgScore = attemptRepository.getAverageScoreByUserId(userId);
        Double highestScore = attemptRepository.getHighestScoreByUserId(userId);
        List<String> topics = quizRepository.findDistinctTopicsByUserId(userId);

        List<UserAttempt> recentAttempts = attemptRepository
                .findRecentCompletedByUserId(userId, PageRequest.of(0, 5));

        List<UserAnalyticsResponse.RecentAttemptSummary> recentSummaries = recentAttempts.stream()
                .map(a -> UserAnalyticsResponse.RecentAttemptSummary.builder()
                        .attemptId(a.getId())
                        .quizId(a.getQuiz().getId())
                        .quizTitle(a.getQuiz().getTitle())
                        .topic(a.getQuiz().getTopic())
                        .percentage(a.getPercentage())
                        .status(a.getStatus().name())
                        .completedAt(a.getCompletedAt() != null ? a.getCompletedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return UserAnalyticsResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .totalQuizzesCreated(totalQuizzes)
                .totalAttemptsCompleted(totalAttempts)
                .averageScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0)
                .highestScore(highestScore != null ? Math.round(highestScore * 100.0) / 100.0 : 0.0)
                .topicsExplored(topics)
                .recentAttempts(recentSummaries)
                .build();
    }
}
