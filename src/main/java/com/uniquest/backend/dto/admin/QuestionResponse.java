package com.uniquest.backend.dto.admin;

import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private String id;
    private String subjectId;
    private QuestionCategory category;
    private QuestionFormat format;
    private String questionText;

    /** Options as A→text, B→text, C→text (null for TRUE_FALSE), D→text (null for TRUE_FALSE). */
    private Map<String, String> options;

    private String correctAnswer;
    private String explanation;
    private Instant createdAt;
}
