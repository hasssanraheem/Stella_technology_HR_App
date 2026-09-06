package com.HR_Managment_System.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must contain @ and a valid domain")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
