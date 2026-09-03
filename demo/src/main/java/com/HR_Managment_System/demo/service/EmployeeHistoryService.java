package com.HR_Managment_System.demo.service;

import com.HR_Managment_System.demo.dto.EmployeeHistoryResponse;
import com.HR_Managment_System.demo.dto.PerformanceNoteRequest;
import com.HR_Managment_System.demo.entity.EmployeeHistory;
import com.HR_Managment_System.demo.enums.HistoryType;
import com.HR_Managment_System.demo.exception.EmployeeNotFoundException;
import com.HR_Managment_System.demo.repository.EmployeeHistoryRepository;
import com.HR_Managment_System.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeHistoryService {

    private final EmployeeHistoryRepository employeeHistoryRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeHistoryResponse addPerformanceNote(String employeeId, PerformanceNoteRequest request) {
        employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));

        String recordedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeHistory history = EmployeeHistory.builder()
                .historyId(generateHistoryId())
                .employeeId(employeeId)
                .type(HistoryType.PERFORMANCE_NOTE)
                .note(request.getNote())
                .recordedBy(recordedBy)
                .recordedAt(LocalDateTime.now())
                .build();

        return toResponse(employeeHistoryRepository.save(history));
    }

    public List<EmployeeHistoryResponse> getHistory(String employeeId, HistoryType type) {
        employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));

        List<EmployeeHistory> records = (type != null)
                ? employeeHistoryRepository.findByEmployeeIdAndType(employeeId, type)
                : employeeHistoryRepository.findByEmployeeId(employeeId);

        return records.stream().map(this::toResponse).toList();
    }

    public void recordPromotion(String employeeId, String previousType, String newType) {
        String recordedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeHistory history = EmployeeHistory.builder()
                .historyId(generateHistoryId())
                .employeeId(employeeId)
                .type(HistoryType.PROMOTION)
                .previousValue(previousType)
                .newValue(newType)
                .recordedBy(recordedBy)
                .recordedAt(LocalDateTime.now())
                .build();

        employeeHistoryRepository.save(history);
    }

    public void recordDesignationChange(String employeeId, String previousDesignation, String newDesignation) {
        String recordedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeHistory history = EmployeeHistory.builder()
                .historyId(generateHistoryId())
                .employeeId(employeeId)
                .type(HistoryType.DESIGNATION_CHANGE)
                .previousValue(previousDesignation)
                .newValue(newDesignation)
                .recordedBy(recordedBy)
                .recordedAt(LocalDateTime.now())
                .build();

        employeeHistoryRepository.save(history);
    }

    private EmployeeHistoryResponse toResponse(EmployeeHistory h) {
        return EmployeeHistoryResponse.builder()
                .historyId(h.getHistoryId())
                .employeeId(h.getEmployeeId())
                .type(h.getType().name())
                .previousValue(h.getPreviousValue())
                .newValue(h.getNewValue())
                .note(h.getNote())
                .recordedBy(h.getRecordedBy())
                .recordedAt(h.getRecordedAt())
                .build();
    }

    private String generateHistoryId() {
        long count = employeeHistoryRepository.count() + 1;
        return String.format("HIST-%04d", count);
    }
}
