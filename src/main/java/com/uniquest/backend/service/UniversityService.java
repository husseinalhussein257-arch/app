package com.uniquest.backend.service;

import com.uniquest.backend.dto.admin.CreateUniversityRequest;
import com.uniquest.backend.dto.common.UniversityResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UniversityService {

    List<UniversityResponse> getAllUniversities();

    Page<UniversityResponse> getUniversities(int page, int size);

    UniversityResponse createUniversity(CreateUniversityRequest request);

    UniversityResponse updateUniversity(String id, CreateUniversityRequest request);

    void deleteUniversity(String id);
}
