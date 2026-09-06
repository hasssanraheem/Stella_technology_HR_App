package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.LeaveBalanceResponse;
import com.HR_Managment_System.demo.dto.LeaveRequestDto;
import com.HR_Managment_System.demo.dto.LeaveResponse;
import com.HR_Managment_System.demo.dto.LeaveStatusUpdateDto;
import com.HR_Managment_System.demo.enums.LeaveStatus;
import com.HR_Managment_System.demo.service.EmployeeService;
import com.HR_Managment_System.demo.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<LeaveResponse> applyLeave(
            @Valid @RequestBody LeaveRequestDto dto,
            Authentication authentication) {
        String empId = employeeService.getEmployeeByEmail(authentication.getName()).getEmployeeId();
        dto.setEmployeeId(empId);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(dto));
    }

    @PutMapping("/{leaveId}/status")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable String leaveId,
            @Valid @RequestBody LeaveStatusUpdateDto dto,
            Authentication authentication) {
        boolean isHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        if (isHR) {
            LeaveResponse leave = leaveService.getLeave(leaveId);
            EmployeeResponse requester =
                    employeeService.getEmployeeById(leave.getEmployeeId());
            if ("HR".equals(requester.getUserRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(leaveService.updateLeaveStatus(leaveId, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves(
            @RequestParam(required = false) LeaveStatus status,
            Authentication authentication) {
        boolean isHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        if (isHR) {
            String deptId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
            return ResponseEntity.ok(leaveService.getLeavesByDepartment(deptId, status));
        }
        return ResponseEntity.ok(leaveService.getAllLeaves(status));
    }

    @GetMapping("/balance/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(@PathVariable String employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveBalanceResponse>> getAllLeaveBalances(
            @RequestParam(required = false) String name,
            Authentication authentication) {
        boolean isHR = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        if (isHR) {
            String deptId = employeeService.getEmployeeByEmail(authentication.getName()).getDepartmentId();
            return ResponseEntity.ok(leaveService.getLeaveBalancesByDepartment(deptId));
        }
        return ResponseEntity.ok(leaveService.getAllLeaveBalances(name));
    }
}
