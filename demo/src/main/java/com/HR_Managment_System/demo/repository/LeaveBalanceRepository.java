package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.LeaveBalance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LeaveBalanceRepository extends MongoRepository<LeaveBalance, String> {

    Optional<LeaveBalance> findByEmployeeId(String employeeId);
}
