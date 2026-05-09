package com.aiquiz.controller;

import com.aiquiz.dto.ApiResponse;
import com.aiquiz.dto.AttemptResultResponse;
import com.aiquiz.dto.SubmitAttemptRequest;
import com.aiquiz.security.CustomUserDetails;
import com.aiquiz.service.AttemptService;
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
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    /**
     * POST /api/attempts/submit
     * Submit answers for a quiz attempt; returns scored result.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> submitAttempt(
            @Valid @RequestBody SubmitAttemptRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AttemptResultResponse result = attemptService.submitAttempt(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz submitted successfully", result));
    }

    /**
     * GET /api/attempts/{id}
     * Get result of a specific attempt.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> getAttemptResult(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AttemptResultResponse result = attemptService.getAttemptResult(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/attempts/my
     * Get all attempts by the current user (paginated).
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<AttemptResultResponse>>> getMyAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AttemptResultResponse> attempts =
                attemptService.getUserAttempts(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(attempts));
    }
}
