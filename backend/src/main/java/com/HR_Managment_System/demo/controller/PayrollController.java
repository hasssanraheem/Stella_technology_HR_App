package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.PayrollRequest;
import com.HR_Managment_System.demo.dto.PayrollResponse;
import com.HR_Managment_System.demo.enums.PaymentStatus;
import com.HR_Managment_System.demo.service.EmployeeService;
import com.HR_Managment_System.demo.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<PayrollResponse> generatePayroll(@Valid @RequestBody PayrollRequest request,
                                                            Authentication authentication) {
        String hrDeptId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
        if (!hrDeptId.equals(employeeService.getEmployeeById(request.getEmployeeId()).getDepartmentId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.generatePayroll(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<PayrollResponse>> getAllPayroll(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus status,
            Authentication authentication) {
        boolean isHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        if (isHR) {
            String deptId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
            return ResponseEntity.ok(payrollService.getPayrollByDepartment(deptId, month, year, status));
        }
        return ResponseEntity.ok(payrollService.getAllPayroll(month, year, status));
    }

    @PatchMapping("/{payrollId}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<PayrollResponse> updatePaymentStatus(
            @PathVariable String payrollId,
            @RequestParam PaymentStatus status,
            Authentication authentication) {
        String hrDeptId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
        String targetDeptId = employeeService.getEmployeeById(
                payrollService.getPayroll(payrollId).getEmployeeId()).getDepartmentId();
        if (!hrDeptId.equals(targetDeptId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(payrollService.updatePaymentStatus(payrollId, status));
    }
}
