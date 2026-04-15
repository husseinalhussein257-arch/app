package com.uniquest.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB collection: subjects
 *
 * A subject belongs to one year and can span multiple branches.
 *
 * Index strategy:
 *   - yearId indexed individually for "list subjects by year" queries
 *   - Compound index (yearId, branchIds) covers the filtered student query:
 *     GET /subjects?yearId=&branchId=
 *     MongoDB can use $elemMatch on branchIds within the compound index.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subjects")
@CompoundIndex(name = "year_branch_idx", def = "{'yearId': 1, 'branchIds': 1}")
public class Subject {

    @Id
    private String id;

    private String name;

    /** Reference to University._id */
    @Indexed
    private String universityId;

    /** Reference to Year._id */
    @Indexed
    private String yearId;

    /** References to Branch._id — array field, supports $elemMatch queries. */
    private List<String> branchIds;

    private boolean deleted = false;
}
