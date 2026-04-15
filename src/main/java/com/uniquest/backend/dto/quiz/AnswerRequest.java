package com.uniquest.backend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * A single question answer submitted by the student.
 *
 * selectedAnswer is validated to be one of A/B/C/D only.
 * If the student skipped a question the frontend should
 * omit it entirely rather than sending null.
 */
@Data
public class AnswerRequest {

    @NotBlank(message = "questionId is required")
    private String questionId;

    /** Must be exactly "A", "B", "C", or "D" */
    @NotBlank(message = "selectedAnswer is required")
    @Pattern(regexp = "^[ABCD]$", message = "selectedAnswer must be A, B, C, or D")
    private String selectedAnswer;
}
