package com.uniquest.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateYearRequest {

    @NotBlank(message = "Year name is required")
    private String name;
}
