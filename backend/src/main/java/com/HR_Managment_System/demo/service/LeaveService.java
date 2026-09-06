package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.LeaveBalanceResponse;
import com.HR_Managment_System.demo.dto.LeaveRequestDto;
import com.HR_Managment_System.demo.dto.LeaveResponse;
import com.HR_Managment_System.demo.dto.LeaveStatusUpdateDto;
import com.HR_Managment_System.demo.entity.LeaveBalance;
import com.HR_Managment_System.demo.entity.LeaveRequest;
import com.HR_Managment_System.demo.enums.LeaveStatus;
import com.HR_Managment_System.demo.enums.LeaveType;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.exception.LeaveRequestNotFoundException;
import com.HR_Managment_System.demo.exception.ValidationException;
import com.HR_Managment_System.demo.mapper.LeaveMapper;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import com.HR_Managment_System.demo.repository.LeaveBalanceRepository;
import com.HR_Managment_System.demo.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    public LeaveResponse applyLeave(LeaveRequestDto dto) {
        employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + dto.getEmployeeId()));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new ValidationException("End date cannot be before start date");
        }

        String leaveId = generateLeaveId();
        LeaveRequest leaveRequest = leaveMapper.toEntity(dto, leaveId);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toResponse(saved);
    }

    public LeaveResponse getLeave(String leaveId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByLeaveId(leaveId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found: " + leaveId));
        return leaveMapper.toResponse(leaveRequest);
    }

    public LeaveResponse updateLeaveStatus(String leaveId, LeaveStatusUpdateDto dto) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByLeaveId(leaveId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found: " + leaveId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new ValidationException("Only PENDING leave requests can be updated");
        }

        if (dto.getStatus() == LeaveStatus.APPROVED) {
            int days = (int) ChronoUnit.DAYS.between(
                    leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            deductLeaveBalance(leaveRequest.getEmployeeId(), leaveRequest.getLeaveType(), days);
        }

        leaveRequest.setStatus(dto.getStatus());
        leaveRequest.setUpdatedAt(LocalDateTime.now());
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toResponse(updated);
    }

    public List<LeaveResponse> getLeavesByEmployee(String employeeId) {
        employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        return leaveRequestRepository.findByEmployeeId(employeeId)
                .stream().map(leaveMapper::toResponse).toList();
    }

    public List<LeaveResponse> getAllLeaves(LeaveStatus status) {
        List<LeaveRequest> requests = (status != null)
                ? leaveRequestRepository.findByStatus(status)
                : leaveRequestRepository.findAll();
        return requests.stream().map(leaveMapper::toResponse).toList();
    }

    public LeaveBalanceResponse getLeaveBalance(String employeeId) {
        employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ValidationException("Leave balance not found for employee: " + employeeId));
        return leaveMapper.toBalanceResponse(balance);
    }

    public List<LeaveBalanceResponse> getAllLeaveBalances(String name) {
        if (name != null && !name.isBlank()) {
            return employeeRepository.findByFilters(name, "", null, "", PageRequest.of(0, 100))
                    .stream()
                    .map(emp -> leaveBalanceRepository.findByEmployeeId(emp.getEmployeeId())
                            .map(leaveMapper::toBalanceResponse)
                            .orElse(null))
                    .filter(b -> b != null)
                    .toList();
        }
        return leaveBalanceRepository.findAll()
                .stream().map(leaveMapper::toBalanceResponse).toList();
    }


    public List<LeaveResponse> getLeavesByDepartment(String departmentId, LeaveStatus status) {
        List<String> empIds = employeeRepository.findByDepartmentId(departmentId)
                .stream().map(emp -> emp.getEmployeeId()).toList();
        List<LeaveRequest> requests = (status != null)
                ? leaveRequestRepository.findByEmployeeIdInAndStatus(empIds, status)
                : leaveRequestRepository.findByEmployeeIdIn(empIds);
        return requests.stream().map(leaveMapper::toResponse).toList();
    }

    public List<LeaveBalanceResponse> getLeaveBalancesByDepartment(String departmentId) {
        List<String> empIds = employeeRepository.findByDepartmentId(departmentId)
                .stream().map(emp -> emp.getEmployeeId()).toList();
        return leaveBalanceRepository.findByEmployeeIdIn(empIds)
                .stream().map(leaveMapper::toBalanceResponse).toList();
    }

    private void deductLeaveBalance(String employeeId, LeaveType leaveType, int days) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ValidationException("Leave balance not found for employee: " + employeeId));

        switch (leaveType) {
            case ANNUAL -> {
                if (balance.getAnnualLeaves() < days)
                    throw new ValidationException("Insufficient annual leave balance. Available: " + balance.getAnnualLeaves());
                balance.setAnnualLeaves(balance.getAnnualLeaves() - days);
            }
            case CASUAL -> {
                if (balance.getCasualLeaves() < days)
                    throw new ValidationException("Insufficient casual leave balance. Available: " + balance.getCasualLeaves());
                balance.setCasualLeaves(balance.getCasualLeaves() - days);
            }
            case SICK -> {
                if (balance.getSickLeaves() < days)
                    throw new ValidationException("Insufficient sick leave balance. Available: " + balance.getSickLeaves());
                balance.setSickLeaves(balance.getSickLeaves() - days);
            }
            case UNPAID -> {
                if (balance.getUnpaidLeaves() < days)
                    throw new ValidationException("Insufficient unpaid leave balance. Available: " + balance.getUnpaidLeaves());
                balance.setUnpaidLeaves(balance.getUnpaidLeaves() - days);
            }
        }

        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    private String generateLeaveId() {
        long count = leaveRequestRepository.count() + 1;
        return String.format("LVE-%04d", count);
    }
}
