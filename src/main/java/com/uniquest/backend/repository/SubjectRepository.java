package com.uniquest.backend.repository;

import com.uniquest.backend.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubjectRepository extends MongoRepository<Subject, String> {

    // ── Student list queries (soft-delete aware) ──────────────────────────────

    List<Subject> findByUniversityIdAndYearIdAndBranchIdsContainingAndDeletedFalse(String universityId, String yearId, String branchId);

    List<Subject> findByUniversityIdAndYearIdAndDeletedFalse(String universityId, String yearId);

    List<Subject> findByUniversityIdAndBranchIdsContainingAndDeletedFalse(String universityId, String branchId);

    List<Subject> findByUniversityIdAndDeletedFalse(String universityId);

    List<Subject> findByYearIdAndBranchIdsContainingAndDeletedFalse(String yearId, String branchId);

    List<Subject> findByYearIdAndDeletedFalse(String yearId);

    List<Subject> findByBranchIdsContainingAndDeletedFalse(String branchId);

    List<Subject> findByDeletedFalse();

    // ── Paginated admin queries (soft-delete aware) ───────────────────────────

    Page<Subject> findByUniversityIdAndYearIdAndBranchIdsContainingAndDeletedFalse(String universityId, String yearId, String branchId, Pageable pageable);

    Page<Subject> findByUniversityIdAndYearIdAndDeletedFalse(String universityId, String yearId, Pageable pageable);

    Page<Subject> findByUniversityIdAndBranchIdsContainingAndDeletedFalse(String universityId, String branchId, Pageable pageable);

    Page<Subject> findByUniversityIdAndDeletedFalse(String universityId, Pageable pageable);

    Page<Subject> findByYearIdAndBranchIdsContainingAndDeletedFalse(String yearId, String branchId, Pageable pageable);

    Page<Subject> findByYearIdAndDeletedFalse(String yearId, Pageable pageable);

    Page<Subject> findByBranchIdsContainingAndDeletedFalse(String branchId, Pageable pageable);

    Page<Subject> findByDeletedFalse(Pageable pageable);

    // ── Validation ────────────────────────────────────────────────────────────

    boolean existsByIdAndDeletedFalse(String id);

    boolean existsByIdAndUniversityIdAndDeletedFalse(String id, String universityId);
}
