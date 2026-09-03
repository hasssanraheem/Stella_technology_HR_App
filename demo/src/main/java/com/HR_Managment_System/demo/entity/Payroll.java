package com.HR_Managment_System.demo.entity;

import com.HR_Managment_System.demo.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payroll")
@CompoundIndex(def = "{'employeeId': 1, 'month': 1, 'year': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    private String id;

    @Indexed(unique = true)
    private String payrollId;

    private String employeeId;

    private int month;

    private int year;

    private Double basicSalary;

    private Double allowances;

    private Double deductions;

    private Double netSalary;

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDateTime generatedAt;

    private LocalDateTime updatedAt;
}
