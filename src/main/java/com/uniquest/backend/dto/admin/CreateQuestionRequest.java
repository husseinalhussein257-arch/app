package com.uniquest.backend.dto.admin;

import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class CreateQuestionRequest {

    @NotBlank(message = "subjectId is required")
    private String subjectId;

    @NotNull(message = "category is required (IMPORTANT or EXAM)")
    private QuestionCategory category;

    @NotNull(message = "format is required (MCQ or TRUE_FALSE)")
    private QuestionFormat format;

    @NotBlank(message = "questionText is required")
    private String questionText;

    /**
     * MCQ     → expected keys: "A", "B", "C", "D"
     * TRUE_FALSE → expected keys: "A", "B"
     * Validated in the service layer based on the format field.
     */
    @NotNull(message = "options are required")
    private Map<String, String> options;

    /**
     * MCQ: must be "A", "B", "C", or "D"
     * TRUE_FALSE: must be "A" or "B"
     * DTO-level pattern accepts A–D; service validates further for TRUE_FALSE.
     */
    @NotBlank(message = "correctAnswer is required")
    @Pattern(regexp = "^[ABCD]$", message = "correctAnswer must be A, B, C, or D")
    private String correctAnswer;

    /** Optional — shown to students after submission */
    private String explanation;
}
