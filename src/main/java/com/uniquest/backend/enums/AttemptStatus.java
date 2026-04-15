package com.uniquest.backend.enums;

/**
 * Lifecycle state of a quiz attempt.
 *
 * IN_PROGRESS  - attempt started, student is answering questions
 * SUBMITTED    - student submitted; result is finalized and stored
 *
 * A SUBMITTED attempt must never be re-submitted (enforced in service).
 */
public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED
}
