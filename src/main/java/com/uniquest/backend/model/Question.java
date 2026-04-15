package com.uniquest.backend.model;

import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB collection: questions
 *
 * Compound index on (subjectId, category) covers the primary query pattern:
 * "give me all EXAM questions for subject X".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
@CompoundIndex(name = "subject_category_idx", def = "{'subjectId': 1, 'category': 1}")
public class Question {

    @Id
    private String id;

    /** Reference to Subject._id */
    private String subjectId;

    /** Educational purpose: IMPORTANT or EXAM */
    private QuestionCategory category;

    /** Answer structure: MCQ (A–D) or TRUE_FALSE (A–B) */
    private QuestionFormat format;

    private String questionText;

    private Options options;

    /** Correct answer key: "A"/"B"/"C"/"D" for MCQ, "A"/"B" for TRUE_FALSE */
    private String correctAnswer;

    /** Explanation shown to student after submission */
    private String explanation;

    /** Set automatically on first save via @EnableMongoAuditing. */
    @CreatedDate
    private Instant createdAt;

    private boolean deleted = false;

    /**
     * Embedded options — keeps the question document self-contained.
     * Named fields (a/b/c/d) instead of a Map so MongoDB stores them
     * as predictable document fields, not dynamic keys.
     *
     * For TRUE_FALSE questions, c and d are null/empty.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        private String a;
        private String b;
        /** Null for TRUE_FALSE questions. */
        private String c;
        /** Null for TRUE_FALSE questions. */
        private String d;
    }
}
