package com.uniquest.backend.repository;

import com.uniquest.backend.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BranchRepository extends MongoRepository<Branch, String> {

    boolean existsByName(String name);

    // ── Soft-delete aware ─────────────────────────────────────────────────────

    List<Branch> findByDeletedFalse();

    Page<Branch> findByDeletedFalse(Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByIdAndDeletedFalse(String id);
}
