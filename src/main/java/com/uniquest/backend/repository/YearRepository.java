package com.uniquest.backend.repository;

import com.uniquest.backend.model.Year;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface YearRepository extends MongoRepository<Year, String> {

    boolean existsByName(String name);

    // ── Soft-delete aware ─────────────────────────────────────────────────────

    List<Year> findByDeletedFalse();

    Page<Year> findByDeletedFalse(Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByIdAndDeletedFalse(String id);
}
