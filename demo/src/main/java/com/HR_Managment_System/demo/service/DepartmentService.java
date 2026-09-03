package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.DepartmentRequest;
import com.HR_Managment_System.demo.dto.DepartmentResponse;
import com.HR_Managment_System.demo.entity.Department;
import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.enums.EmployeeType;
import com.HR_Managment_System.demo.exception.DepartmentNotFoundException;
import com.HR_Managment_System.demo.exception.DuplicateEmailException;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.exception.ValidationException;
import com.HR_Managment_System.demo.mapper.DepartmentMapper;
import com.HR_Managment_System.demo.repository.DepartmentRepository;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentResponse addDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateEmailException("Department already exists with name: " + request.getName());
        }

        validateManager(request.getManagerId());

        String departmentId = generateDepartmentId();
        Department department = departmentMapper.toEntity(request, departmentId);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    public DepartmentResponse getDepartmentById(String departmentId) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));
        return departmentMapper.toResponse(department);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    public List<DepartmentResponse> searchByName(String name) {
        return departmentRepository.searchByName(name)
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    public DepartmentResponse updateDepartment(String departmentId, DepartmentRequest request) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));

        if (!department.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateEmailException("Department name already in use: " + request.getName());
        }

        validateManager(request.getManagerId());

        departmentMapper.updateEntity(department, request);
        Department updated = departmentRepository.save(department);
        return departmentMapper.toResponse(updated);
    }

    public void deleteDepartment(String departmentId) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));
        departmentRepository.delete(department);
    }

    private void validateManager(String managerId) {
        if (managerId == null || managerId.isBlank()) return;

        Employee manager = employeeRepository.findByEmployeeId(managerId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + managerId));

        if (manager.getEmployeeType() != EmployeeType.MANAGER) {
            throw new ValidationException("Employee " + managerId + " is not designated as a MANAGER");
        }
    }

    private String generateDepartmentId() {
        long count = departmentRepository.count() + 1;
        return String.format("DEPT-%04d", count);
    }
}
