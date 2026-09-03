package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.EmployeeRequest;
import com.HR_Managment_System.demo.dto.EmployeeResponse;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.entity.LeaveBalance;
import com.HR_Managment_System.demo.enums.EmployeeStatus;
import com.HR_Managment_System.demo.enums.EmployeeType;
import com.HR_Managment_System.demo.exception.DuplicateEmailException;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.mapper.EmployeeMapper;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import com.HR_Managment_System.demo.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeHistoryService employeeHistoryService;

    public EmployeeResponse addEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Employee with email already exists: " + request.getEmail());
        }

        String employeeId = generateEmployeeId();
        Employee employee = employeeMapper.toEntity(request, employeeId);
        Employee saved = employeeRepository.save(employee);

        LeaveBalance leaveBalance = LeaveBalance.builder()
                .employeeId(saved.getEmployeeId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        leaveBalanceRepository.save(leaveBalance);

        return employeeMapper.toResponse(saved);
    }

    public EmployeeResponse getEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));
        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
        return employeeMapper.toResponse(employee);
    }

    public Page<EmployeeResponse> getAllEmployees(String name, String departmentId,
                                                   EmployeeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        String nameFilter = (name != null) ? name : "";
        String deptFilter = (departmentId != null) ? departmentId : "";

        return employeeRepository
                .findByFilters(nameFilter, deptFilter, status, pageable)
                .map(employeeMapper::toResponse);
    }

    public EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already in use: " + request.getEmail());
        }

        String previousDesignation = employee.getDesignation();
        String newDesignation = request.getDesignation();

        employeeMapper.updateEntity(employee, request);
        Employee updated = employeeRepository.save(employee);

        if (newDesignation != null && !newDesignation.equals(previousDesignation)) {
            employeeHistoryService.recordDesignationChange(employeeId, previousDesignation, newDesignation);
        }

        return employeeMapper.toResponse(updated);
    }

    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));
        employeeRepository.delete(employee);
    }

    public EmployeeResponse updateEmployeeType(String employeeId, EmployeeType employeeType) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        String previousType = employee.getEmployeeType().name();

        employee.setEmployeeType(employeeType);
        employee.setUpdatedAt(LocalDateTime.now());
        Employee updated = employeeRepository.save(employee);

        if (!previousType.equals(employeeType.name())) {
            employeeHistoryService.recordPromotion(employeeId, previousType, employeeType.name());
        }

        return employeeMapper.toResponse(updated);
    }

    private String generateEmployeeId() {
        long count = employeeRepository.count() + 1;
        return String.format("EMP-%04d", count);
    }
}
