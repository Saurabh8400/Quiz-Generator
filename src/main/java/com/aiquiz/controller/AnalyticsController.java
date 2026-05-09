package com.aiquiz.controller;

import com.aiquiz.dto.ApiResponse;
import com.aiquiz.dto.UserAnalyticsResponse;
import com.aiquiz.security.CustomUserDetails;
import com.aiquiz.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/me
     * Returns quiz performance analytics for the authenticated user:
     * total quizzes, total attempts, average score, highest score, recent history.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserAnalyticsResponse>> getMyAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
