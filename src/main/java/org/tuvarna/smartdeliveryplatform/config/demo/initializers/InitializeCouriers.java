package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;

import java.util.List;

@Order(6)
@Component
@Slf4j
public class InitializeCouriers implements CommandLineRunner {

    private final CourierService courierService;
    private final AdminService adminService;

    public InitializeCouriers(CourierService courierService, AdminService adminService) {
        this.courierService = courierService;
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        initializeCouriers();
    }

    private void initializeCouriers() {
        if (courierService.courierCountMoreThanZero()) {
            return;
        }

        log.info("Initializing demo couriers...");
        List<String> demoCourierEmails = getDemoCourierEmails();
        demoCourierEmails.forEach(this::registerCourier);
        log.info("Demo couriers initialized successfully");
    }

    private void registerCourier(String email) {
        adminService.makeUserCourier(email);
        courierService.toggleCourierAvailability(email);
    }

    private List<String> getDemoCourierEmails() {
        return List.of(
                DemoDataConstants.COURIER1_EMAIL,
                DemoDataConstants.COURIER2_EMAIL
        );
    }
}
