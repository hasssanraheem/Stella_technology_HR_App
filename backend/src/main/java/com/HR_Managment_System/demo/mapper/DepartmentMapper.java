package com.HR_Managment_System.demo.mapper;

import com.HR_Managment_System.demo.dto.DepartmentRequest;
import com.HR_Managment_System.demo.dto.DepartmentUpdateRequest;
import com.HR_Managment_System.demo.dto.DepartmentResponse;
import com.HR_Managment_System.demo.entity.Department;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request, String departmentId) {
        return Department.builder()
                .departmentId(departmentId)
                .name(request.getName())
                .description(request.getDescription())
                .managerId(request.getManagerId())
                .hrId(request.getHrId())
                .isSystem(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManagerId())
                .hrId(department.getHrId())
                .isSystem(department.isSystem())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    public void updateEntity(Department department, DepartmentRequest request) {
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setManagerId(request.getManagerId());
        department.setHrId(request.getHrId());
        department.setUpdatedAt(LocalDateTime.now());
    }

    public void updateEntity(Department department, DepartmentUpdateRequest request) {
        if (request.getName() != null)        department.setName(request.getName());
        if (request.getDescription() != null) department.setDescription(request.getDescription());
        if (request.getManagerId() != null)   department.setManagerId(request.getManagerId());
        if (request.getHrId() != null)        department.setHrId(request.getHrId());
        department.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
