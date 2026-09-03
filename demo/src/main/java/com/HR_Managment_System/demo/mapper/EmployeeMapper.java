package com.HR_Managment_System.demo.mapper;

import com.HR_Managment_System.demo.dto.EmployeeRequest;
import com.HR_Managment_System.demo.dto.EmployeeResponse;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.enums.EmployeeType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request, String employeeId) {
        return Employee.builder()
                .employeeId(employeeId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .departmentId(request.getDepartmentId())
                .designation(request.getDesignation())
                .dateOfJoining(request.getDateOfJoining())
                .salary(request.getSalary())
                .allowances(request.getAllowances() != null ? request.getAllowances() : 0.0)
                .deductions(request.getDeductions() != null ? request.getDeductions() : 0.0)
                .employeeType(request.getEmployeeType() != null ? request.getEmployeeType() : EmployeeType.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .gender(employee.getGender())
                .dateOfBirth(employee.getDateOfBirth())
                .address(employee.getAddress())
                .departmentId(employee.getDepartmentId())
                .designation(employee.getDesignation())
                .dateOfJoining(employee.getDateOfJoining())
                .salary(employee.getSalary())
                .allowances(employee.getAllowances())
                .deductions(employee.getDeductions())
                .status(employee.getStatus())
                .employeeType(employee.getEmployeeType())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public void updateEntity(Employee employee, EmployeeRequest request) {
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setAddress(request.getAddress());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setSalary(request.getSalary());
        if (request.getAllowances() != null) employee.setAllowances(request.getAllowances());
        if (request.getDeductions() != null) employee.setDeductions(request.getDeductions());
        if (request.getEmployeeType() != null) {
            employee.setEmployeeType(request.getEmployeeType());
        }
        employee.setUpdatedAt(LocalDateTime.now());
    }
}
