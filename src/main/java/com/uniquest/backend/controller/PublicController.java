package com.uniquest.backend.controller;

import com.uniquest.backend.dto.common.ApiResponse;
import com.uniquest.backend.dto.common.BranchResponse;
import com.uniquest.backend.dto.common.UniversityResponse;
import com.uniquest.backend.service.BranchService;
import com.uniquest.backend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class PublicController {

    private final UniversityService universityService;
    private final BranchService branchService;

    /**
     * GET /universities
     *
     * Returns all non-deleted universities as a simple list (no pagination).
     * Response: { "success": true, "data": [{ "id": "...", "name": "..." }, ...] }
     */
    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<List<UniversityResponse>>> getUniversities() {
        return ResponseEntity.ok(ApiResponse.ok(universityService.getAllUniversities()));
    }

    /**
     * GET /branches
     *
     * Returns all non-deleted branches as a simple list (no pagination).
     * Response: { "success": true, "data": [{ "id": "...", "name": "..." }, ...] }
     */
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches() {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getAllBranches()));
    }
}
