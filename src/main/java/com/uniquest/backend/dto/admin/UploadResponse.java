package com.uniquest.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of a CSV / Excel bulk question upload.
 *
 * 'errors' contains row-level messages so the admin knows exactly
 * which rows failed and why (e.g., "Row 5: missing correctAnswer").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private int total;
    private int success;
    private int failed;

    /** Human-readable error per failed row */
    private List<String> errors;
}
