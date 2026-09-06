package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.DepartmentRequest;
import com.HR_Managment_System.demo.dto.DepartmentUpdateRequest;
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
        validateHrNotAssigned(request.getHrId(), null);

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

    public DepartmentResponse updateDepartment(String departmentId, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));

        if (request.getName() != null
                && !department.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateEmailException("Department name already in use: " + request.getName());
        }

        validateManager(request.getManagerId());
        validateHrNotAssigned(request.getHrId(), departmentId);

        departmentMapper.updateEntity(department, request);
        Department updated = departmentRepository.save(department);
        return departmentMapper.toResponse(updated);
    }

    public void reassignAllEmployees(String fromDeptId, String toDeptId) {
        departmentRepository.findByDepartmentId(fromDeptId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + fromDeptId));
        departmentRepository.findByDepartmentId(toDeptId)
                .orElseThrow(() -> new DepartmentNotFoundException("Target department not found: " + toDeptId));

        java.util.List<com.HR_Managment_System.demo.entity.Employee> employees =
                employeeRepository.findByDepartmentId(fromDeptId);
        for (com.HR_Managment_System.demo.entity.Employee emp : employees) {
            emp.setDepartmentId(toDeptId);
        }
        employeeRepository.saveAll(employees);
    }

    public long getEmployeeCount(String departmentId) {
        return employeeRepository.countByDepartmentId(departmentId);
    }

    public void deleteDepartment(String departmentId, String targetDepartmentId) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));

        if (department.isSystem()) {
            throw new ValidationException("The Unassigned department is system-reserved and cannot be deleted.");
        }

        long count = employeeRepository.countByDepartmentId(departmentId);
        if (count > 0) {
            if (targetDepartmentId == null || targetDepartmentId.isBlank()) {
                throw new ValidationException(count + " employee(s) are still assigned to this department. Provide a target department to move them.");
            }
            departmentRepository.findByDepartmentId(targetDepartmentId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Target department not found: " + targetDepartmentId));

            java.util.List<com.HR_Managment_System.demo.entity.Employee> employees =
                    employeeRepository.findByDepartmentId(departmentId);
            for (com.HR_Managment_System.demo.entity.Employee emp : employees) {
                emp.setDepartmentId(targetDepartmentId);
            }
            employeeRepository.saveAll(employees);
        }

        departmentRepository.delete(department);
    }

    private void validateHrNotAssigned(String hrId, String currentDeptId) {
        if (hrId == null || hrId.isBlank()) return;
        departmentRepository.findByHrId(hrId).ifPresent(existing -> {
            if (currentDeptId == null || !existing.getDepartmentId().equals(currentDeptId)) {
                throw new ValidationException("HR already assigned to department: " + existing.getName());
            }
        });

        // Validate the employee exists and has HR role
        com.HR_Managment_System.demo.entity.Employee hr = employeeRepository.findByEmployeeId(hrId)
                .orElseThrow(() -> new com.HR_Managment_System.demo.exception.EmployeeNotFoundException("Employee not found with ID: " + hrId));
        if (!"HR".equals(hr.getUserRole())) {
            throw new ValidationException("Employee " + hrId + " does not have the HR role");
        }
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
        return departmentRepository.findTopByOrderByDepartmentIdDesc()
                .map(d -> {
                    int num = Integer.parseInt(d.getDepartmentId().replace("DEPT-", ""));
                    return String.format("DEPT-%04d", num + 1);
                })
                .orElse("DEPT-0001");
    }
}
