package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.LeaveRequest;
import com.HR_Managment_System.demo.enums.LeaveStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {

    Optional<LeaveRequest> findByLeaveId(String leaveId);

    List<LeaveRequest> findByEmployeeId(String employeeId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findByEmployeeIdAndStatus(String employeeId, LeaveStatus status);
}
