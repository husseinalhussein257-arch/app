package com.uniquest.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "Subject name is required")
    private String name;

    @NotBlank(message = "universityId is required")
    private String universityId;

    @NotBlank(message = "yearId is required")
    private String yearId;

    @NotEmpty(message = "At least one branchId is required")
    private List<String> branchIds;
}
