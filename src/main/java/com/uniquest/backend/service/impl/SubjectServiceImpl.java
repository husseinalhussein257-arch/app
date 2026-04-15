package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.admin.CreateSubjectRequest;
import com.uniquest.backend.dto.common.SubjectResponse;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.Subject;
import com.uniquest.backend.repository.BranchRepository;
import com.uniquest.backend.repository.SubjectRepository;
import com.uniquest.backend.repository.UniversityRepository;
import com.uniquest.backend.repository.YearRepository;
import com.uniquest.backend.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final UniversityRepository universityRepository;
    private final YearRepository yearRepository;
    private final BranchRepository branchRepository;

    @Override
    public List<SubjectResponse> getSubjects(String universityId, String branchId, String yearId) {
        List<Subject> subjects;

        if (universityId != null && branchId != null && yearId != null) {
            subjects = subjectRepository.findByUniversityIdAndYearIdAndBranchIdsContainingAndDeletedFalse(universityId, yearId, branchId);
        } else if (universityId != null && yearId != null) {
            subjects = subjectRepository.findByUniversityIdAndYearIdAndDeletedFalse(universityId, yearId);
        } else if (universityId != null && branchId != null) {
            subjects = subjectRepository.findByUniversityIdAndBranchIdsContainingAndDeletedFalse(universityId, branchId);
        } else if (universityId != null) {
            subjects = subjectRepository.findByUniversityIdAndDeletedFalse(universityId);
        } else if (branchId != null && yearId != null) {
            subjects = subjectRepository.findByYearIdAndBranchIdsContainingAndDeletedFalse(yearId, branchId);
        } else if (yearId != null) {
            subjects = subjectRepository.findByYearIdAndDeletedFalse(yearId);
        } else if (branchId != null) {
            subjects = subjectRepository.findByBranchIdsContainingAndDeletedFalse(branchId);
        } else {
            subjects = subjectRepository.findByDeletedFalse();
        }

        return subjects.stream().map(this::toResponse).toList();
    }

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        if (!universityRepository.existsByIdAndDeletedFalse(request.getUniversityId())) {
            throw new ResourceNotFoundException("University", request.getUniversityId());
        }

        if (!yearRepository.existsByIdAndDeletedFalse(request.getYearId())) {
            throw new ResourceNotFoundException("Year", request.getYearId());
        }

        for (String branchId : request.getBranchIds()) {
            if (!branchRepository.existsByIdAndDeletedFalse(branchId)) {
                throw new ResourceNotFoundException("Branch", branchId);
            }
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .universityId(request.getUniversityId())
                .yearId(request.getYearId())
                .branchIds(request.getBranchIds())
                .build();

        return toResponse(subjectRepository.save(subject));
    }

    @Override
    public SubjectResponse updateSubject(String id, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));

        if (!universityRepository.existsByIdAndDeletedFalse(request.getUniversityId())) {
            throw new ResourceNotFoundException("University", request.getUniversityId());
        }

        if (!yearRepository.existsByIdAndDeletedFalse(request.getYearId())) {
            throw new ResourceNotFoundException("Year", request.getYearId());
        }

        for (String branchId : request.getBranchIds()) {
            if (!branchRepository.existsByIdAndDeletedFalse(branchId)) {
                throw new ResourceNotFoundException("Branch", branchId);
            }
        }

        subject.setName(request.getName().trim());
        subject.setUniversityId(request.getUniversityId());
        subject.setYearId(request.getYearId());
        subject.setBranchIds(request.getBranchIds());

        return toResponse(subjectRepository.save(subject));
    }

    @Override
    public Page<SubjectResponse> getSubjects(String universityId, String branchId, String yearId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        if (universityId != null && branchId != null && yearId != null) {
            return subjectRepository.findByUniversityIdAndYearIdAndBranchIdsContainingAndDeletedFalse(universityId, yearId, branchId, pageable)
                    .map(this::toResponse);
        } else if (universityId != null && yearId != null) {
            return subjectRepository.findByUniversityIdAndYearIdAndDeletedFalse(universityId, yearId, pageable)
                    .map(this::toResponse);
        } else if (universityId != null && branchId != null) {
            return subjectRepository.findByUniversityIdAndBranchIdsContainingAndDeletedFalse(universityId, branchId, pageable)
                    .map(this::toResponse);
        } else if (universityId != null) {
            return subjectRepository.findByUniversityIdAndDeletedFalse(universityId, pageable).map(this::toResponse);
        } else if (branchId != null && yearId != null) {
            return subjectRepository.findByYearIdAndBranchIdsContainingAndDeletedFalse(yearId, branchId, pageable)
                    .map(this::toResponse);
        } else if (yearId != null) {
            return subjectRepository.findByYearIdAndDeletedFalse(yearId, pageable).map(this::toResponse);
        } else if (branchId != null) {
            return subjectRepository.findByBranchIdsContainingAndDeletedFalse(branchId, pageable).map(this::toResponse);
        } else {
            return subjectRepository.findByDeletedFalse(pageable).map(this::toResponse);
        }
    }

    @Override
    public void deleteSubject(String id) {
        Subject subject = subjectRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));

        subject.setDeleted(true);
        subjectRepository.save(subject);
    }

    @Override
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findByDeletedFalse().stream().map(this::toResponse).toList();
    }

    private SubjectResponse toResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .universityId(subject.getUniversityId())
                .yearId(subject.getYearId())
                .branchIds(subject.getBranchIds())
                .build();
    }
}
