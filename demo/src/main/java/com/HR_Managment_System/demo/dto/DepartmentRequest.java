package com.HR_Managment_System.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;

    private String managerId;

    @NotBlank(message = "HR assignment is required for each department")
    private String hrId;
}
