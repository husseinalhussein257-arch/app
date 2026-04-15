package com.uniquest.backend.enums;

/**
 * Thresholds:
 *   >= 85%  -> EXCELLENT
 *   >= 65%  -> GOOD
 *   >= 50%  -> AVERAGE
 *   < 50%   -> NEEDS_IMPROVEMENT
 */
public enum PerformanceLevel {
    EXCELLENT,
    GOOD,
    AVERAGE,
    NEEDS_IMPROVEMENT;

    public static PerformanceLevel fromPercentage(double percentage) {
        if (percentage >= 85) return EXCELLENT;
        if (percentage >= 65) return GOOD;
        if (percentage >= 50) return AVERAGE;
        return NEEDS_IMPROVEMENT;
    }
}
