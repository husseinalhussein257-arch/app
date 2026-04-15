package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.admin.CreateBranchRequest;
import com.uniquest.backend.dto.common.BranchResponse;
import com.uniquest.backend.exception.ConflictException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.Branch;
import com.uniquest.backend.repository.BranchRepository;
import com.uniquest.backend.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<BranchResponse> getBranches(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return branchRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    public BranchResponse createBranch(CreateBranchRequest request) {
        if (branchRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ConflictException("Branch already exists: " + request.getName());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .build();

        return toResponse(branchRepository.save(branch));
    }

    @Override
    public BranchResponse updateBranch(String id, CreateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        String newName = request.getName().trim();
        if (!branch.getName().equals(newName) && branchRepository.existsByNameAndDeletedFalse(newName)) {
            throw new ConflictException("Branch already exists: " + newName);
        }

        branch.setName(newName);
        return toResponse(branchRepository.save(branch));
    }

    @Override
    public void deleteBranch(String id) {
        Branch branch = branchRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        branch.setDeleted(true);
        branchRepository.save(branch);
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .build();
    }
}
