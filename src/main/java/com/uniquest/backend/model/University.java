package com.uniquest.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB collection: universities
 *
 * Represents an academic institution.
 * Subjects and users reference universities by ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "universities")
public class University {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private boolean deleted = false;
}
