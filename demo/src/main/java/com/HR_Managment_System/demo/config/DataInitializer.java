package com.HR_Managment_System.demo.config;

import com.HR_Managment_System.demo.entity.Department;
import com.HR_Managment_System.demo.entity.User;
import com.HR_Managment_System.demo.enums.Role;
import com.HR_Managment_System.demo.repository.DepartmentRepository;
import com.HR_Managment_System.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default admin
        if (!userRepository.existsByEmail("admin@hrms.com")) {
            User admin = User.builder()
                    .email("admin@hrms.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Default admin created — email: admin@hrms.com | password: Admin@1234");
        } else {
            log.info("Admin account already exists — skipping seed.");
        }

        // Seed system Unassigned department
        if (!departmentRepository.existsByDepartmentId("DEPT-0000")) {
            Department unassigned = Department.builder()
                    .departmentId("DEPT-0000")
                    .name("Unassigned")
                    .description("Holding department for employees pending reassignment")
                    .isSystem(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            departmentRepository.save(unassigned);
            log.info("System department 'Unassigned' (DEPT-0000) created.");
        }
    }
}
