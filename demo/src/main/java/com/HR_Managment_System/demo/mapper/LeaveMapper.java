package com.HR_Managment_System.demo.mapper;

import com.HR_Managment_System.demo.dto.LeaveBalanceResponse;
import com.HR_Managment_System.demo.dto.LeaveRequestDto;
import com.HR_Managment_System.demo.dto.LeaveResponse;
import com.HR_Managment_System.demo.entity.LeaveBalance;
import com.HR_Managment_System.demo.entity.LeaveRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class LeaveMapper {

    public LeaveRequest toEntity(LeaveRequestDto dto, String leaveId) {
        return LeaveRequest.builder()
                .leaveId(leaveId)
                .employeeId(dto.getEmployeeId())
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .appliedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public LeaveResponse toResponse(LeaveRequest leaveRequest) {
        int totalDays = (int) ChronoUnit.DAYS.between(
                leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        return LeaveResponse.builder()
                .leaveId(leaveRequest.getLeaveId())
                .employeeId(leaveRequest.getEmployeeId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .totalDays(totalDays)
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .appliedAt(leaveRequest.getAppliedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    public LeaveBalanceResponse toBalanceResponse(LeaveBalance balance) {
        return LeaveBalanceResponse.builder()
                .employeeId(balance.getEmployeeId())
                .annualLeaves(balance.getAnnualLeaves())
                .casualLeaves(balance.getCasualLeaves())
                .sickLeaves(balance.getSickLeaves())
                .unpaidLeaves(balance.getUnpaidLeaves())
                .build();
    }
}
