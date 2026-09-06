package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.EmployeeHistoryResponse;
import com.HR_Managment_System.demo.dto.EmployeeRequest;
import com.HR_Managment_System.demo.dto.EmployeeUpdateRequest;
import com.HR_Managment_System.demo.dto.EmployeeResponse;
import com.HR_Managment_System.demo.dto.LeaveResponse;
import com.HR_Managment_System.demo.dto.PayrollResponse;
import com.HR_Managment_System.demo.dto.PerformanceNoteRequest;
import com.HR_Managment_System.demo.enums.EmployeeStatus;
import com.HR_Managment_System.demo.enums.HistoryType;
import com.HR_Managment_System.demo.service.EmployeeHistoryService;
import com.HR_Managment_System.demo.service.LeaveService;
import com.HR_Managment_System.demo.service.PayrollService;
import com.HR_Managment_System.demo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.HR_Managment_System.demo.enums.EmployeeType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final EmployeeHistoryService employeeHistoryService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<EmployeeResponse> getMyProfile(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponse> addEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        boolean isHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        if (isHR) {
            departmentId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
        }

        Page<EmployeeResponse> employees = employeeService.getAllEmployees(name, departmentId, status, userRole, page, size);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable String employeeId) {
        EmployeeResponse response = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable String employeeId,
            @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{employeeId}/type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployeeType(
            @PathVariable String employeeId,
            @RequestParam EmployeeType employeeType) {
        EmployeeResponse response = employeeService.updateEmployeeType(employeeId, employeeType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employeeId}/leaves")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<List<LeaveResponse>> getEmployeeLeaves(@PathVariable String employeeId) {
        List<LeaveResponse> leaves = leaveService.getLeavesByEmployee(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{employeeId}/payroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<List<PayrollResponse>> getEmployeePayroll(@PathVariable String employeeId) {
        List<PayrollResponse> records = payrollService.getPayrollByEmployee(employeeId);
        return ResponseEntity.ok(records);
    }

    @PostMapping("/{employeeId}/performance-notes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeHistoryResponse> addPerformanceNote(
            @PathVariable String employeeId,
            @Valid @RequestBody PerformanceNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeHistoryService.addPerformanceNote(employeeId, request));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<List<EmployeeHistoryResponse>> getEmployeeHistory(
            @PathVariable String employeeId,
            @RequestParam(required = false) HistoryType type) {
        return ResponseEntity.ok(employeeHistoryService.getHistory(employeeId, type));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }
}
