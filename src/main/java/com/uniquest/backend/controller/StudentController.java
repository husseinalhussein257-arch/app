package com.uniquest.backend.controller;

import com.uniquest.backend.dto.common.ApiResponse;
import com.uniquest.backend.dto.common.SubjectResponse;
import com.uniquest.backend.dto.common.YearResponse;
import com.uniquest.backend.dto.quiz.ResultResponse;
import com.uniquest.backend.dto.quiz.StartAttemptRequest;
import com.uniquest.backend.dto.quiz.StartAttemptResponse;
import com.uniquest.backend.dto.quiz.SubmitAttemptRequest;
import com.uniquest.backend.exception.ForbiddenException;
import com.uniquest.backend.security.CustomUserDetails;
import com.uniquest.backend.service.BranchService;
import com.uniquest.backend.service.QuizAttemptService;
import com.uniquest.backend.service.SubjectService;
import com.uniquest.backend.service.YearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final YearService yearService;
    private final SubjectService subjectService;
    private final QuizAttemptService quizAttemptService;

    // ── Discovery endpoints (accessible by both roles) ───────────────────────

    /**
     * GET /years
     *
     * Returns all academic years.
     *
     * Response 200:
     *   { "success": true, "data": [ { "id": "...", "name": "Year 1" } ] }
     */
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<YearResponse>>> getYears() {
        return ResponseEntity.ok(ApiResponse.ok(yearService.getAllYears()));
    }

    /**
     * GET /subjects?branchId=xxx&yearId=yyy
     *
     * Returns subjects for the authenticated user's university, filtered by branch and/or year.
     * Both params are optional — the service handles partial filtering.
     *
     * universityId is extracted from JWT and enforced — never accepted from request.
     *
     * Response 200:
     *   { "success": true, "data": [ { "id": "...", "name": "Mathematics" } ] }
     * Response 403: user has no universityId assigned in JWT
     */
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String yearId,
            @AuthenticationPrincipal CustomUserDetails user) {

        // Extract universityId from authenticated principal (JWT claim)
        String universityId = user.getUniversityId();

        // Enforce that user has a universityId
        if (universityId == null) {
            throw new ForbiddenException("User is not assigned to any university");
        }

        // Always filter by user's universityId — never accept from request
        return ResponseEntity.ok(ApiResponse.ok(
                subjectService.getSubjects(universityId, branchId, yearId)));
    }

    // ── Quiz attempt endpoints (STUDENT only) ────────────────────────────────

    /**
     * POST /student/attempts/start
     *
     * Starts a new quiz attempt. Returns randomly ordered questions WITHOUT answers.
     *
     * Request:
     *   { "subjectId": "...", "type": "EXAM" }
     *
     * Response 200:
     *   { "success": true, "data": { "attemptId": "...", "questions": [...] } }
     */
    @PostMapping("/student/attempts/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StartAttemptResponse>> startAttempt(
            @Valid @RequestBody StartAttemptRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // userId is extracted from the JWT principal — never trusted from request body
        StartAttemptResponse response = quizAttemptService.startAttempt(
                userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * POST /student/attempts/{attemptId}/submit
     *
     * Submits answers, scores the attempt, and returns the full result.
     * Can only be called once per attempt.
     *
     * Request:
     *   { "answers": [ { "questionId": "...", "selectedAnswer": "B" }, ... ] }
     *
     * Response 200:
     *   { "success": true, "data": { "totalQuestions": 20, "correctCount": 15, ... } }
     *
     * Response 400: attempt already submitted
     * Response 403: attempt doesn't belong to this user
     */
    @PostMapping("/student/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResultResponse>> submitAttempt(
            @PathVariable String attemptId,
            @Valid @RequestBody SubmitAttemptRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResultResponse result = quizAttemptService.submitAttempt(
                userDetails.getUsername(), attemptId, request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /student/attempts/{attemptId}/result
     *
     * Retrieves a previously submitted result. Useful for reviewing past quizzes.
     *
     * Response 200: same shape as submit response
     * Response 403: attempt doesn't belong to this user
     * Response 404: attempt not found
     */
    @GetMapping("/student/attempts/{attemptId}/result")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResultResponse>> getResult(
            @PathVariable String attemptId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResultResponse result = quizAttemptService.getResult(
                userDetails.getUsername(), attemptId);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
