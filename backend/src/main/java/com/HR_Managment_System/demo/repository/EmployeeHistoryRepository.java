package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.EmployeeHistory;
import com.HR_Managment_System.demo.enums.HistoryType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeHistoryRepository extends MongoRepository<EmployeeHistory, String> {
    List<EmployeeHistory> findByEmployeeId(String employeeId);
    List<EmployeeHistory> findByEmployeeIdAndType(String employeeId, HistoryType type);
    Optional<EmployeeHistory> findByHistoryId(String historyId);
}
