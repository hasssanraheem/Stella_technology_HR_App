package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.LeaveStatus;
import com.HR_Managment_System.demo.enums.LeaveType;
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
public class LeaveResponse {

    private String leaveId;
    private String employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private String reason;
    private LeaveStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
