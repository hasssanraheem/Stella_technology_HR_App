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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock PayrollRepository payrollRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock PayrollMapper payrollMapper;

    @InjectMocks PayrollService payrollService;

    @Test
    void generatePayroll_happyPath_returnsPayrollResponse() {
        PayrollRequest request = new PayrollRequest();
        request.setEmployeeId("EMP-0001");
        request.setMonth(9);
        request.setYear(2026);

        Employee employee = new Employee();
        employee.setEmployeeId("EMP-0001");
        Payroll payroll = new Payroll();
        PayrollResponse expected = new PayrollResponse();

        when(employeeRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonthAndYear("EMP-0001", 9, 2026)).thenReturn(false);
        when(payrollRepository.count()).thenReturn(0L);
        when(payrollMapper.toEntity(any(Employee.class), anyInt(), anyInt(), anyString())).thenReturn(payroll);
        when(payrollRepository.save(payroll)).thenReturn(payroll);
        when(payrollMapper.toResponse(payroll)).thenReturn(expected);

        PayrollResponse result = payrollService.generatePayroll(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void generatePayroll_employeeNotFound_throwsEmployeeNotFoundException() {
        PayrollRequest request = new PayrollRequest();
        request.setEmployeeId("GHOST-99");
        request.setMonth(9);
        request.setYear(2026);

        when(employeeRepository.findByEmployeeId("GHOST-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.generatePayroll(request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("GHOST-99");
    }

    @Test
    void generatePayroll_duplicate_throwsValidationException() {
        PayrollRequest request = new PayrollRequest();
        request.setEmployeeId("EMP-0001");
        request.setMonth(9);
        request.setYear(2026);

        when(employeeRepository.findByEmployeeId("EMP-0001"))
                .thenReturn(Optional.of(new Employee()));
        when(payrollRepository.existsByEmployeeIdAndMonthAndYear("EMP-0001", 9, 2026)).thenReturn(true);

        assertThatThrownBy(() -> payrollService.generatePayroll(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already generated");
    }

    @Test
    void getPayroll_found_returnsResponse() {
        Payroll payroll = new Payroll();
        PayrollResponse expected = new PayrollResponse();

        when(payrollRepository.findByPayrollId("PAY-0001")).thenReturn(Optional.of(payroll));
        when(payrollMapper.toResponse(payroll)).thenReturn(expected);

        PayrollResponse result = payrollService.getPayroll("PAY-0001");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPayroll_notFound_throwsPayrollNotFoundException() {
        when(payrollRepository.findByPayrollId("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getPayroll("GHOST"))
                .isInstanceOf(PayrollNotFoundException.class)
                .hasMessageContaining("GHOST");
    }

    @Test
    void updatePaymentStatus_happyPath_returnsUpdatedResponse() {
        Payroll payroll = new Payroll();
        payroll.setPaymentStatus(PaymentStatus.UNPAID);
        PayrollResponse expected = new PayrollResponse();

        when(payrollRepository.findByPayrollId("PAY-0001")).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(payroll)).thenReturn(payroll);
        when(payrollMapper.toResponse(payroll)).thenReturn(expected);

        PayrollResponse result = payrollService.updatePaymentStatus("PAY-0001", PaymentStatus.PAID);

        assertThat(result).isEqualTo(expected);
        assertThat(payroll.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void updatePaymentStatus_notFound_throwsPayrollNotFoundException() {
        when(payrollRepository.findByPayrollId("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.updatePaymentStatus("GHOST", PaymentStatus.PAID))
                .isInstanceOf(PayrollNotFoundException.class);
    }
}
