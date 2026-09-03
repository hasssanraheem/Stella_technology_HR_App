package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.EmployeeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must contain @ and a valid domain")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    private LocalDate dateOfBirth;

    private String address;

    @NotBlank(message = "Department ID is required")
    private String departmentId;

    @NotBlank(message = "Designation is required")
    private String designation;

    private LocalDate dateOfJoining;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be a positive number")
    private Double salary;

    private Double allowances = 0.0;

    private Double deductions = 0.0;

    private EmployeeType employeeType;
}
