package com.uniquest.backend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document inside QuizAttempt.answers array.
 *
 * Design decision: answers are embedded (not a separate collection)
 * because they are always accessed together with their parent attempt.
 * Embedding avoids N+1 lookups and keeps result retrieval a single read.
 *
 * We snapshot correctAnswer here so the result is stable even if the
 * question document is later edited by an admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    /** Reference to Question._id */
    private String questionId;

    /** The option the student picked: "A", "B", "C", "D", or null if skipped */
    private String selectedAnswer;

    /** Snapshotted at submit time — decoupled from live question data */
    private String correctAnswer;

    private boolean correct;
}
