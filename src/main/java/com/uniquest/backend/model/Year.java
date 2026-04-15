package com.uniquest.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB collection: years
 *
 * Academic year (e.g., "Year 1", "Year 2", "Year 3", "Year 4").
 * Subjects are linked to a year — students filter by year + branch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "years")
public class Year {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private boolean deleted = false;
}
