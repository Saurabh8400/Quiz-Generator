package com.aiquiz.controller;

import com.aiquiz.dto.ApiResponse;
import com.aiquiz.dto.QuizGenerateRequest;
import com.aiquiz.dto.QuizResponse;
import com.aiquiz.security.CustomUserDetails;
import com.aiquiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * POST /api/quizzes/generate
     * Generates an AI-powered quiz using the LLM API.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QuizResponse>> generateQuiz(
            @Valid @RequestBody QuizGenerateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        QuizResponse quiz = quizService.generateQuiz(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz generated successfully", quiz));
    }

    /**
     * GET /api/quizzes/{id}
     * Fetch a quiz by ID (owner or public).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        QuizResponse quiz = quizService.getQuizById(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(quiz));
    }

    /**
     * GET /api/quizzes/my
     * Get all quizzes created by the current user (paginated).
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getMyQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizResponse> quizzes = quizService.getUserQuizzes(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    /**
     * GET /api/quizzes/public
     * Browse all publicly available quizzes.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getPublicQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizResponse> quizzes = quizService.getPublicQuizzes(pageable);
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    /**
     * GET /api/quizzes/public/search?keyword=java
     * Search public quizzes by title or topic.
     */
    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> searchPublicQuizzes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizResponse> quizzes = quizService.searchPublicQuizzes(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    /**
     * DELETE /api/quizzes/{id}
     * Delete a quiz (owner only).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        quizService.deleteQuiz(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted successfully", null));
    }
}
