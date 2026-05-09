package com.aiquiz.service;

import com.aiquiz.dto.AttemptResultResponse;
import com.aiquiz.dto.SubmitAttemptRequest;
import com.aiquiz.entity.*;
import com.aiquiz.repository.QuizRepository;
import com.aiquiz.repository.QuestionRepository;
import com.aiquiz.repository.UserAttemptRepository;
import com.aiquiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptService {

    private final UserAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    /**
     * Submits and scores a user's quiz attempt.
     * Calculates per-question correctness, total score, and percentage.
     */
    @Transactional
    public AttemptResultResponse submitAttempt(SubmitAttemptRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + request.getQuizId()));

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quiz.getId());

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // Build attempt
        UserAttempt attempt = UserAttempt.builder()
                .user(user)
                .quiz(quiz)
                .status(UserAttempt.AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusSeconds(
                        request.getTimeTakenSeconds() != null ? request.getTimeTakenSeconds() : 0))
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .answers(new ArrayList<>())
                .build();

        int totalPoints = 0;
        int earnedPoints = 0;
        List<AttemptResultResponse.AnswerResultResponse> answerResults = new ArrayList<>();

        for (SubmitAttemptRequest.AnswerRequest answerReq : request.getAnswers()) {
            Question question = questionMap.get(answerReq.getQuestionId());
            if (question == null) continue;

            boolean isCorrect = evaluateAnswer(answerReq.getSelectedAnswer(), question);
            int pts = isCorrect ? question.getPoints() : 0;

            AttemptAnswer answerEntity = AttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedAnswer(answerReq.getSelectedAnswer())
                    .isCorrect(isCorrect)
                    .pointsEarned(pts)
                    .build();

            attempt.getAnswers().add(answerEntity);
            totalPoints += question.getPoints();
            earnedPoints += pts;

            answerResults.add(AttemptResultResponse.AnswerResultResponse.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .selectedAnswer(answerReq.getSelectedAnswer())
                    .correctAnswer(question.getCorrectAnswer())
                    .correct(isCorrect)
                    .pointsEarned(pts)
                    .totalPoints(question.getPoints())
                    .explanation(question.getExplanation())
                    .build());
        }

        double percentage = totalPoints > 0
                ? Math.round((earnedPoints * 100.0 / totalPoints) * 100.0) / 100.0
                : 0.0;

        attempt.setTotalPoints(totalPoints);
        attempt.setEarnedPoints(earnedPoints);
        attempt.setPercentage(percentage);
        attempt.setScore((double) earnedPoints);
        attempt.setStatus(UserAttempt.AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());

        UserAttempt saved = attemptRepository.save(attempt);
        log.info("Attempt saved: id={} user={} quiz={} score={}%",
                saved.getId(), userId, quiz.getId(), percentage);

        return AttemptResultResponse.builder()
                .attemptId(saved.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .userId(user.getId())
                .username(user.getUsername())
                .totalPoints(totalPoints)
                .earnedPoints(earnedPoints)
                .percentage(percentage)
                .status(saved.getStatus().name())
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .startedAt(saved.getStartedAt())
                .completedAt(saved.getCompletedAt())
                .answers(answerResults)
                .build();
    }

    @Transactional(readOnly = true)
    public AttemptResultResponse getAttemptResult(Long attemptId, Long userId) {
        UserAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Attempt not found or access denied"));

        List<AttemptResultResponse.AnswerResultResponse> answerResults =
                attempt.getAnswers().stream().map(a -> AttemptResultResponse.AnswerResultResponse.builder()
                        .questionId(a.getQuestion().getId())
                        .questionText(a.getQuestion().getQuestionText())
                        .selectedAnswer(a.getSelectedAnswer())
                        .correctAnswer(a.getQuestion().getCorrectAnswer())
                        .correct(Boolean.TRUE.equals(a.getIsCorrect()))
                        .pointsEarned(a.getPointsEarned())
                        .totalPoints(a.getQuestion().getPoints())
                        .explanation(a.getQuestion().getExplanation())
                        .build()
                ).collect(Collectors.toList());

        return AttemptResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .userId(attempt.getUser().getId())
                .username(attempt.getUser().getUsername())
                .totalPoints(attempt.getTotalPoints())
                .earnedPoints(attempt.getEarnedPoints())
                .percentage(attempt.getPercentage())
                .status(attempt.getStatus().name())
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .answers(answerResults)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<AttemptResultResponse> getUserAttempts(Long userId, Pageable pageable) {
        return attemptRepository.findByUserId(userId, pageable)
                .map(a -> AttemptResultResponse.builder()
                        .attemptId(a.getId())
                        .quizId(a.getQuiz().getId())
                        .quizTitle(a.getQuiz().getTitle())
                        .totalPoints(a.getTotalPoints())
                        .earnedPoints(a.getEarnedPoints())
                        .percentage(a.getPercentage())
                        .status(a.getStatus().name())
                        .completedAt(a.getCompletedAt())
                        .build());
    }

    /**
     * Evaluates whether a user's answer is correct.
     * Case-insensitive comparison for robustness.
     */
    private boolean evaluateAnswer(String selectedAnswer, Question question) {
        if (selectedAnswer == null || question.getCorrectAnswer() == null) return false;
        return selectedAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
    }
}
