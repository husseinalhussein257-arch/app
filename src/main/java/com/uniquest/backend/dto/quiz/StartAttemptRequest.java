package com.uniquest.backend.dto.quiz;

import com.uniquest.backend.enums.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartAttemptRequest {

    @NotBlank(message = "subjectId is required")
    private String subjectId;

    @NotNull(message = "category is required (IMPORTANT or EXAM)")
    private QuestionCategory category;
}
