package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.Payroll;
import com.HR_Managment_System.demo.enums.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends MongoRepository<Payroll, String> {

    Optional<Payroll> findByPayrollId(String payrollId);

    boolean existsByEmployeeIdAndMonthAndYear(String employeeId, int month, int year);

    List<Payroll> findByEmployeeId(String employeeId);

    List<Payroll> findByMonthAndYear(int month, int year);

    List<Payroll> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Payroll> findByMonthAndYearAndPaymentStatus(int month, int year, PaymentStatus paymentStatus);
}
