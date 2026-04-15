package com.uniquest.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB collection: branches
 *
 * Represents an academic branch (e.g., Computer Science, Electrical Engineering).
 * Kept minimal — subjects reference branches by ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "branches")
public class Branch {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private boolean deleted = false;
}
