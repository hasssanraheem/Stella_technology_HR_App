package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.EmployeeRequest;
import com.HR_Managment_System.demo.dto.EmployeeResponse;
import com.HR_Managment_System.demo.dto.EmployeeUpdateRequest;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.entity.LeaveBalance;
import com.HR_Managment_System.demo.exception.DuplicateEmailException;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.mapper.EmployeeMapper;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import com.HR_Managment_System.demo.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock EmployeeMapper employeeMapper;
    @Mock LeaveBalanceRepository leaveBalanceRepository;
    @Mock EmployeeHistoryService employeeHistoryService;

    @InjectMocks EmployeeService employeeService;

    @Test
    void addEmployee_happyPath_returnsResponse() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("ali@company.com");

        Employee entity = new Employee();
        entity.setEmployeeId("EMP-0001");

        EmployeeResponse expected = new EmployeeResponse();
        expected.setEmployeeId("EMP-0001");

        when(employeeRepository.existsByEmail("ali@company.com")).thenReturn(false);
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeMapper.toEntity(any(), anyString())).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(entity);
        when(employeeMapper.toResponse(entity)).thenReturn(expected);

        EmployeeResponse result = employeeService.addEmployee(request);

        assertThat(result.getEmployeeId()).isEqualTo("EMP-0001");
        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
    }

    @Test
    void addEmployee_duplicateEmail_throwsDuplicateEmailException() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("exists@company.com");

        when(employeeRepository.existsByEmail("exists@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.addEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("exists@company.com");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_found_returnsResponse() {
        Employee employee = new Employee();
        employee.setEmployeeId("EMP-0001");
        EmployeeResponse expected = new EmployeeResponse();

        when(employeeRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(expected);

        EmployeeResponse result = employeeService.getEmployeeById("EMP-0001");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getEmployeeById_notFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findByEmployeeId("GHOST-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById("GHOST-99"))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("GHOST-99");
    }

    @Test
    void updateEmployee_happyPath_returnsUpdatedResponse() {
        Employee employee = new Employee();
        employee.setEmployeeId("EMP-0001");
        employee.setEmail("old@company.com");
        employee.setDesignation("Engineer");

        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setDesignation("Senior Engineer");

        EmployeeResponse expected = new EmployeeResponse();

        when(employeeRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(expected);

        EmployeeResponse result = employeeService.updateEmployee("EMP-0001", request);

        assertThat(result).isEqualTo(expected);
        verify(employeeHistoryService).recordDesignationChange("EMP-0001", "Engineer", "Senior Engineer");
    }

    @Test
    void updateEmployee_notFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findByEmployeeId("GHOST-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee("GHOST-99", new EmployeeUpdateRequest()))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void deleteEmployee_happyPath_callsRepositoryDelete() {
        Employee employee = new Employee();
        when(employeeRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee("EMP-0001");

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_notFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findByEmployeeId("GHOST-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee("GHOST-99"))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
