package com.uniquest.backend.repository;

import com.uniquest.backend.enums.AttemptStatus;
import com.uniquest.backend.model.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    /**
     * Ownership-aware lookup — used by submit and result endpoints.
     * Combining id + userId in a single query prevents ID-enumeration attacks:
     * a student cannot access another student's attempt even with a valid attemptId.
     */
    Optional<QuizAttempt> findByIdAndUserId(String id, String userId);

    /** Useful for a future "my history" endpoint. */
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(String userId);

    /** Check if a user has an active in-progress attempt for a subject (optional guard). */
    boolean existsByUserIdAndSubjectIdAndStatus(String userId, String subjectId, AttemptStatus status);
}
