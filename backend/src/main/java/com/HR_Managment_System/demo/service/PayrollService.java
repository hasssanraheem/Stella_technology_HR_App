package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.PayrollRequest;
import com.HR_Managment_System.demo.dto.PayrollResponse;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.entity.Payroll;
import com.HR_Managment_System.demo.enums.PaymentStatus;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.exception.PayrollNotFoundException;
import com.HR_Managment_System.demo.exception.ValidationException;
import com.HR_Managment_System.demo.mapper.PayrollMapper;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import com.HR_Managment_System.demo.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollMapper payrollMapper;

    public PayrollResponse generatePayroll(PayrollRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + request.getEmployeeId()));

        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new ValidationException("Payroll already generated for "
                    + request.getEmployeeId() + " for " + request.getMonth() + "/" + request.getYear());
        }

        String payrollId = generatePayrollId();
        Payroll payroll = payrollMapper.toEntity(employee, request.getMonth(), request.getYear(), payrollId);
        Payroll saved = payrollRepository.save(payroll);
        return payrollMapper.toResponse(saved);
    }

    public List<PayrollResponse> getAllPayroll(Integer month, Integer year, PaymentStatus status) {
        List<Payroll> records;

        if (month != null && year != null && status != null) {
            records = payrollRepository.findByMonthAndYearAndPaymentStatus(month, year, status);
        } else if (month != null && year != null) {
            records = payrollRepository.findByMonthAndYear(month, year);
        } else if (month != null && status != null) {
            records = payrollRepository.findByMonthAndPaymentStatus(month, status);
        } else if (year != null && status != null) {
            records = payrollRepository.findByYearAndPaymentStatus(year, status);
        } else if (year != null) {
            records = payrollRepository.findByYear(year);
        } else if (month != null) {
            records = payrollRepository.findByMonth(month);
        } else if (status != null) {
            records = payrollRepository.findByPaymentStatus(status);
        } else {
            records = payrollRepository.findAll();
        }

        return records.stream().map(payrollMapper::toResponse).toList();
    }

    public List<PayrollResponse> getPayrollByEmployee(String employeeId) {
        employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        return payrollRepository.findByEmployeeId(employeeId)
                .stream().map(payrollMapper::toResponse).toList();
    }

    public PayrollResponse getPayroll(String payrollId) {
        Payroll payroll = payrollRepository.findByPayrollId(payrollId)
                .orElseThrow(() -> new PayrollNotFoundException("Payroll not found: " + payrollId));
        return payrollMapper.toResponse(payroll);
    }

    public PayrollResponse updatePaymentStatus(String payrollId, PaymentStatus status) {
        Payroll payroll = payrollRepository.findByPayrollId(payrollId)
                .orElseThrow(() -> new PayrollNotFoundException("Payroll not found: " + payrollId));
        payroll.setPaymentStatus(status);
        payroll.setUpdatedAt(LocalDateTime.now());
        Payroll updated = payrollRepository.save(payroll);
        return payrollMapper.toResponse(updated);
    }


    public List<PayrollResponse> getPayrollByDepartment(String departmentId, Integer month, Integer year, PaymentStatus status) {
        List<String> empIds = employeeRepository.findByDepartmentId(departmentId)
                .stream().map(emp -> emp.getEmployeeId()).toList();
        return payrollRepository.findByEmployeeIdIn(empIds).stream()
                .filter(p -> month == null || p.getMonth() == month)
                .filter(p -> year == null || p.getYear() == year)
                .filter(p -> status == null || p.getPaymentStatus() == status)
                .map(payrollMapper::toResponse)
                .toList();
    }

    private String generatePayrollId() {
        long count = payrollRepository.count() + 1;
        return String.format("PAY-%04d", count);
    }
}
