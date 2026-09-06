package com.HR_Managment_System.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceNoteRequest {

    @NotBlank(message = "Note cannot be blank")
    private String note;
}
