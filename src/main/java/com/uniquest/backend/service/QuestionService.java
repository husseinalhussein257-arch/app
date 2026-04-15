package com.uniquest.backend.service;

import com.uniquest.backend.dto.admin.CreateQuestionRequest;
import com.uniquest.backend.dto.admin.QuestionResponse;
import com.uniquest.backend.dto.admin.UploadResponse;
import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface QuestionService {

    void createQuestion(CreateQuestionRequest request);

    /**
     * Parses a CSV or Excel file and bulk-inserts valid questions.
     * Returns a report of how many rows succeeded / failed.
     */
    UploadResponse uploadQuestions(MultipartFile file, String subjectId);

    /**
     * Paginated admin list. All filters are optional and compose independently.
     * Results are sorted by createdAt DESC.
     */
    Page<QuestionResponse> getQuestions(String subjectId, QuestionCategory category,
                                        QuestionFormat format, int page, int size);

    QuestionResponse updateQuestion(String id, CreateQuestionRequest request);

    void deleteQuestion(String id);
}
