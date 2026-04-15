package com.uniquest.backend.model.embedded;

import com.uniquest.backend.enums.QuestionFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Immutable snapshot of a question captured at attempt-start time.
 *
 * Embedded inside QuizAttempt.questionSnapshots so that:
 *   - The result review page is accurate even if an admin later edits
 *     or deletes the original Question document.
 *   - Scoring at submit time reads from this snapshot, NOT the live
 *     questions collection, keeping results historically stable.
 *
 * This is the single source of truth for everything the student saw
 * during their quiz.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSnapshot {

    /** Reference back to Question._id — used to link with Answer records. */
    private String questionId;

    private String questionText;

    /**
     * Keys: "A","B","C","D" for MCQ — or "A","B" for TRUE_FALSE.
     * Values: option text at snapshot time.
     */
    private Map<String, String> options;

    /** MCQ or TRUE_FALSE — tells the frontend how to render options. */
    private QuestionFormat format;

    /** Correct answer key at snapshot time. */
    private String correctAnswer;

    /** Explanation shown to student after submission. */
    private String explanation;
}
