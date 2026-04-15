package com.uniquest.backend.model;

import com.uniquest.backend.enums.AttemptStatus;
import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.model.embedded.Answer;
import com.uniquest.backend.model.embedded.QuestionSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB collection: quiz_attempts
 *
 * Lifecycle: IN_PROGRESS -> SUBMITTED
 *
 * Snapshot design:
 *   questionSnapshots stores a full copy of every question served to the student.
 *   This means results remain accurate even if an admin later edits or deletes
 *   the original Question document. The submit and result endpoints read ONLY
 *   from this document — no secondary queries to the questions collection.
 *
 * Storage trade-off:
 *   Each attempt is slightly larger (~5–20 KB) but result reads are a single
 *   O(1) MongoDB find — no joins, no secondary lookups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quiz_attempts")
@CompoundIndex(name = "user_subject_idx", def = "{'userId': 1, 'subjectId': 1}")
public class QuizAttempt {

    @Id
    private String id;

    /** Reference to User._id — indexed for ownership lookups */
    @Indexed
    private String userId;

    /** Reference to Subject._id */
    private String subjectId;

    /** The category used when this attempt was started. */
    private QuestionCategory category;

    /** IN_PROGRESS until the student submits. Indexed for status-filtered queries. */
    @Indexed
    private AttemptStatus status;

    private Instant startedAt;

    /** Populated on submit. */
    private Instant finishedAt;

    /**
     * Immutable snapshots of every question in this attempt.
     * Captured at start time — decoupled from the live questions collection.
     */
    private List<QuestionSnapshot> questionSnapshots;

    // ── Result fields — all populated atomically on submit ───────────────────

    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private double percentage;

    /** Wall-clock seconds from startedAt to finishedAt. */
    private long durationSeconds;

    /**
     * One Answer per question in questionSnapshots (including skipped ones).
     * Embedded so GET /result is a single document read with zero joins.
     */
    private List<Answer> answers;
}
