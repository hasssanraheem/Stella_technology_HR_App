package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {

    private String payrollId;
    private String employeeId;
    private int month;
    private int year;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double netSalary;
    private PaymentStatus paymentStatus;
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;
}
