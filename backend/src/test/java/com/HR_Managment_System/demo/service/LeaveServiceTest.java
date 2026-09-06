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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock LeaveRequestRepository leaveRequestRepository;
    @Mock LeaveBalanceRepository leaveBalanceRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock LeaveMapper leaveMapper;

    @InjectMocks LeaveService leaveService;

    @Test
    void applyLeave_happyPath_returnsLeaveResponse() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId("EMP-0001");
        dto.setLeaveType(LeaveType.ANNUAL);
        dto.setStartDate(LocalDate.of(2026, 10, 1));
        dto.setEndDate(LocalDate.of(2026, 10, 5));

        LeaveRequest entity = new LeaveRequest();
        LeaveResponse expected = new LeaveResponse();

        when(employeeRepository.findByEmployeeId("EMP-0001"))
                .thenReturn(Optional.of(new com.HR_Managment_System.demo.entity.Employee()));
        when(leaveRequestRepository.count()).thenReturn(0L);
        when(leaveMapper.toEntity(any(), anyString())).thenReturn(entity);
        when(leaveRequestRepository.save(entity)).thenReturn(entity);
        when(leaveMapper.toResponse(entity)).thenReturn(expected);

        LeaveResponse result = leaveService.applyLeave(dto);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void applyLeave_employeeNotFound_throwsEmployeeNotFoundException() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId("GHOST-99");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        when(employeeRepository.findByEmployeeId("GHOST-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.applyLeave(dto))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("GHOST-99");
    }

    @Test
    void applyLeave_endBeforeStart_throwsValidationException() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId("EMP-0001");
        dto.setStartDate(LocalDate.of(2026, 10, 5));
        dto.setEndDate(LocalDate.of(2026, 10, 1));

        when(employeeRepository.findByEmployeeId("EMP-0001"))
                .thenReturn(Optional.of(new com.HR_Managment_System.demo.entity.Employee()));

        assertThatThrownBy(() -> leaveService.applyLeave(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void updateLeaveStatus_approved_decrementsBalance() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setLeaveId("LVE-0001");
        leaveRequest.setEmployeeId("EMP-0001");
        leaveRequest.setLeaveType(LeaveType.ANNUAL);
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setStartDate(LocalDate.of(2026, 10, 1));
        leaveRequest.setEndDate(LocalDate.of(2026, 10, 3));

        LeaveBalance balance = new LeaveBalance();
        balance.setAnnualLeaves(20);

        LeaveStatusUpdateDto statusDto = new LeaveStatusUpdateDto();
        statusDto.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findByLeaveId("LVE-0001")).thenReturn(Optional.of(leaveRequest));
        when(leaveBalanceRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenReturn(leaveRequest);
        when(leaveMapper.toResponse(any())).thenReturn(new LeaveResponse());

        leaveService.updateLeaveStatus("LVE-0001", statusDto);

        assertThat(balance.getAnnualLeaves()).isEqualTo(17);
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    void updateLeaveStatus_alreadyApproved_throwsValidationException() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStatus(LeaveStatus.APPROVED);

        LeaveStatusUpdateDto statusDto = new LeaveStatusUpdateDto();
        statusDto.setStatus(LeaveStatus.REJECTED);

        when(leaveRequestRepository.findByLeaveId("LVE-0001")).thenReturn(Optional.of(leaveRequest));

        assertThatThrownBy(() -> leaveService.updateLeaveStatus("LVE-0001", statusDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void updateLeaveStatus_notFound_throwsLeaveRequestNotFoundException() {
        when(leaveRequestRepository.findByLeaveId("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.updateLeaveStatus("GHOST", new LeaveStatusUpdateDto()))
                .isInstanceOf(LeaveRequestNotFoundException.class);
    }

    @Test
    void getLeaveBalance_found_returnsBalance() {
        LeaveBalance balance = new LeaveBalance();
        balance.setAnnualLeaves(18);
        LeaveBalanceResponse expected = new LeaveBalanceResponse();

        when(employeeRepository.findByEmployeeId("EMP-0001"))
                .thenReturn(Optional.of(new com.HR_Managment_System.demo.entity.Employee()));
        when(leaveBalanceRepository.findByEmployeeId("EMP-0001")).thenReturn(Optional.of(balance));
        when(leaveMapper.toBalanceResponse(balance)).thenReturn(expected);

        LeaveBalanceResponse result = leaveService.getLeaveBalance("EMP-0001");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getLeaveBalance_employeeNotFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findByEmployeeId("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getLeaveBalance("GHOST"))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
