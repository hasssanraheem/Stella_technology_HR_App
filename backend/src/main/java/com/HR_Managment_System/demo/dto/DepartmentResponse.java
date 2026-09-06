package com.HR_Managment_System.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private String departmentId;
    private String name;
    private String description;
    private String managerId;
    private String hrId;
    @JsonProperty("isSystem")
    private boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
