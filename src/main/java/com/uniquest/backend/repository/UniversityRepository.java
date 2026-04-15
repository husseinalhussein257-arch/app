package com.uniquest.backend.repository;

import com.uniquest.backend.model.University;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository extends MongoRepository<University, String> {

    // ── List queries (soft-delete aware) ───────────────────────────────────

    List<University> findByDeletedFalse();

    Page<University> findByDeletedFalse(Pageable pageable);

    // ── Lookup queries (soft-delete aware) ──────────────────────────────────

    Optional<University> findByIdAndDeletedFalse(String id);

    Optional<University> findByNameAndDeletedFalse(String name);

    // ── Validation ─────────────────────────────────────────────────────────

    boolean existsByIdAndDeletedFalse(String id);

    boolean existsByNameAndDeletedFalse(@NotBlank(message = "University name is required") String name);
}

