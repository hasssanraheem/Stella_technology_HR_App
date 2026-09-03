package com.HR_Managment_System.demo.entity;

import com.HR_Managment_System.demo.enums.HistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "employee_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistory {

    @Id
    private String id;

    @Indexed(unique = true)
    private String historyId;

    @Indexed
    private String employeeId;

    private HistoryType type;

    private String previousValue;
    private String newValue;
    private String note;

    private String recordedBy;

    private LocalDateTime recordedAt;
}
