package com.HR_Managment_System.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    private String id;

    @Indexed(unique = true)
    private String departmentId;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String managerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
