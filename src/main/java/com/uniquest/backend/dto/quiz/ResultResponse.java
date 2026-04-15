package com.uniquest.backend.dto.quiz;

import com.uniquest.backend.enums.PerformanceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full quiz result — returned after submit or on GET /result.
 *
 * Designed for the Angular result page:
 * - Summary stats at the top
 * - Full per-question review below
 *
 * 'performance' gives the frontend a label to display
 * (e.g., a color-coded badge: EXCELLENT=green, NEEDS_IMPROVEMENT=red).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {

    // ── Summary ─────────────────────────────────────────────────────────────
    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private double percentage;
    private long durationSeconds;
    private PerformanceLevel performance;

    // ── Per-question review ──────────────────────────────────────────────────
    private List<QuestionReviewDTO> review;
}
