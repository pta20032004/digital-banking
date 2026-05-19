package com.tony.demo.infra.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tony.demo.modules.user.domain.Role;
import com.tony.demo.modules.user.domain.RoleRepository;
import com.tony.demo.modules.user.domain.Staff;
import com.tony.demo.modules.user.domain.StaffRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FakeStaffSeeder implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fake.admin.employeeCode}")
    private String adminEmployeeCode;

    @Value("${fake.admin.fullName}")
    private String adminFullName;

    @Value("${fake.admin.email}")
    private String adminEmail;

    @Value("${fake.admin.password}")
    private String adminPassword;

    @Value("${fake.admin.roleId}")
    private Long adminRoleId;

    @Value("${fake.admin.department}")
    private String adminDepartment;

    @Value("${fake.admin.branchCode}")
    private String adminBranchCode;

    @Value("${fake.manager.employeeCode}")
    private String managerEmployeeCode;

    @Value("${fake.manager.fullName}")
    private String managerFullName;

    @Value("${fake.manager.email}")
    private String managerEmail;

    @Value("${fake.manager.password}")
    private String managerPassword;

    @Value("${fake.manager.roleId}")
    private Long managerRoleId;

    @Value("${fake.manager.department}")
    private String managerDepartment;

    @Value("${fake.manager.branchCode}")
    private String managerBranchCode;

    @Value("${fake.teller.employeeCode}")
    private String tellerEmployeeCode;

    @Value("${fake.teller.fullName}")
    private String tellerFullName;

    @Value("${fake.teller.email}")
    private String tellerEmail;

    @Value("${fake.teller.password}")
    private String tellerPassword;

    @Value("${fake.teller.roleId}")
    private Long tellerRoleId;

    @Value("${fake.teller.department}")
    private String tellerDepartment;

    @Value("${fake.teller.branchCode}")
    private String tellerBranchCode;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        insertStaff(adminEmployeeCode, adminFullName, adminEmail, adminPassword, adminRoleId, adminDepartment, adminBranchCode, "admin");
        insertStaff(managerEmployeeCode, managerFullName, managerEmail, managerPassword, managerRoleId, managerDepartment, managerBranchCode, "manager");
        insertStaff(tellerEmployeeCode, tellerFullName, tellerEmail, tellerPassword, tellerRoleId, tellerDepartment, tellerBranchCode, "teller");
    }

    private void insertStaff(String employeeCode, String fullName, String email, String password, Long roleId, String department, String branchCode, String title) {
        if (!staffRepository.existsByEmployeeCode(employeeCode)) {
            log.info("Inserting fake {} from environment variables...", title);
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
            
            Staff staff = Staff.builder()
                    .employeeCode(employeeCode)
                    .fullName(fullName != null ? fullName.replace("\"", "") : "")
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .department(department)
                    .branchCode(branchCode)
                    .isEnabled(true)
                    .isLocked(false)
                    .failedAttempt(0)
                    .build();
            
            staffRepository.save(staff);
            log.info("Fake {} {} inserted successfully.", title, employeeCode);
        } else {
            log.info("Fake {} {} already exists in database.", title, employeeCode);
        }
    }
}
