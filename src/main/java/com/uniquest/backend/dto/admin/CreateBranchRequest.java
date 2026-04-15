package com.uniquest.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;
}
