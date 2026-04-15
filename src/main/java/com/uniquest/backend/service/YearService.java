package com.uniquest.backend.service;

import com.uniquest.backend.dto.admin.CreateYearRequest;
import com.uniquest.backend.dto.common.YearResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface YearService {

    List<YearResponse> getAllYears();

    Page<YearResponse> getYears(int page, int size);

    YearResponse createYear(CreateYearRequest request);

    YearResponse updateYear(String id, CreateYearRequest request);

    void deleteYear(String id);
}
