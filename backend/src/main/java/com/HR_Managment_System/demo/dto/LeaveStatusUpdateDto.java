package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveStatusUpdateDto {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private LeaveStatus status;
}
