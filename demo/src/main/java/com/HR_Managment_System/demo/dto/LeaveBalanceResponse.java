package com.HR_Managment_System.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResponse {

    private String employeeId;
    private int annualLeaves;
    private int casualLeaves;
    private int sickLeaves;
    private int unpaidLeaves;
}
