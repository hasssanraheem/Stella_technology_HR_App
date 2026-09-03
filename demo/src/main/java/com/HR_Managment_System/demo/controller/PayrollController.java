package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.PayrollRequest;
import com.HR_Managment_System.demo.dto.PayrollResponse;
import com.HR_Managment_System.demo.enums.PaymentStatus;
import com.HR_Managment_System.demo.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> generatePayroll(@Valid @RequestBody PayrollRequest request) {
        PayrollResponse response = payrollService.generatePayroll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<PayrollResponse>> getAllPayroll(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus status) {
        List<PayrollResponse> records = payrollService.getAllPayroll(month, year, status);
        return ResponseEntity.ok(records);
    }

    @PatchMapping("/{payrollId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> updatePaymentStatus(
            @PathVariable String payrollId,
            @RequestParam PaymentStatus status) {
        PayrollResponse response = payrollService.updatePaymentStatus(payrollId, status);
        return ResponseEntity.ok(response);
    }
}
