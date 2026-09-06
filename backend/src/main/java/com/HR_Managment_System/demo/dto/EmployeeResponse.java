package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.EmployeeStatus;
import com.HR_Managment_System.demo.enums.EmployeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private String employeeId;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String departmentId;
    private String designation;
    private LocalDate dateOfJoining;
    private Double salary;
    private Double allowances;
    private Double deductions;
    private EmployeeStatus status;
    private EmployeeType employeeType;
    private String userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
