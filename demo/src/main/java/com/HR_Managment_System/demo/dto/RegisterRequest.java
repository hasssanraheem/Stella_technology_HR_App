package com.HR_Managment_System.demo.dto;

import com.HR_Managment_System.demo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must contain @ and a valid domain")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;

    @NotNull(message = "Role is required (ADMIN, HR, EMPLOYEE)")
    private Role role;
}
