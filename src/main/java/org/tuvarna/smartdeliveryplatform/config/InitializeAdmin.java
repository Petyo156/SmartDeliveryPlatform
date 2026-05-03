package org.tuvarna.smartdeliveryplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

@Order(1)
@Component
public class InitializeAdmin implements CommandLineRunner {
    private final AdminService adminService;
    private final UserService userService;

    public InitializeAdmin(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userService.userCountMoreThanZero()){
            return;
        }

        adminService.insertAdmin();
    }
}
