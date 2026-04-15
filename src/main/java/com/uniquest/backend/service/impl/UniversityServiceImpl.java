package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.admin.CreateUniversityRequest;
import com.uniquest.backend.dto.common.UniversityResponse;
import com.uniquest.backend.exception.ConflictException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.University;
import com.uniquest.backend.repository.UniversityRepository;
import com.uniquest.backend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

    @Override
    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<UniversityResponse> getUniversities(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return universityRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    public UniversityResponse createUniversity(CreateUniversityRequest request) {
        if (universityRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ConflictException("University already exists: " + request.getName());
        }

        University university = University.builder()
                .name(request.getName())
                .build();

        return toResponse(universityRepository.save(university));
    }

    @Override
    public UniversityResponse updateUniversity(String id, CreateUniversityRequest request) {
        University university = universityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("University", id));

        String newName = request.getName().trim();
        if (!university.getName().equals(newName) && universityRepository.existsByNameAndDeletedFalse(newName)) {
            throw new ConflictException("University already exists: " + newName);
        }

        university.setName(newName);
        return toResponse(universityRepository.save(university));
    }

    @Override
    public void deleteUniversity(String id) {
        University university = universityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("University", id));

        university.setDeleted(true);
        universityRepository.save(university);
    }

    private UniversityResponse toResponse(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .build();
    }
}
