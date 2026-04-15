package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.admin.CreateYearRequest;
import com.uniquest.backend.dto.common.YearResponse;
import com.uniquest.backend.exception.ConflictException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.Year;
import com.uniquest.backend.repository.YearRepository;
import com.uniquest.backend.service.YearService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YearServiceImpl implements YearService {

    private final YearRepository yearRepository;

    @Override
    public List<YearResponse> getAllYears() {
        return yearRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<YearResponse> getYears(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return yearRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    public YearResponse createYear(CreateYearRequest request) {
        if (yearRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ConflictException("Year already exists: " + request.getName());
        }

        Year year = Year.builder()
                .name(request.getName())
                .build();

        return toResponse(yearRepository.save(year));
    }

    @Override
    public YearResponse updateYear(String id, CreateYearRequest request) {
        Year year = yearRepository.findById(id)
                .filter(y -> !y.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Year", id));

        String newName = request.getName().trim();
        if (!year.getName().equals(newName) && yearRepository.existsByNameAndDeletedFalse(newName)) {
            throw new ConflictException("Year already exists: " + newName);
        }

        year.setName(newName);
        return toResponse(yearRepository.save(year));
    }

    @Override
    public void deleteYear(String id) {
        Year year = yearRepository.findById(id)
                .filter(y -> !y.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Year", id));

        year.setDeleted(true);
        yearRepository.save(year);
    }

    private YearResponse toResponse(Year year) {
        return YearResponse.builder()
                .id(year.getId())
                .name(year.getName())
                .build();
    }
}
