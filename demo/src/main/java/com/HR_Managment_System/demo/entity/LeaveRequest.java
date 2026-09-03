package com.HR_Managment_System.demo.entity;

import com.HR_Managment_System.demo.enums.LeaveStatus;
import com.HR_Managment_System.demo.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "leave_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    @Id
    private String id;

    @Indexed(unique = true)
    private String leaveId;

    private String employeeId;

    private LeaveType leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    private LocalDateTime appliedAt;

    private LocalDateTime updatedAt;
}
