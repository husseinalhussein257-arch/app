package com.uniquest.backend.repository;

import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

    // ── Quiz attempt queries (soft-delete aware) ──────────────────────────────

    List<Question> findBySubjectIdAndCategoryAndDeletedFalse(String subjectId, QuestionCategory category);

    long countBySubjectIdAndCategoryAndDeletedFalse(String subjectId, QuestionCategory category);

    // ── Validation ────────────────────────────────────────────────────────────

    boolean existsBySubjectIdAndDeletedFalse(String subjectId);
}
