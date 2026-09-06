package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.EmployeeType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeUpdateRequest {
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
    private EmployeeType employeeType;
    private String userRole;
}
