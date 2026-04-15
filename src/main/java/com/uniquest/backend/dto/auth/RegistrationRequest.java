package com.uniquest.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Student registration request.
 *
 * Role is always STUDENT for public registration — no user input.
 * Admin accounts are created separately by administrators.
 */
@Data
public class RegistrationRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "University ID is required")
    private String universityId;

    @NotBlank(message = "Branch ID is required")
    private String branchId;
}
