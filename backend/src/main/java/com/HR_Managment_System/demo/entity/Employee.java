package com.HR_Managment_System.demo.entity;

import com.HR_Managment_System.demo.enums.EmployeeStatus;
import com.HR_Managment_System.demo.enums.EmployeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    private String id;

    @Indexed(unique = true)
    private String employeeId;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String gender;

    private LocalDate dateOfBirth;

    private String address;

    private String departmentId;

    private String designation;

    private LocalDate dateOfJoining;

    private Double salary;

    @Builder.Default
    private Double allowances = 0.0;

    @Builder.Default
    private Double deductions = 0.0;

    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    private String userRole;

    @Builder.Default
    private EmployeeType employeeType = EmployeeType.EMPLOYEE;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
