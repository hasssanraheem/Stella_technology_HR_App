package com.HR_Managment_System.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "leave_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    private String id;

    @Indexed(unique = true)
    private String employeeId;

    @Builder.Default
    private int annualLeaves = 20;

    @Builder.Default
    private int casualLeaves = 10;

    @Builder.Default
    private int sickLeaves = 15;

    @Builder.Default
    private int unpaidLeaves = 30;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
