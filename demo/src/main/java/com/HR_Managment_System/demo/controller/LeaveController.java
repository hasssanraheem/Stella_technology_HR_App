package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.LeaveBalanceResponse;
import com.HR_Managment_System.demo.dto.LeaveRequestDto;
import com.HR_Managment_System.demo.dto.LeaveResponse;
import com.HR_Managment_System.demo.dto.LeaveStatusUpdateDto;
import com.HR_Managment_System.demo.enums.LeaveStatus;
import com.HR_Managment_System.demo.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<LeaveResponse> applyLeave(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveResponse response = leaveService.applyLeave(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{leaveId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable String leaveId,
            @Valid @RequestBody LeaveStatusUpdateDto dto) {
        LeaveResponse response = leaveService.updateLeaveStatus(leaveId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves(
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveResponse> leaves = leaveService.getAllLeaves(status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/balance/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(@PathVariable String employeeId) {
        LeaveBalanceResponse balance = leaveService.getLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveBalanceResponse>> getAllLeaveBalances(
            @RequestParam(required = false) String name) {
        List<LeaveBalanceResponse> balances = leaveService.getAllLeaveBalances(name);
        return ResponseEntity.ok(balances);
    }
}
