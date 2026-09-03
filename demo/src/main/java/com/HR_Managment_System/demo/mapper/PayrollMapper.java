package com.HR_Managment_System.demo.mapper;

import com.HR_Managment_System.demo.dto.PayrollResponse;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.entity.Payroll;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PayrollMapper {

    public Payroll toEntity(Employee employee, int month, int year, String payrollId) {
        double basicSalary = employee.getSalary();
        double allowances  = employee.getAllowances() != null ? employee.getAllowances() : 0.0;
        double deductions  = employee.getDeductions() != null ? employee.getDeductions() : 0.0;
        double netSalary   = basicSalary + allowances - deductions;

        return Payroll.builder()
                .payrollId(payrollId)
                .employeeId(employee.getEmployeeId())
                .month(month)
                .year(year)
                .basicSalary(basicSalary)
                .allowances(allowances)
                .deductions(deductions)
                .netSalary(netSalary)
                .generatedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PayrollResponse toResponse(Payroll payroll) {
        return PayrollResponse.builder()
                .payrollId(payroll.getPayrollId())
                .employeeId(payroll.getEmployeeId())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .basicSalary(payroll.getBasicSalary())
                .allowances(payroll.getAllowances())
                .deductions(payroll.getDeductions())
                .netSalary(payroll.getNetSalary())
                .paymentStatus(payroll.getPaymentStatus())
                .generatedAt(payroll.getGeneratedAt())
                .updatedAt(payroll.getUpdatedAt())
                .build();
    }
}
