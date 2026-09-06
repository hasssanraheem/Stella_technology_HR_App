package com.HR_Managment_System.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistoryResponse {
    private String historyId;
    private String employeeId;
    private String type;
    private String previousValue;
    private String newValue;
    private String note;
    private String recordedBy;
    private LocalDateTime recordedAt;
}
