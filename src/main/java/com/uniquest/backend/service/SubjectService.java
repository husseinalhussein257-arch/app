package com.uniquest.backend.service;

import com.uniquest.backend.dto.admin.CreateSubjectRequest;
import com.uniquest.backend.dto.common.SubjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubjectService {

    /**
     * Returns subjects matching filters (universityId, branchId, yearId).
     * All filters can be null — the service handles partial filtering.
     */
    List<SubjectResponse> getSubjects(String universityId, String branchId, String yearId);

    /**
     * Paginated subjects for admin, with optional universityId/branchId/yearId filters, sorted by name ASC.
     */
    Page<SubjectResponse> getSubjects(String universityId, String branchId, String yearId, int page, int size);

    SubjectResponse createSubject(CreateSubjectRequest request);

    SubjectResponse updateSubject(String id, CreateSubjectRequest request);

    void deleteSubject(String id);

    List<SubjectResponse> getAllSubjects();
}
