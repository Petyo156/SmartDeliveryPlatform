package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;

@Order(1)
@Component
@Slf4j
public class InitializeAdmin implements CommandLineRunner {
    private final UserService userService;
    private final AdminService adminService;

    public InitializeAdmin(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminService.adminAlreadyExists()){
            return;
        }

        insertAdmin();
    }

    private void insertAdmin() {
        log.info("Inserting admin user");
        registerAdmin();
    }

    private void registerAdmin() {
        UserRegisterRequest userRegisterRequest = initializeAdminRegisterRequest();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .userRegisterRequest(userRegisterRequest)
                .build();

        userService.register(registerRequest);
        adminService.makeUserAdmin(userRegisterRequest.getEmail());
    }

    private UserRegisterRequest initializeAdminRegisterRequest() {
        return UserRegisterRequest.builder()
                .firstName("Admin")
                .lastName("Adminov")
                .phoneNumber(DemoDataConstants.DEMO_PHONE)
                .email(DemoDataConstants.ADMIN_EMAIL)
                .password(DemoDataConstants.DEMO_PASSWORD)
                .confirmPassword(DemoDataConstants.DEMO_PASSWORD)
                .build();
    }
}
