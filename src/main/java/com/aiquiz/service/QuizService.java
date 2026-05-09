package com.aiquiz.service;

import com.aiquiz.dto.LlmQuestionDto;
import com.aiquiz.dto.QuizGenerateRequest;
import com.aiquiz.dto.QuizResponse;
import com.aiquiz.entity.*;
import com.aiquiz.repository.QuizRepository;
import com.aiquiz.repository.UserRepository;
import com.aiquiz.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final LlmService llmService;
    private final PromptBuilder promptBuilder;

    /**
     * Orchestrates AI quiz generation:
     * 1. Build prompt
     * 2. Call LLM API
     * 3. Parse response
     * 4. Persist to database
     */
    @Transactional
    public QuizResponse generateQuiz(QuizGenerateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Generating quiz for user={} topic={} difficulty={}",
                userId, request.getTopic(), request.getDifficulty());

        // Call LLM API
        List<LlmQuestionDto> llmQuestions = llmService.generateQuestions(request);

        if (llmQuestions.isEmpty()) {
            throw new RuntimeException("LLM returned no questions. Please try again.");
        }

        // Build quiz entity
        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : "Quiz: " + request.getTopic();

        String promptUsed = promptBuilder.buildUserPrompt(request);

        Quiz quiz = Quiz.builder()
                .title(title)
                .description(request.getDescription())
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .numQuestions(llmQuestions.size())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .isPublic(request.isPublic())
                .llmModel(llmService.getModel())
                .promptUsed(promptUsed)
                .user(user)
                .questions(new ArrayList<>())
                .build();

        // Map LLM questions to entities
        List<Question> questions = mapLlmQuestionsToEntities(llmQuestions, quiz);
        quiz.setQuestions(questions);

        Quiz savedQuiz = quizRepository.save(quiz);
        log.info("Quiz saved: id={} questions={}", savedQuiz.getId(), savedQuiz.getQuestions().size());

        return mapToQuizResponse(savedQuiz, true);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));

        // Allow access if owner or public
        if (!quiz.getUser().getId().equals(userId) && !quiz.getIsPublic()) {
            throw new RuntimeException("Access denied to this quiz");
        }

        return mapToQuizResponse(quiz, false); // hide correct answers for non-owners taking quiz
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getUserQuizzes(Long userId, Pageable pageable) {
        return quizRepository.findByUserId(userId, pageable)
                .map(q -> mapToQuizResponse(q, false));
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getPublicQuizzes(Pageable pageable) {
        return quizRepository.findByIsPublicTrue(pageable)
                .map(q -> mapToQuizResponse(q, false));
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> searchPublicQuizzes(String keyword, Pageable pageable) {
        return quizRepository.searchPublicQuizzes(keyword, pageable)
                .map(q -> mapToQuizResponse(q, false));
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (!quiz.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own quizzes");
        }
        quizRepository.delete(quiz);
        log.info("Quiz deleted: id={}", quizId);
    }

    // ---- Mapping Helpers ----

    private List<Question> mapLlmQuestionsToEntities(List<LlmQuestionDto> llmQuestions, Quiz quiz) {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < llmQuestions.size(); i++) {
            LlmQuestionDto llmQ = llmQuestions.get(i);

            Question.QuestionType type = parseQuestionType(llmQ.getType());

            Question question = Question.builder()
                    .questionText(llmQ.getQuestionText())
                    .type(type)
                    .correctAnswer(llmQ.getCorrectAnswer())
                    .explanation(llmQ.getExplanation())
                    .points(llmQ.getPoints() != null ? llmQ.getPoints() : 1)
                    .orderIndex(i + 1)
                    .quiz(quiz)
                    .options(new ArrayList<>())
                    .build();

            List<Option> options = mapOptions(llmQ.getOptions(), question);
            question.setOptions(options);
            questions.add(question);
        }

        return questions;
    }

    private List<Option> mapOptions(List<LlmQuestionDto.LlmOptionDto> llmOptions, Question question) {
        if (llmOptions == null) return new ArrayList<>();
        return llmOptions.stream().map(o -> Option.builder()
                .optionText(o.getText())
                .optionLabel(o.getLabel())
                .isCorrect(o.getLabel().equalsIgnoreCase(question.getCorrectAnswer()))
                .question(question)
                .build()
        ).collect(Collectors.toList());
    }

    private Question.QuestionType parseQuestionType(String type) {
        if (type == null) return Question.QuestionType.MCQ;
        return switch (type.toUpperCase()) {
            case "TRUE_FALSE", "TRUE/FALSE" -> Question.QuestionType.TRUE_FALSE;
            case "SHORT_ANSWER" -> Question.QuestionType.SHORT_ANSWER;
            default -> Question.QuestionType.MCQ;
        };
    }

    public QuizResponse mapToQuizResponse(Quiz quiz, boolean includeCorrectAnswers) {
        List<QuizResponse.QuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> {
                    List<QuizResponse.OptionResponse> optionResponses = q.getOptions().stream()
                            .map(o -> QuizResponse.OptionResponse.builder()
                                    .id(o.getId())
                                    .optionText(o.getOptionText())
                                    .optionLabel(o.getOptionLabel())
                                    .build())
                            .collect(Collectors.toList());

                    return QuizResponse.QuestionResponse.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .type(q.getType().name())
                            .points(q.getPoints())
                            .explanation(includeCorrectAnswers ? q.getExplanation() : null)
                            .orderIndex(q.getOrderIndex())
                            .options(optionResponses)
                            .build();
                })
                .collect(Collectors.toList());

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .topic(quiz.getTopic())
                .difficulty(quiz.getDifficulty())
                .numQuestions(quiz.getQuestions().size())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .isPublic(Boolean.TRUE.equals(quiz.getIsPublic()))
                .llmModel(quiz.getLlmModel())
                .userId(quiz.getUser().getId())
                .username(quiz.getUser().getUsername())
                .questions(questionResponses)
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}
