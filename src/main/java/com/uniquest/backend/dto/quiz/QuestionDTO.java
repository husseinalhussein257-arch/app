package com.uniquest.backend.dto.quiz;

import com.uniquest.backend.enums.QuestionFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Question as served to the student during an active quiz.
 *
 * SECURITY: correctAnswer is intentionally EXCLUDED.
 * Exposing it here would allow trivial cheating via network inspection.
 *
 * format is included so the frontend can render the correct number of
 * option buttons without hardcoding 4 — MCQ shows A–D, TRUE_FALSE shows A–B.
 *
 * options is a Map<String, String> keyed by the available answer letters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private String id;
    private String questionText;

    /**
     * MCQ: keys "A","B","C","D"
     * TRUE_FALSE: keys "A","B"
     */
    private Map<String, String> options;

    /** Tells the frontend how many options to render. */
    private QuestionFormat format;
}
