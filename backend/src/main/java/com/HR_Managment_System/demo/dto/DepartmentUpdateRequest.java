package com.HR_Managment_System.demo.dto;

import lombok.Data;

@Data
public class DepartmentUpdateRequest {
    private String name;
    private String description;
    private String managerId;
    private String hrId;
}
