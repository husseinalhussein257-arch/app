package com.uniquest.backend.service;

import com.uniquest.backend.dto.admin.CreateBranchRequest;
import com.uniquest.backend.dto.common.BranchResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BranchService {

    List<BranchResponse> getAllBranches();

    Page<BranchResponse> getBranches(int page, int size);

    BranchResponse createBranch(CreateBranchRequest request);

    BranchResponse updateBranch(String id, CreateBranchRequest request);

    void deleteBranch(String id);
}
