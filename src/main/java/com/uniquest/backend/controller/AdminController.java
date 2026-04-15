package com.uniquest.backend.controller;

import com.uniquest.backend.dto.admin.*;
import com.uniquest.backend.dto.common.ApiResponse;
import com.uniquest.backend.dto.common.BranchResponse;
import com.uniquest.backend.dto.common.SubjectResponse;
import com.uniquest.backend.dto.common.UniversityResponse;
import com.uniquest.backend.dto.common.YearResponse;
import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import com.uniquest.backend.service.BranchService;
import com.uniquest.backend.service.QuestionService;
import com.uniquest.backend.service.SubjectService;
import com.uniquest.backend.service.UniversityService;
import com.uniquest.backend.service.YearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")   // all endpoints in this controller require ADMIN
@RequiredArgsConstructor
public class AdminController {

    private final UniversityService universityService;
    private final BranchService branchService;
    private final YearService yearService;
    private final SubjectService subjectService;
    private final QuestionService questionService;

    // ── University management ────────────────────────────────────────────────

    /**
     * POST /admin/universities
     *
     * Request:  { "name": "University of XYZ" }
     * Response: { "success": true, "data": { "id": "...", "name": "University of XYZ" } }
     */
    @PostMapping("/universities")
    public ResponseEntity<ApiResponse<UniversityResponse>> createUniversity(
            @Valid @RequestBody CreateUniversityRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(universityService.createUniversity(request)));
    }

    /**
     * GET /admin/universities?page=0&size=20
     *
     * Returns a paginated, name-sorted list of universities.
     * Response: { "success": true, "data": { "content": [...], "totalElements": N, ... } }
     */
    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<Page<UniversityResponse>>> getUniversities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(universityService.getUniversities(page, size)));
    }

    /**
     * PUT /admin/universities/{id}
     *
     * Request:  { "name": "New Name" }
     * Response: { "success": true, "data": { "id": "...", "name": "New Name" } }
     * 404 if id not found, 409 if name already taken by another university.
     */
    @PutMapping("/universities/{id}")
    public ResponseEntity<ApiResponse<UniversityResponse>> updateUniversity(
            @PathVariable String id,
            @Valid @RequestBody CreateUniversityRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(universityService.updateUniversity(id, request)));
    }

    /** DELETE /admin/universities/{id} — soft-deletes; returns 204 No Content. */
    @DeleteMapping("/universities/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable String id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }

    // ── Branch management ────────────────────────────────────────────────────

    /**
     * POST /admin/branches
     *
     * Request:  { "name": "Computer Science" }
     * Response: { "success": true, "data": { "id": "...", "name": "Computer Science" } }
     */
    @PostMapping("/branches")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody CreateBranchRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(branchService.createBranch(request)));
    }

    /**
     * PUT /admin/branches/{id}
     *
     * Request:  { "name": "New Name" }
     * Response: { "success": true, "data": { "id": "...", "name": "New Name" } }
     * 404 if id not found, 409 if name already taken by another branch.
     */
    @PutMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable String id,
            @Valid @RequestBody CreateBranchRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(branchService.updateBranch(id, request)));
    }

    /** DELETE /admin/branches/{id} — soft-deletes; returns 204 No Content. */
    @DeleteMapping("/branches/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable String id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

    // ── Year management ──────────────────────────────────────────────────────

    /**
     * POST /admin/years
     *
     * Request:  { "name": "Year 2" }
     * Response: { "success": true, "data": { "id": "...", "name": "Year 2" } }
     */
    @PostMapping("/years")
    public ResponseEntity<ApiResponse<YearResponse>> createYear(
            @Valid @RequestBody CreateYearRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(yearService.createYear(request)));
    }

    /**
     * PUT /admin/years/{id}
     *
     * Request:  { "name": "New Name" }
     * Response: { "success": true, "data": { "id": "...", "name": "New Name" } }
     * 404 if id not found, 409 if name already taken by another year.
     */
    @PutMapping("/years/{id}")
    public ResponseEntity<ApiResponse<YearResponse>> updateYear(
            @PathVariable String id,
            @Valid @RequestBody CreateYearRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(yearService.updateYear(id, request)));
    }

    /** DELETE /admin/years/{id} — soft-deletes; returns 204 No Content. */
    @DeleteMapping("/years/{id}")
    public ResponseEntity<Void> deleteYear(@PathVariable String id) {
        yearService.deleteYear(id);
        return ResponseEntity.noContent().build();
    }

    // ── Subject management ───────────────────────────────────────────────────

    /**
     * POST /admin/subjects
     *
     * Request:  { "name": "Mathematics", "yearId": "...", "branchIds": ["...", "..."] }
     * Response: { "success": true, "data": { "id": "...", "name": "Mathematics" } }
     */
    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(subjectService.createSubject(request)));
    }

    /**
     * PUT /admin/subjects/{id}
     *
     * Request:  { "name": "...", "yearId": "...", "branchIds": ["...", "..."] }
     * Response: { "success": true, "data": { "id": "...", "name": "...", "yearId": "...", "branchIds": [...] } }
     * 404 if subject/year/any branchId not found.
     */
    @PutMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable String id,
            @Valid @RequestBody CreateSubjectRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(subjectService.updateSubject(id, request)));
    }

    /** DELETE /admin/subjects/{id} — soft-deletes; returns 204 No Content. */
    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * GET /admin/subjects?universityId=&branchId=&yearId=&page=0&size=20
     *
     * Returns a paginated, name-sorted list of subjects.
     * All filters are optional and can be combined.
     * Response: { "success": true, "data": { "content": [...], "totalElements": N, ... } }
     */
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<Page<SubjectResponse>>> getSubjects(
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String yearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(subjectService.getSubjects(universityId, branchId, yearId, page, size)));
    }

    // ── Question management ──────────────────────────────────────────────────

    /**
     * POST /admin/questions
     *
     * Manually creates a single question.
     *
     * Request:
     * {
     *   "subjectId": "...",
     *   "type": "EXAM",
     *   "questionText": "What is 2+2?",
     *   "options": { "A": "1", "B": "2", "C": "4", "D": "5" },
     *   "correctAnswer": "C",
     *   "explanation": "Basic arithmetic."
     * }
     *
     * Response 201: { "success": true, "message": "Question created" }
     */
    /**
     * GET /admin/questions?subjectId=&category=EXAM&format=MCQ&page=0&size=20
     *
     * Paginated question list for admin. All filters are optional.
     * Sorted by createdAt DESC (newest first).
     */
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<Page<QuestionResponse>>> getQuestions(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) QuestionCategory category,
            @RequestParam(required = false) QuestionFormat format,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(
                questionService.getQuestions(subjectId, category, format, page, size)));
    }

    /**
     * PUT /admin/questions/{id}
     *
     * Updates all question fields. Validates format-correctAnswer consistency:
     * TRUE_FALSE → correctAnswer must be A or B; MCQ → A, B, C, or D.
     * 404 if question or subjectId not found.
     */
    @PutMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody CreateQuestionRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(questionService.updateQuestion(id, request)));
    }

    /** DELETE /admin/questions/{id} — soft-deletes; returns 204 No Content. */
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<Void>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {

        questionService.createQuestion(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Question created", null));
    }

    /**
     * POST /admin/questions/upload
     *
     * Bulk upload questions from a CSV or Excel file.
     *
     * CSV column order: subjectId, type, questionText, A, B, C, D, correctAnswer, explanation
     *
     * Response 200:
     * {
     *   "success": true,
     *   "data": {
     *     "total": 50,
     *     "success": 48,
     *     "failed": 2,
     *     "errors": ["Row 12: missing correctAnswer", "Row 31: invalid type"]
     *   }
     * }
     */
    @PostMapping("/questions/upload")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectId") String subjectId) {

        UploadResponse result = questionService.uploadQuestions(file, subjectId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    /**
     * GET /admin/branches?page=0&size=20
     *
     * Returns a paginated, name-sorted list of branches.
     * Response: { "success": true, "data": { "content": [...], "totalElements": N, ... } }
     */
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> getBranches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(branchService.getBranches(page, size)));
    }
    /**
     * GET /admin/years?page=0&size=20
     *
     * Returns a paginated, name-sorted list of years.
     * Response: { "success": true, "data": { "content": [...], "totalElements": N, ... } }
     */
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<Page<YearResponse>>> getYears(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(yearService.getYears(page, size)));
    }
}
