package com.uniquest.backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Per-question breakdown inside a result response.
 *
 * Unlike QuestionDTO (served during the quiz), this DOES include
 * correctAnswer and explanation — because the attempt is already
 * submitted and scored.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReviewDTO {

    private String questionText;

    /** Keys: "A", "B", "C", "D" */
    private Map<String, String> options;

    /** What the student picked — null means skipped */
    private String selectedAnswer;

    private String correctAnswer;
    private boolean correct;

    /** Admin-authored explanation shown after submission */
    private String explanation;
}
