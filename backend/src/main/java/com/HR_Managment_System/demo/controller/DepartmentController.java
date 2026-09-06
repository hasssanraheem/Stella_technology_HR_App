package com.HR_Managment_System.demo.controller;

import com.HR_Managment_System.demo.dto.DepartmentRequest;
import com.HR_Managment_System.demo.dto.DepartmentUpdateRequest;
import com.HR_Managment_System.demo.dto.DepartmentResponse;
import com.HR_Managment_System.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> addDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.addDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(
            @RequestParam(required = false) String name) {

        List<DepartmentResponse> departments = (name != null && !name.isBlank())
                ? departmentService.searchByName(name)
                : departmentService.getAllDepartments();

        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable String departmentId) {
        DepartmentResponse response = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable String departmentId,
            @RequestBody DepartmentUpdateRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(departmentId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{departmentId}/reassign-employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reassignAllEmployees(
            @PathVariable String departmentId,
            @RequestParam String targetDepartmentId) {
        departmentService.reassignAllEmployees(departmentId, targetDepartmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{departmentId}/employee-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getEmployeeCount(@PathVariable String departmentId) {
        return ResponseEntity.ok(departmentService.getEmployeeCount(departmentId));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable String departmentId,
            @RequestParam(required = false) String targetDepartmentId) {
        departmentService.deleteDepartment(departmentId, targetDepartmentId);
        return ResponseEntity.noContent().build();
    }
}
