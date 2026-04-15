package com.uniquest.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUniversityRequest {

    @NotBlank(message = "University name is required")
    private String name;
}
