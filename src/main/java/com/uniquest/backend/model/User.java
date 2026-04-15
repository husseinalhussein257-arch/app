package com.uniquest.backend.model;

import com.uniquest.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB collection: users
 *
 * Stores both admins and students. Role field controls access.
 * Password is always stored BCrypt-hashed — never plaintext.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    /** Must be unique across all users */
    @Indexed(unique = true)
    private String username;

    /** BCrypt-hashed password */
    private String password;

    private Role role;

    /** Reference to University._id — optional for ADMIN, required for STUDENT */
    @Indexed
    private String universityId;

    /** Reference to Branch._id — optional for ADMIN, required for STUDENT */
    @Indexed
    private String branchId;
}
