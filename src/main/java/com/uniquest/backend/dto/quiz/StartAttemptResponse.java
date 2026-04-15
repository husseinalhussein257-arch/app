package com.uniquest.backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Returned after a student starts a quiz attempt.
 *
 * The frontend should store attemptId locally and send it back on submit.
 * Questions are randomized and correctAnswers are NOT included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartAttemptResponse {

    private String attemptId;
    private List<QuestionDTO> questions;
}
