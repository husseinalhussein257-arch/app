package com.uniquest.backend.service;

import com.uniquest.backend.dto.quiz.ResultResponse;
import com.uniquest.backend.dto.quiz.StartAttemptRequest;
import com.uniquest.backend.dto.quiz.StartAttemptResponse;
import com.uniquest.backend.dto.quiz.SubmitAttemptRequest;

public interface QuizAttemptService {

    /**
     * Creates a new attempt, randomly selects questions, returns them without correctAnswers.
     *
     * @param username  JWT subject — resolved to User inside the service
     */
    StartAttemptResponse startAttempt(String username, StartAttemptRequest request);

    /**
     * Scores the attempt, persists the full result, returns the review.
     * Throws ConflictException if already submitted.
     * Throws ResourceNotFoundException if attempt not found or not owned by username.
     *
     * @param username   JWT subject
     * @param attemptId  path variable from the request
     */
    ResultResponse submitAttempt(String username, String attemptId, SubmitAttemptRequest request);

    /**
     * Returns a previously submitted result.
     * Throws if attempt is IN_PROGRESS or doesn't belong to username.
     */
    ResultResponse getResult(String username, String attemptId);
}
